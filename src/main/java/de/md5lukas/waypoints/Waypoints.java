/*
 *     Waypoints2, A plugin for spigot to add waypoints functionality
 *     Copyright (C) 2020  Lukas Planz
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.md5lukas.waypoints;

import de.md5lukas.commons.tags.LocationTag;
import de.md5lukas.nbt.Tags;
import de.md5lukas.waypoints.command.WaypointsCommand;
import de.md5lukas.waypoints.config.WPConfig;
import de.md5lukas.waypoints.display.WaypointDisplay;
import de.md5lukas.waypoints.lang.InventoryTranslations;
import de.md5lukas.waypoints.lang.Translations;
import de.md5lukas.waypoints.listener.WaypointsListener;
import de.md5lukas.waypoints.store.FileManager;
import de.md5lukas.waypoints.store.GlobalStore;
import de.md5lukas.waypoints.store.LegacyImporter;
import de.md5lukas.waypoints.util.FileHelper;
import de.md5lukas.waypoints.util.PlayerItemCheckRunner;
import de.md5lukas.waypoints.util.VaultHook;
import fr.minuskube.inv.SmartInvsPlugin;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Waypoints extends JavaPlugin {

    private static final int METRICS_PLUGIN_ID = 6864;

    private boolean inOnEnableDisable = false;

    private static Waypoints instance;
    private Translations translations;
    private FileManager fileManager;
    private GlobalStore globalStore;
    private boolean disabled = false;

    public static Plugin instance() {
        return instance;
    }

    public static Logger logger() {
        return instance.getLogger();
    }

    public static FileManager getFileManager() {
        return instance.fileManager;
    }

    public static GlobalStore getGlobalStore() {
        return instance.globalStore;
    }

    public static Translations getTranslations() {
        return instance.translations;
    }
    public static InventoryTranslations getITranslations() {
        return instance.translations.getInventoryTranslations();
    }

    public static boolean isDisabled() {
        return instance.disabled;
    }

    @Override
    public void onEnable() {
        instance = this;

        if (detectLegacy()) {
            File config = new File(getDataFolder(), "config.yml"), data = new File(getDataFolder(), "data");
            try {
                Files.move(config.toPath(), new File(getDataFolder(), "config.old.yml").toPath());
                Files.move(data.toPath(), new File(getDataFolder(), "data.old").toPath());
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Couldn't move old plugin data", e);
                inOnEnableDisable = true;
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        }

        setupExternalDependencies();

        try {
            loadConfig();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Couldn't load the config", e);
            inOnEnableDisable = true;
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (WPConfig.getGeneralConfig().getTeleportConfig().isVaultRequired()) {
            String result = VaultHook.setupEconomy();
            if (result != null) {
                getLogger().log(Level.SEVERE, result);
                inOnEnableDisable = true;
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        }

        fileManager = new FileManager(this);

        if (!extractLanguages())
            return;
        if (!loadGlobalStore())
            return;

        translations = new Translations();
        translations.updateLanguageStore();

        PlayerItemCheckRunner.start(this);
        WaypointDisplay.activateDisplays();

        getServer().getPluginManager().registerEvents(new WaypointsListener(), this);
        getCommand("waypoints").setExecutor(new WaypointsCommand());

        setupMetrics();
    }

    //<editor-fold defaultstate="collapsed" desc="onEnabled helpers">
    private void setupExternalDependencies() {
        Tags.registerTag(LocationTag::new);
        SmartInvsPlugin.setPlugin(this);
    }

    private boolean detectLegacy() {
        if (new File(getDataFolder(), "data").exists()) {
            getLogger().log(Level.INFO, "Loading legacy stores");
            LegacyImporter.importLegacy(getDataFolder());
            getLogger().log(Level.INFO, "Done!");
            return true;
        }
        return false;
    }

    private void loadConfig() throws IOException {
        saveResource("config.base.yml", true);

        File baseConfigFile = new File(getDataFolder(), "config.base.yml");
        File configFile = new File(getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            FileHelper.copyFile(baseConfigFile, configFile);
        }

        FileConfiguration baseConfig = YamlConfiguration.loadConfiguration(baseConfigFile);
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        config.setDefaults(baseConfig);

        WPConfig.loadConfig(config);
    }

    private boolean extractLanguages() {
        if (!new File(getDataFolder(), "lang/en.yml").exists())
            saveResource("lang/en.yml", false);
        if (!new File(getDataFolder(), "lang/de.yml").exists())
            saveResource("lang/de.yml", false);
        return true;
    }

    private boolean loadGlobalStore() {
        try {
            globalStore = new GlobalStore(this);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Couldn't load global store, shutting down plugin", e);
            inOnEnableDisable = true;
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }
        return true;
    }

    private void setupMetrics() {
        Metrics metrics = new Metrics(this, METRICS_PLUGIN_ID);
    }
    //</editor-fold>

    @Override
    public void onDisable() {
        disabled = true;
        SmartInvsPlugin.deleteStaticReferences();
        if (inOnEnableDisable)
            return;
        if (globalStore != null)
            globalStore.save(false);
    }
}
