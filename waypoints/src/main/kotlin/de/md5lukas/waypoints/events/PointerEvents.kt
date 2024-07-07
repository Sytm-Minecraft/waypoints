package de.md5lukas.waypoints.events

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.okkero.skedule.skedule
import de.md5lukas.waypoints.WaypointsPlugin
import de.md5lukas.waypoints.api.Waypoint
import de.md5lukas.waypoints.api.event.WaypointPostDeleteEvent
import de.md5lukas.waypoints.pointers.PlayerTrackable
import de.md5lukas.waypoints.pointers.WaypointTrackable
import de.md5lukas.waypoints.util.checkWorldAvailability
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.time.delay
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRespawnEvent

class PointerEvents(private val plugin: WaypointsPlugin) : Listener {

  private val pointerManager
    get() = plugin.pointerManager

  @EventHandler(priority = EventPriority.MONITOR)
  fun onPlayerRespawn(e: PlayerRespawnEvent) {
    val player = e.player
    val config = plugin.waypointsConfig.general.pointToDeathWaypointOnDeath
    if (plugin.waypointsConfig.general.features.deathWaypoints &&
        config.enabled &&
        checkWorldAvailability(plugin, e.respawnLocation.world!!)) {
      plugin.skedule(e.player) {
        plugin.api
            .getWaypointPlayer(e.player.uniqueId)
            .deathFolder
            .getWaypoints()
            .maxByOrNull { it.createdAt }
            ?.let { deathWaypoint ->
              val trackable = WaypointTrackable(plugin, deathWaypoint)
              pointerManager.enable(e.player, trackable)
              if (config.autoDeselectAfter.isPositive) {
                delay(config.autoDeselectAfter)
                if (player.isOnline) {
                  pointerManager.disable(player, trackable.asPredicate())
                } else {
                  val apiPlayer = plugin.api.getWaypointPlayer(player.uniqueId)
                  apiPlayer.setSelectedWaypoints(
                      apiPlayer.getSelectedWaypoints().filterNot { deathWaypoint.id == it.id })
                }
              }
            }
      }
    }
  }

  private val visitedCache: Cache<Player, Waypoint> =
      CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.SECONDS).build()

  @EventHandler
  fun onMove(e: PlayerMoveEvent) {
    val visitedRadius = plugin.waypointsConfig.general.teleport.visitedRadiusSquared

    plugin.pointerManager
        .getCurrentTargets(e.player)
        .mapNotNull(WaypointTrackable.Extract)
        .forEach {
          if (e.player.world === it.location.world) {
            if (e.player.location.distanceSquared(it.location) <= visitedRadius) {
              if (visitedCache.getIfPresent(e.player) != it) {
                visitedCache.put(e.player, it)
                plugin.skedule(e.player) { it.getWaypointMeta(e.player.uniqueId).setVisited(true) }
              }
            }
          }
        }
  }

  @EventHandler
  fun onWaypointDelete(e: WaypointPostDeleteEvent) {
    pointerManager.disableAll {
      if (it is Waypoint) {
        it.id == e.waypoint.id
      } else false
    }
  }

  @EventHandler
  fun onPlayerQuit(e: PlayerQuitEvent) {
    pointerManager.disableAll {
      if (it is PlayerTrackable) {
        it.player == e.player
      } else false
    }
  }

  @EventHandler
  fun onConfigReload(e: ConfigReloadEvent) {
    pointerManager.applyNewConfiguration(e.config.pointers)
  }
}
