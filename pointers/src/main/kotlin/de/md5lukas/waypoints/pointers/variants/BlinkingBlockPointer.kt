package de.md5lukas.waypoints.pointers.variants

import de.md5lukas.schedulers.AbstractScheduler
import de.md5lukas.waypoints.pointers.Pointer
import de.md5lukas.waypoints.pointers.PointerManager
import de.md5lukas.waypoints.pointers.StaticTrackable
import de.md5lukas.waypoints.pointers.Trackable
import de.md5lukas.waypoints.pointers.util.sendActualBlock
import java.util.*
import org.bukkit.Location
import org.bukkit.entity.Player

internal class BlinkingBlockPointer(
    pointerManager: PointerManager,
    player: Player,
    scheduler: AbstractScheduler,
) : Pointer(pointerManager, player, scheduler, PointerVariant.BLINKING_BLOCK) {

  private val config = pointerManager.configuration.blinkingBlock

  override val interval: Int
    get() = config.interval

  override val supportsMultipleTargets: Boolean
    get() = true

  private val counters: MutableMap<Trackable, Int> = HashMap()
  private val lastLocations: MutableMap<Trackable, Location> = HashMap()

  override fun update(trackable: Trackable, translatedTarget: Location?) {
    if (trackable !is StaticTrackable) return
    if (translatedTarget !== null) {
      val distance = player.location.distanceSquared(translatedTarget)
      if (distance >= config.minDistanceSquared && distance < config.maxDistanceSquared) {
        val currentCounter =
            counters.compute(trackable) { _, count ->
              if (count == null) {
                0
              } else {
                (count + 1) % config.blockDataSequence.size
              }
            }!!

        lastLocations[trackable] = translatedTarget
        player.sendBlockChange(translatedTarget, config.blockDataSequence[currentCounter])
      } else {
        hide(trackable, translatedTarget)
      }
    } else {
      hide(trackable, null)
    }
  }

  override fun hide(trackable: Trackable, translatedTarget: Location?) {
    if (trackable !is StaticTrackable) return
    val lastLocation = lastLocations.remove(trackable)
    counters.remove(trackable)
    if (lastLocation !== null) {
      player.sendActualBlock(lastLocation)
    }
  }
}
