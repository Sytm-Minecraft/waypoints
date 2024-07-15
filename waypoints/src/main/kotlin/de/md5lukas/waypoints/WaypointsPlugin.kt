package de.md5lukas.waypoints

import de.md5lukas.commons.paper.UUIDUtils
import de.md5lukas.commons.paper.registerEvents
import de.md5lukas.commons.time.DurationFormatter
import de.md5lukas.konfig.Konfig
import de.md5lukas.schedulers.Schedulers
import de.md5lukas.waypoints.api.SQLiteManager
import de.md5lukas.waypoints.api.WaypointsAPI
import de.md5lukas.waypoints.api.WaypointsPointerManager
import de.md5lukas.waypoints.api.base.DatabaseManager
import de.md5lukas.waypoints.command.WaypointsCommand
import de.md5lukas.waypoints.command.WaypointsScriptCommand
import de.md5lukas.waypoints.config.BlockDataAdapter
import de.md5lukas.waypoints.config.DurationAdapter
import de.md5lukas.waypoints.config.MaterialListAdapter
import de.md5lukas.waypoints.config.SoundAdapter
import de.md5lukas.waypoints.config.StyleAdapter
import de.md5lukas.waypoints.config.WaypointsConfiguration
import de.md5lukas.waypoints.config.general.TeleportPaymentType
import de.md5lukas.waypoints.events.ConfigReloadEvent
import de.md5lukas.waypoints.events.PointerEvents
import de.md5lukas.waypoints.events.WaypointsListener
import de.md5lukas.waypoints.integrations.BlueMapIntegration
import de.md5lukas.waypoints.integrations.DynMapIntegration
import de.md5lukas.waypoints.integrations.Pl3xMapIntegration
import de.md5lukas.waypoints.integrations.SquareMapIntegration
import de.md5lukas.waypoints.integrations.VaultIntegration
import de.md5lukas.waypoints.lang.Translations
import de.md5lukas.waypoints.lang.WorldTranslations
import de.md5lukas.waypoints.lang.YmlTranslationLoader
import de.md5lukas.waypoints.pointers.PointerManager
import de.md5lukas.waypoints.pointers.PointerManagerHooks
import de.md5lukas.waypoints.pointers.WaypointsPointerManagerImpl
import de.md5lukas.waypoints.tasks.CleanDatabaseTask
import de.md5lukas.waypoints.util.APIExtensions
import de.md5lukas.waypoints.util.TeleportManager
import de.md5lukas.waypoints.util.UpdateChecker
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import org.bstats.bukkit.Metrics
import org.bstats.charts.SimplePie
import org.bstats.charts.SingleLineChart
import org.bukkit.permissions.Permission
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin

class WaypointsPlugin : JavaPlugin() {

  @Suppress("PrivatePropertyName") private val METRICS_PLUGIN_ID = 6864

  internal lateinit var databaseManager: DatabaseManager
  lateinit var waypointsConfig: WaypointsConfiguration
    private set

  lateinit var api: WaypointsAPI
    private set

  val apiExtensions = APIExtensions(this)
  lateinit var pointerManager: PointerManager

  private lateinit var translationLoader: YmlTranslationLoader
  lateinit var translations: Translations
    private set

  lateinit var worldTranslations: WorldTranslations
    private set

  lateinit var teleportManager: TeleportManager
    private set

  lateinit var uuidUtils: UUIDUtils
    private set

  lateinit var durationFormatter: DurationFormatter
    private set

  private var vaultIntegration0: VaultIntegration? = null
  val vaultIntegration: VaultIntegration
    get() =
        vaultIntegration0
            ?: throw IllegalStateException(
                "The vault integration is configured to be used, but no vault compatible plugin is installed")

  var dynMapIntegrationAvailable = false
    private set

  var squareMapIntegrationAvailable = false
    private set

  var pl3xMapIntegrationAvailable = false
    private set

  private var blueMapIntegrationAvailable = false

  private lateinit var metrics: Metrics

  override fun onLoad() {
    Konfig.preloadClasses<WaypointsConfiguration>()
  }

  override fun onEnable() {
    try {
      Class.forName("io.papermc.paper.configuration.Configuration")
    } catch (_: ClassNotFoundException) {
      logger.log(Level.SEVERE, "Waypoints requires the Paper server implementation")
      server.pluginManager.disablePlugin(this)
      return
    }
    loadConfiguration()
    initDatabase()
    initApiServiceProvider()

    initTranslations()
    initTeleportManager()
    initCommons()
    initIntegrations()

    registerCommands()
    registerEvents()
    registerCustomizablePermissions(false)

    startMetrics()
    startBackgroundTasks()
  }

  // <editor-fold desc="onEnable Methods">
  private val konfig =
      Konfig(
          listOf(
              MaterialListAdapter,
              BlockDataAdapter,
              DurationAdapter,
              StyleAdapter,
              SoundAdapter,
          ))

  private fun loadConfiguration() {
    saveDefaultConfig()
    waypointsConfig = WaypointsConfiguration()
    konfig.deserializeInto(config, waypointsConfig)
  }

  fun reloadConfiguration() {
    saveDefaultConfig()
    reloadConfig()
    konfig.deserializeInto(config, waypointsConfig)

    ConfigReloadEvent(waypointsConfig).callEvent()
    registerCustomizablePermissions(true)
  }

  private fun initDatabase() {
    databaseManager =
        SQLiteManager(this, waypointsConfig.database, File(dataFolder, "waypoints.db"))

    databaseManager.initDatabase()

    api = databaseManager.api

    pointerManager = PointerManager(this, PointerManagerHooks(this), waypointsConfig.pointers)
  }

  private fun initApiServiceProvider() {
    server.servicesManager.register(WaypointsAPI::class.java, api, this, ServicePriority.Normal)
    server.servicesManager.register(
        WaypointsPointerManager::class.java,
        WaypointsPointerManagerImpl(this),
        this,
        ServicePriority.Normal)
    server.servicesManager.register(
        PointerManager::class.java, pointerManager, this, ServicePriority.Normal)
  }

  private fun initTranslations() {
    translationLoader = YmlTranslationLoader(this)

    translationLoader.loadLanguage(waypointsConfig.general.language)

    translations = Translations(translationLoader)

    worldTranslations = WorldTranslations(translationLoader)
  }

  private fun initTeleportManager() {
    teleportManager = TeleportManager(this)
  }

  private fun initCommons() {
    uuidUtils = UUIDUtils(Dispatchers.Default.asExecutor())
    durationFormatter = DurationFormatter { timeUnit, isPlural ->
      with(translations) {
        when (timeUnit) {
          TimeUnit.SECONDS -> if (isPlural) TEXT_DURATION_SECONDS else TEXT_DURATION_SECOND
          TimeUnit.MINUTES -> if (isPlural) TEXT_DURATION_MINUTES else TEXT_DURATION_MINUTE
          TimeUnit.HOURS -> if (isPlural) TEXT_DURATION_HOURS else TEXT_DURATION_HOUR
          TimeUnit.DAYS -> if (isPlural) TEXT_DURATION_DAYS else TEXT_DURATION_DAY
          else -> throw UnsupportedOperationException("The TimeUnit $timeUnit is not supported")
        }.rawText
      }
    }
  }

  private fun initIntegrations() {
    val integration = VaultIntegration(this)
    if (integration.setupEconomy()) {
      vaultIntegration0 = integration
    }

    if (waypointsConfig.general.features.globalWaypoints) {
      with(waypointsConfig.integrations) {
        if (dynmap.enabled) {
          dynMapIntegrationAvailable = DynMapIntegration(this@WaypointsPlugin).setupDynMap()
        }
        if (squaremap.enabled) {
          squareMapIntegrationAvailable =
              SquareMapIntegration(this@WaypointsPlugin).setupSquareMap()
        }
        if (bluemap.enabled) {
          blueMapIntegrationAvailable = BlueMapIntegration(this@WaypointsPlugin).setupBlueMap()
        }
        if (pl3xmap.enabled) {
          pl3xMapIntegrationAvailable = Pl3xMapIntegration(this@WaypointsPlugin).setupPl3xMap()
        }
      }
    }
  }

  @Suppress("UnstableApiUsage")
  private fun registerCommands() {
    lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {
      val registrar = it.registrar()
      registrar.register(
          WaypointsCommand(this).buildCommand(), waypointsConfig.general.commands.waypointsAliases)
      registrar.register(
          WaypointsScriptCommand(this).buildCommand(),
          waypointsConfig.general.commands.waypointsScriptAliases)
    }
  }

  private fun registerEvents() {
    registerEvents(WaypointsListener(this))
    registerEvents(PointerEvents(this))
  }

  private fun registerCustomizablePermissions(clearPrevious: Boolean) {
    val pm = server.pluginManager
    if (clearPrevious) {
      pm.permissions.forEach {
        val name = it.name
        if (name.startsWith(WaypointsPermissions.LIMIT_PREFIX_WAYPOINTS) ||
            name.startsWith(WaypointsPermissions.LIMIT_PREFIX_FOLDERS)) {
          pm.removePermission(name)
        }
      }
    }

    val permissions = mutableListOf<Permission>()
    waypointsConfig.general.waypoints.permissionLimits.mapTo(permissions) {
      Permission(WaypointsPermissions.LIMIT_PREFIX_WAYPOINTS + it)
    }
    waypointsConfig.general.folders.permissionLimits.mapTo(permissions) {
      Permission(WaypointsPermissions.LIMIT_PREFIX_FOLDERS + it)
    }

    pm.addPermissions(permissions)
  }

  private fun startMetrics() {
    metrics = Metrics(this, METRICS_PLUGIN_ID)

    with(api.statistics) {
      metrics.addCustomChart(SingleLineChart("total_waypoints") { totalWaypoints })
      metrics.addCustomChart(SingleLineChart("total_folders") { totalFolders })
    }
    metrics.addCustomChart(
        SimplePie("web_map") {
          when {
            dynMapIntegrationAvailable -> "DynMap"
            squareMapIntegrationAvailable -> "squaremap"
            blueMapIntegrationAvailable -> "BlueMap"
            pl3xMapIntegrationAvailable -> "Pl3xMap"
            else -> "none"
          }
        })
    metrics.addCustomChart(
        SimplePie("actually_uses_vault") {
          if (vaultIntegration0 !== null) {
                waypointsConfig.general.teleport
                    .let { arrayOf(it.private, it.death, it.public, it.permission) }
                    .any { it.paymentType === TeleportPaymentType.VAULT }
              } else {
                false
              }
              .toString()
        })
    metrics.addCustomChart(
        SimplePie("global_waypoints_enabled") {
          waypointsConfig.general.features.globalWaypoints.toString()
        })
    metrics.addCustomChart(
        SimplePie("death_waypoints_enabled") {
          waypointsConfig.general.features.deathWaypoints.toString()
        })
    metrics.addCustomChart(
        SimplePie("player_tracking_enabled") { waypointsConfig.playerTracking.enabled.toString() })
    metrics.addCustomChart(
        SimplePie("protocollib_available") {
          (server.pluginManager.getPlugin("ProtocolLib") !== null).toString()
        })
    metrics.addCustomChart(
        SimplePie("actionbar_pointer_enabled") {
          waypointsConfig.pointers.actionBar.enabled.toString()
        })
    metrics.addCustomChart(
        SimplePie("bossbar_pointer_enabled") {
          waypointsConfig.pointers.bossBar.enabled.toString()
        })
    metrics.addCustomChart(
        SimplePie("beacon_pointer_enabled") { waypointsConfig.pointers.beacon.enabled.toString() })
    metrics.addCustomChart(
        SimplePie("blinking_block_pointer_enabled") {
          waypointsConfig.pointers.blinkingBlock.enabled.toString()
        })
    metrics.addCustomChart(
        SimplePie("compass_pointer_enabled") {
          waypointsConfig.pointers.compass.enabled.toString()
        })
    metrics.addCustomChart(
        SimplePie("particle_pointer_enabled") {
          waypointsConfig.pointers.particle.enabled.toString()
        })
    metrics.addCustomChart(
        SimplePie("trails_pointer_enabled") { waypointsConfig.pointers.trail.enabled.toString() })
  }

  private fun startBackgroundTasks() {
    val scheduler = Schedulers.global(this)
    // Run once every day
    scheduler.scheduleAtFixedRateAsync(
        20 * 60 * 60 * 24, 20 * 60 * 60 * 24, CleanDatabaseTask(this))
    if (waypointsConfig.general.updateChecker) {
      scheduler.scheduleAsync(UpdateChecker(this, "Sytm", "waypoints"))
    }
  }
  // </editor-fold>

  override fun onDisable() {
    if (this::databaseManager.isInitialized) {
      databaseManager.close()
    }
    if (this::metrics.isInitialized) {
      metrics.shutdown()
    }
  }
}
