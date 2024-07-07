package de.md5lukas.waypoints.pointers.variants

import de.md5lukas.schedulers.AbstractScheduler
import de.md5lukas.waypoints.pointers.Pointer
import de.md5lukas.waypoints.pointers.PointerManager
import de.md5lukas.waypoints.pointers.Trackable
import de.md5lukas.waypoints.pointers.util.minus
import java.util.concurrent.ConcurrentHashMap
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.entity.*
import org.bukkit.entity.ItemDisplay.*
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.AxisAngle4f
import org.joml.Vector3f

internal class HologramPointer(
    pointerManager: PointerManager,
    player: Player,
    scheduler: AbstractScheduler,
) : Pointer(pointerManager, player, scheduler, PointerVariant.HOLOGRAM) {

  private val config = pointerManager.configuration.hologram

  override val interval: Int
    get() = config.interval

  override val supportsMultipleTargets: Boolean
    get() = true

  override val async
    get() = true

  private val activeHolograms: MutableMap<Trackable, Hologram> = ConcurrentHashMap()

  private lateinit var playerEyes: Location
  private lateinit var velocityOffset: Vector

  override fun preUpdates() {
    playerEyes = player.eyeLocation
    velocityOffset = player.velocity.setY(0).multiply(interval)
  }

  override fun update(trackable: Trackable, translatedTarget: Location?) {
    if (translatedTarget === null) return

    val hologramText = trackable.getHologramText(player, translatedTarget)

    if (hologramText === null) return
    val hologramTarget = translatedTarget.clone()

    hologramTarget.y += config.hologramHeightOffset

    val distanceSquared = playerEyes.distanceSquared(translatedTarget)

    val location =
        if (distanceSquared <= config.distanceFromPlayerSquared) {
          hologramTarget
        } else {
          val pVec = playerEyes.toVector()
          val tVec = hologramTarget.toVector()

          val direction = (tVec - pVec).normalize()

          pVec
              .add(direction.multiply(config.distanceFromPlayer))
              .add(velocityOffset)
              .toLocation(playerEyes.world!!)
        }

    if (trackable in activeHolograms) {
      val hologram = activeHolograms[trackable]!!

      hologram.location = location
      hologram.text = hologramText
    } else {
      activeHolograms[trackable] = Hologram(trackable, location, hologramText)
    }
  }

  override fun postUpdates() {
    scheduler.schedule { activeHolograms.values.forEach(Hologram::update) }
  }

  override fun hide(trackable: Trackable, translatedTarget: Location?) {
    scheduler.schedule { activeHolograms.remove(trackable)?.remove() }
  }

  override fun immediateCleanup(trackable: Trackable, translatedTarget: Location?) {
    activeHolograms.remove(trackable)?.remove()
  }

  private inner class Hologram(
      private val trackable: Trackable,
      var location: Location,
      var text: Component
  ) {

    private lateinit var textDisplay: TextDisplay
    private var itemDisplay: ItemDisplay? = null

    fun update() {
      // When teleporting between worlds, the chunks are immediately unloaded, so the display
      // entities are no longer valid and must be recreated
      if (::textDisplay.isInitialized && textDisplay.isValid && itemDisplay?.isValid != false) {
        textDisplay.teleportAsync(location)
        itemDisplay?.teleportAsync(location)

        // Only update the text if the display entity has been teleported into the same region as
        // the player again
        if (textDisplay.server.isOwnedByCurrentRegion(textDisplay)) {
          textDisplay.text(text)
        }
      } else {
        val world = player.world

        // Clean up possible remains of a previous entity
        if (::textDisplay.isInitialized && textDisplay.isValid) {
          val capture = textDisplay
          capture.scheduler.run(pointerManager.plugin, { capture.remove() }, {})
        }
        textDisplay =
            world.spawn(location, TextDisplay::class.java) {
              it.isPersistent = false
              it.isVisibleByDefault = false

              it.teleportDuration = interval

              it.billboard = Display.Billboard.CENTER

              it.text(text)
              it.isDefaultBackground = true
              it.isSeeThrough = true
            }
        player.showEntity(pointerManager.plugin, textDisplay)

        if (config.iconEnabled) {
          // Clean up possible remains of a previous entity
          if (itemDisplay?.isValid == true) {
            val capture = itemDisplay!!
            capture.scheduler.run(pointerManager.plugin, { capture.remove() }, {})
          }
          itemDisplay =
              trackable.hologramItem?.let { itemStack ->
                world
                    .spawn(location, ItemDisplay::class.java) {
                      it.isPersistent = false
                      it.isVisibleByDefault = false

                      it.teleportDuration = interval

                      it.itemStack = itemStack

                      val isBlock = itemStack.type.isBlock
                      it.billboard =
                          if (isBlock) {
                            Display.Billboard.FIXED
                          } else {
                            Display.Billboard.CENTER
                          }
                      it.transformation =
                          Transformation(
                              Vector3f(0.0f, config.iconOffset, 0.0f),
                              AxisAngle4f(),
                              Vector3f(if (isBlock) 1.0f else 0.6f),
                              AxisAngle4f(),
                          )
                      it.itemDisplayTransform = ItemDisplayTransform.FIXED
                    }
                    .also { player.showEntity(pointerManager.plugin, it) }
              }
        }
      }
    }

    fun remove() {
      textDisplay.remove()
      itemDisplay?.remove()
    }
  }
}
