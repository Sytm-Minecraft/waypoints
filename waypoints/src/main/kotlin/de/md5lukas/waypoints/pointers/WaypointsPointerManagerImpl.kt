package de.md5lukas.waypoints.pointers

import de.md5lukas.waypoints.WaypointsPlugin
import de.md5lukas.waypoints.api.Waypoint
import de.md5lukas.waypoints.api.WaypointsPointerManager
import org.bukkit.Location
import org.bukkit.entity.Player

class WaypointsPointerManagerImpl(private val plugin: WaypointsPlugin) : WaypointsPointerManager {

  private val realManager: PointerManager
    get() = plugin.pointerManager

  override fun enable(player: Player, waypoint: Waypoint) {
    realManager.enable(player, WaypointTrackable(plugin, waypoint))
  }

  override fun enable(player: Player, target: Player) {
    realManager.enable(player, PlayerTrackable(plugin, player))
  }

  override fun enable(player: Player, target: Location) {
    realManager.enable(player, TemporaryWaypointTrackable(plugin, target))
  }

  override fun disable(player: Player, waypoint: Waypoint) {
    realManager.disable(player) { it is WaypointTrackable && it.waypoint == waypoint }
  }

  override fun disable(player: Player, target: Player) {
    realManager.disable(player) { it is PlayerTrackable && it.player == target }
  }

  override fun disableAllTemporaryWaypoints(player: Player) {
    realManager.disable(player) { it is TemporaryWaypointTrackable }
  }

  override fun disableAll(player: Player) {
    realManager.disable(player) { true }
  }

  override fun getWaypoints(player: Player): Collection<Waypoint> {
    return realManager.getCurrentTargets(player).mapNotNull(WaypointTrackable.Extract)
  }

  override fun getTrackedPlayers(player: Player): Collection<Player> {
    return realManager.getCurrentTargets(player).mapNotNull { (it as? PlayerTrackable)?.player }
  }

  override fun getTemporaryWaypoints(player: Player): Collection<Location> {
    return realManager.getCurrentTargets(player).mapNotNull {
      (it as? TemporaryWaypointTrackable)?.location
    }
  }
}
