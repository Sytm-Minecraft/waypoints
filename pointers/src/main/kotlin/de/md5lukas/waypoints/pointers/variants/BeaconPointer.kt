package de.md5lukas.waypoints.pointers.variants

import de.md5lukas.schedulers.AbstractScheduler
import de.md5lukas.waypoints.pointers.*
import de.md5lukas.waypoints.pointers.util.blockEquals
import de.md5lukas.waypoints.pointers.util.highestBlock
import de.md5lukas.waypoints.pointers.util.sendActualBlock
import java.util.*
import kotlin.math.max
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player

internal class BeaconPointer(
    pointerManager: PointerManager,
    player: Player,
    scheduler: AbstractScheduler,
) : Pointer(pointerManager, player, scheduler, PointerVariant.BEACON) {

  private val config = pointerManager.configuration.beacon

  override val interval: Int
    get() = config.interval

  override val supportsMultipleTargets: Boolean
    get() = true

  private val beacon = Material.BEACON.createBlockData()

  private val activeBeacons: MutableMap<Trackable, Location> = HashMap()

  override fun update(trackable: Trackable, translatedTarget: Location?) {
    if (trackable !is StaticTrackable) return
    if (translatedTarget !== null) {
      val distance = player.location.distanceSquared(translatedTarget)

      if (distance >= config.minDistanceSquared && distance < config.maxDistanceSquared) {
        val beaconBase = translatedTarget.highestBlock.location

        val lastBeaconPosition = activeBeacons[trackable]
        if (lastBeaconPosition != null && !lastBeaconPosition.blockEquals(beaconBase)) {
          hide0(trackable, lastBeaconPosition)
        }

        activeBeacons[trackable] = beaconBase

        sendBeacon(beaconBase, trackable, true)
      } else {
        hide(trackable, translatedTarget)
      }
    }
  }

  override fun hide(trackable: Trackable, translatedTarget: Location?) {
    if (trackable !is StaticTrackable) return
    hide0(trackable, activeBeacons.remove(trackable))
  }

  private fun hide0(trackable: Trackable, lastBeaconPosition: Location?) {
    if (lastBeaconPosition != null && player.world == lastBeaconPosition.world) {
      sendBeacon(lastBeaconPosition, trackable, false)
    }
  }

  private fun sendBeacon(beaconBase: Location, trackable: Trackable, create: Boolean) {
    val loc = beaconBase.clone()
    val world = loc.world!!

    loc.y = max(loc.blockY, world.minHeight + 2).toDouble()

    if (create) {
      player.sendBlockChange(
          loc,
          ((trackable as? StaticTrackable)?.beaconColor
                  ?: config.getDefaultColor(trackable)
                  ?: BeaconColor.CLEAR)
              .blockData)
    } else {
      player.sendActualBlock(loc)
    }
    loc.y--
    if (create) {
      player.sendBlockChange(loc, beacon)
    } else {
      player.sendActualBlock(loc)
    }
    loc.y--

    // Create or remove 3x3 base platform
    for (x in -1..1) {
      for (z in -1..1) {
        loc.add(x.toDouble(), 0.0, z.toDouble())
        if (create) {
          player.sendBlockChange(loc, config.baseBlock)
        } else {
          player.sendActualBlock(loc)
        }
        loc.subtract(x.toDouble(), 0.0, z.toDouble())
      }
    }
  }
}
