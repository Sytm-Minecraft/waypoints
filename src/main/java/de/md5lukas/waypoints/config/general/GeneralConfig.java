/*
 *     Waypoints2, A plugin for spigot to add waypoints functionality
 *     Copyright (C) 2019-2020 Lukas Planz
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

package de.md5lukas.waypoints.config.general;

import de.md5lukas.commons.language.Languages;
import de.md5lukas.waypoints.Messages;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GeneralConfig {

    private boolean deathWaypointEnabled;
    private int waypointLimit, folderLimit;
    private OpenUsingCompass openUsingCompass;

    private final RenamingConfig renamingConfig;
    private final DuplicateNameConfig duplicateConfig;
    private final TeleportConfig teleportConfig;

    private final Map<String, Map<String, String>> worldNameAliases;

    public GeneralConfig(ConfigurationSection cfg) {
        renamingConfig = new RenamingConfig();
        duplicateConfig = new DuplicateNameConfig();
        teleportConfig = new TeleportConfig();

        worldNameAliases = new HashMap<>();
        load(cfg);
    }

    public void load(ConfigurationSection cfg) {
        deathWaypointEnabled = cfg.getBoolean("deathWaypointEnabled");
        waypointLimit = cfg.getInt("limits.waypoints");
        folderLimit = cfg.getInt("limits.folders");

        openUsingCompass = OpenUsingCompass.getFromConfig(cfg.getString("openUsingCompass"));

        renamingConfig.load(Objects.requireNonNull(cfg.getConfigurationSection("allowRenaming")));
        duplicateConfig.load(Objects.requireNonNull(cfg.getConfigurationSection("allowDuplicateNames")));
        teleportConfig.load(Objects.requireNonNull(cfg.getConfigurationSection("teleport")));

        worldNameAliases.clear();
        for (String lang : Objects.requireNonNull(cfg.getConfigurationSection("worldNameAliases")).getKeys(false)) {
            Map<String, String> aliases = new HashMap<>();
            worldNameAliases.put(lang.toLowerCase(), aliases);
            for (String world : Objects.requireNonNull(cfg.getConfigurationSection("worldNameAliases." + lang)).getKeys(false)) {
                aliases.put(world, cfg.getString("worldNameAliases." + lang + "." + world));
            }
        }
    }

    public boolean isDeathWaypointEnabled() {
        return deathWaypointEnabled;
    }

    public int getWaypointLimit() {
        return waypointLimit;
    }

    public int getFolderLimit() {
        return folderLimit;
    }

    public OpenUsingCompass getOpenUsingCompass() {
        return openUsingCompass;
    }

    public RenamingConfig getRenamingConfig() {
        return renamingConfig;
    }

    public DuplicateNameConfig getDuplicateConfig() {
        return duplicateConfig;
    }

    public TeleportConfig getTeleportConfig() {
        return teleportConfig;
    }

    @Deprecated
    public String translateWorldName(String world, String language) {
        if (!worldNameAliases.containsKey(language))
            language = Languages.getDefaultLanguage();
        if (!worldNameAliases.containsKey(language))
            return world;
        String translated = worldNameAliases.get(language).get(world);
        return translated == null ? world : translated;
    }

    @Deprecated
    public String translateWorldName(String world, CommandSender sender) {
        return translateWorldName(world, Languages.getLanguage(sender));
    }

    @Deprecated
    public void checkWorldTranslations() {
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        StringBuilder message = new StringBuilder();
        for (String lang : worldNameAliases.keySet()) {
            boolean firstLang = true;
            for (World w : Bukkit.getWorlds()) {
                if (!worldNameAliases.get(lang).containsKey(w.getName())) {
                    if (firstLang) {
                        firstLang = false;
                        message.append("\n&c").append(lang).append(": &e").append(w.getName());
                    } else {
                        message.append(", ").append(w.getName());
                    }
                }
            }
        }
        String messageString = message.toString();
        console.sendMessage(Messages.CHAT_WARNING_WORLD_TRANSLATIONS_MISSING.getRaw(console) + messageString);
        Bukkit.getOnlinePlayers().stream().map(p -> (Player) p).filter(p ->
                p.hasPermission("waypoints.admin")).forEach(p -> p.sendMessage(Messages.CHAT_WARNING_WORLD_TRANSLATIONS_MISSING.getRaw(p) + messageString));
    }
}
