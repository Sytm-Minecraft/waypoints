package de.md5lukas.waypoints.pointers

import de.md5lukas.schedulers.AbstractScheduler
import de.md5lukas.waypoints.pointers.variants.PointerVariant
import org.bukkit.Location
import org.bukkit.entity.Player

internal abstract class Pointer(
    protected val pointerManager: PointerManager,
    protected val player: Player,
    protected val scheduler: AbstractScheduler,
    val variant: PointerVariant,
) {

  protected val syncExecutor = scheduler.asExecutor()

  abstract val interval: Int
  abstract val supportsMultipleTargets: Boolean
  open val async
    get() = false

  open fun show(trackable: Trackable, translatedTarget: Location?) {
    update(trackable, translatedTarget)
  }

  open fun preUpdates() {}

  open fun update(trackable: Trackable, translatedTarget: Location?) {}

  open fun postUpdates() {}

  open fun hide(trackable: Trackable, translatedTarget: Location?) {}

  open fun immediateCleanup(trackable: Trackable, translatedTarget: Location?) =
      hide(trackable, translatedTarget)
}
