package de.md5lukas.waypoints.pointers.variants

import de.md5lukas.schedulers.AbstractScheduler
import de.md5lukas.waypoints.pointers.Pointer
import de.md5lukas.waypoints.pointers.PointerManager
import de.md5lukas.waypoints.pointers.Trackable
import java.util.concurrent.CompletableFuture
import org.bukkit.Location
import org.bukkit.entity.Player

internal class CompassPointer(
    pointerManager: PointerManager,
    player: Player,
    scheduler: AbstractScheduler,
) : Pointer(pointerManager, player, scheduler, PointerVariant.COMPASS) {

  private val config = pointerManager.configuration.compass

  override val interval: Int
    get() = config.interval

  override val supportsMultipleTargets: Boolean
    get() = false

  private var isTracking = false
  private var restoreCompassTarget: CompletableFuture<Void>? = null

  override fun show(trackable: Trackable, translatedTarget: Location?) {
    val currentCompassTarget = player.compassTarget

    val localFuture = restoreCompassTarget
    if (localFuture === null || localFuture.isDone) {
      pointerManager.hooks.saveCompassTarget(player, currentCompassTarget)
    }
    restoreCompassTarget = null

    update(trackable, translatedTarget)
  }

  override fun update(trackable: Trackable, translatedTarget: Location?) {
    isTracking = true

    val location = translatedTarget ?: trackable.location
    player.compassTarget = location
  }

  override fun hide(trackable: Trackable, translatedTarget: Location?) {
    isTracking = false

    restoreCompassTarget =
        pointerManager.hooks
            .loadCompassTarget(player)
            .thenAcceptAsync(
                {
                  if (it != null && !isTracking) {
                    player.compassTarget = it
                  }
                },
                syncExecutor)
  }

  override fun immediateCleanup(trackable: Trackable, translatedTarget: Location?) {
    isTracking = false
    // We need to join here because if the player leaves and is the last one keeping the chunk
    // loaded
    // after disconnect all chunks are basically unloaded and saved instantaneously and
    // modifications
    // to the player after that are no longer saved
    pointerManager.hooks.loadCompassTarget(player).join()?.let { player.compassTarget = it }
  }
}
