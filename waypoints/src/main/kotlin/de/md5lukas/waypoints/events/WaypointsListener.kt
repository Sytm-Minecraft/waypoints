package de.md5lukas.waypoints.events

import com.okkero.skedule.skedule
import de.md5lukas.waypoints.WaypointsPlugin
import de.md5lukas.waypoints.gui.WaypointsGUI
import de.md5lukas.waypoints.util.checkWorldAvailability
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent

class WaypointsListener(private val plugin: WaypointsPlugin) : Listener {

  @EventHandler
  fun onPlayerDeath(e: PlayerDeathEvent) {
    if (plugin.waypointsConfig.general.features.deathWaypoints &&
        checkWorldAvailability(plugin, e.entity.world)) {
      plugin.skedule {
        plugin.api.getWaypointPlayer(e.entity.uniqueId).addDeathLocation(e.entity.location)
      }
    }
  }

  @EventHandler
  fun onPlayerInteract(e: PlayerInteractEvent) {
    val config = plugin.waypointsConfig.general.openWithItem
    if (config.enabled &&
        (!config.mustSneak || e.player.isSneaking) &&
        e.action in config.validClicks &&
        e.material in config.items) {
      // Run in next tick to hopefully fix https://github.com/Sytm/waypoints/issues/86 (will be run
      // automatically in next tick due to context switch)
      WaypointsGUI(plugin, e.player, e.player.uniqueId)
    }
  }

  @EventHandler
  fun onConfigReload(e: ConfigReloadEvent) {
    plugin.server.onlinePlayers.forEach(Player::updateCommands)
  }
}
