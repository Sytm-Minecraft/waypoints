package de.md5lukas.waypoints.pointers.variants

import de.md5lukas.schedulers.AbstractScheduler
import de.md5lukas.waypoints.pointers.Pointer
import de.md5lukas.waypoints.pointers.PointerManager
import de.md5lukas.waypoints.pointers.Trackable
import de.md5lukas.waypoints.pointers.util.div
import org.bukkit.Location
import org.bukkit.entity.Player

internal class ParticlePointer(
    pointerManager: PointerManager,
    player: Player,
    scheduler: AbstractScheduler,
) : Pointer(pointerManager, player, scheduler, PointerVariant.PARTICLE) {

  private val config = pointerManager.configuration.particle

  override val interval: Int
    get() = config.interval

  override val supportsMultipleTargets: Boolean
    get() = true

  override fun update(trackable: Trackable, translatedTarget: Location?) {
    if (translatedTarget !== null) {
      val pLoc = player.location
      var dir = translatedTarget.toVector().subtract(pLoc.toVector())

      if (!config.showVerticalDirection) {
        dir.y = 0.0
      }

      dir = dir.normalize().multiply(config.length)

      dir /= config.amount

      for (i in 0 until config.amount) {
        var y = config.heightOffset

        if (config.showVerticalDirection) {
          y += dir.y * i
        }

        player.spawnParticle(
            config.particle,
            pLoc.x + (dir.x * i),
            pLoc.y + y,
            pLoc.z + (dir.z * i),
            1,
            0.0,
            0.0,
            0.0,
            0.0,
        )
      }
    }
  }
}
