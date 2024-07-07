package de.md5lukas.waypoints.pointers.variants

import de.md5lukas.pathfinder.PathSuccess
import de.md5lukas.pathfinder.Pathfinder
import de.md5lukas.pathfinder.behaviour.BasicPlayerPathingStrategy
import de.md5lukas.pathfinder.behaviour.ConstantFWeigher
import de.md5lukas.schedulers.AbstractScheduler
import de.md5lukas.schedulers.Schedulers
import de.md5lukas.waypoints.pointers.Pointer
import de.md5lukas.waypoints.pointers.PointerManager
import de.md5lukas.waypoints.pointers.StaticTrackable
import de.md5lukas.waypoints.pointers.Trackable
import de.md5lukas.waypoints.pointers.config.TrailConfiguration
import de.md5lukas.waypoints.pointers.util.blockEquals
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.ceil
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

internal class TrailPointer(
    pointerManager: PointerManager,
    player: Player,
    scheduler: AbstractScheduler,
) : Pointer(pointerManager, player, scheduler, PointerVariant.TRAIL) {

  private val config = pointerManager.configuration.trail

  companion object {
    private val pathfinderLock = Any()
    private var pathfinder: Pathfinder? = null

    fun getPathfinder(plugin: Plugin, config: TrailConfiguration): Pathfinder {
      pathfinder?.let {
        return it
      }
      synchronized(pathfinderLock) {
        pathfinder?.let {
          return it
        }
        val pathfinder =
            with(config) {
              Pathfinder(
                  plugin = plugin,
                  executor = Schedulers.global(plugin).asExecutor(async = true),
                  maxIterations = pathingMaxIterations,
                  maxLength = pathingMaxLength,
                  pathingStrategy =
                      BasicPlayerPathingStrategy(pathingSwimPenalty > 0.0, pathingSwimPenalty),
                  allowChunkLoading = pathingAllowChunkLoading,
                  allowChunkGeneration = pathingAllowChunkGeneration,
                  weigher = ConstantFWeigher(pathingHeuristicWeight),
              )
            }
        this.pathfinder = pathfinder
        return pathfinder
      }
    }

    fun resetPathfinder() {
      synchronized(pathfinderLock) { pathfinder = null }
    }
  }

  override val interval: Int
    get() = config.interval

  override val supportsMultipleTargets: Boolean
    get() = false

  private var highlightCounter: Int = 0

  private val pathfinder = getPathfinder(pointerManager.plugin, config)

  private val locationTrail: CopyOnWriteArrayList<Location> = CopyOnWriteArrayList()
  private var lastFuture: CompletableFuture<*> = CompletableFuture.completedFuture(Unit)

  override fun update(trackable: Trackable, translatedTarget: Location?) {
    if (translatedTarget === null || trackable !is StaticTrackable) {
      return
    }
    if (player.world !== translatedTarget.world) {
      hide(trackable, translatedTarget)
      return
    }

    if (lastFuture.isDone) {
      // If the player has moved too far away from any parts of the calculated trail invalidate the
      // previous trail or if the calculated trail is only one block long because the path couldn't
      // be calculated
      if (player.world !== locationTrail.firstOrNull()?.world ||
          locationTrail.all {
            val squared = player.location.distanceSquared(it)
            val outOfReach = squared >= config.pathInvalidationDistanceSquared
            outOfReach
          } ||
          (locationTrail.size == 1 && !locationTrail.last().blockEquals(translatedTarget))) {
        locationTrail.clear()
      }

      if (locationTrail.isEmpty()) {
        lastFuture =
            pathfinder
                .findPath(player.location, translatedTarget)
                .thenApply { result ->
                  if (result is PathSuccess) {
                    locationTrail.addAll(result.path.map { it.toCenterLocation0() })
                  }
                }
                .exceptionally {
                  pointerManager.plugin.slF4JLogger.error("Couldn't calculate path", it)
                }
      } else {
        val last = locationTrail.last()
        if (!last.blockEquals(translatedTarget) &&
            (player.world !== last.world ||
                player.location.distanceSquared(last) < config.pathCalculateAheadDistanceSquared)) {
          lastFuture =
              pathfinder
                  .findPath(last, translatedTarget)
                  .thenApply { result ->
                    if (result is PathSuccess) {
                      // Remove everything behind the player further away than X
                      val lastIndex =
                          locationTrail.indexOfLast {
                            player.world !== it.world ||
                                player.location.distanceSquared(it) >=
                                    config.retainMaxPlayerDistanceSquared
                          }
                      if (lastIndex > 0) {
                        locationTrail.subList(0, lastIndex).clear()
                      }
                      locationTrail.addAll(
                          result.path.mapIndexedNotNull { index, pathPosition ->
                            if (index == 0) {
                              null // Skip start because it is already contained in trail
                            } else {
                              pathPosition.toCenterLocation0()
                            }
                          })
                    } else {
                      // Can no longer calculate path. Invalidate entire trail
                      locationTrail.clear()
                    }
                  }
                  .exceptionally {
                    pointerManager.plugin.slF4JLogger.error("Couldn't calculate path", it)
                  }
        }
      }
    }

    locationTrail.forEachIndexed { index, location ->
      val isHighlight = ((index - highlightCounter) % config.highlightDistance) == 0

      player.spawnParticle(
          if (isHighlight) {
            config.particleHighlight
          } else {
            config.particleNormal
          },
          location,
          if (isHighlight) {
            ceil(config.particleAmount * 1.5).toInt()
          } else {
            config.particleAmount
          },
          config.particleSpread,
          config.particleSpread,
          config.particleSpread,
          0.0,
      )
    }

    highlightCounter = (highlightCounter + 1) % config.highlightDistance
  }

  override fun hide(trackable: Trackable, translatedTarget: Location?) {
    locationTrail.clear()
    highlightCounter = 0
    lastFuture.cancel(true)
  }

  private fun Location.toCenterLocation0(): Location {
    x += 0.5
    y += 0.5
    z += 0.5
    return this
  }
}
