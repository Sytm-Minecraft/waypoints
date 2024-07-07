package de.md5lukas.waypoints.pointers.variants

import de.md5lukas.schedulers.AbstractScheduler
import de.md5lukas.waypoints.pointers.Pointer
import de.md5lukas.waypoints.pointers.PointerManager
import de.md5lukas.waypoints.pointers.StaticTrackable
import de.md5lukas.waypoints.pointers.Trackable
import de.md5lukas.waypoints.pointers.util.getAngleToTarget
import de.md5lukas.waypoints.pointers.util.normalizeAngleTo360
import de.md5lukas.waypoints.pointers.util.textComponent
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.util.HSVLike
import org.bukkit.Location
import org.bukkit.entity.Player

internal class BossBarPointer(
    pointerManager: PointerManager,
    player: Player,
    scheduler: AbstractScheduler,
) : Pointer(pointerManager, player, scheduler, PointerVariant.BOSS_BAR) {

  private val config = pointerManager.configuration.bossBar

  override val interval: Int
    get() = config.interval

  override val supportsMultipleTargets: Boolean
    get() = true

  override val async
    get() = true

  private val rawTitle: String
    get() = config.title

  private var bossBar: BossBar? = null
  private var counter = 0
  private val targetData = mutableMapOf<Trackable, TargetData>()

  private lateinit var playerLocation: Location

  override fun preUpdates() {
    playerLocation = player.location
  }

  override fun update(trackable: Trackable, translatedTarget: Location?) {
    if (translatedTarget === null) {
      hide(trackable, null)
      return
    }

    val data =
        targetData.computeIfAbsent(trackable) {
          TargetData(0f, (it as? StaticTrackable)?.beaconColor?.textColor ?: randomColor(it.seed))
        }

    // Don't calculate the position of the indicator everytime in favour to make the compass update
    // smoother
    if (counter == 0) {
      // Subtract 90° from the returned angle because Minecraft yaw is rotated by 90°
      data.angle = normalizeAngleTo360(getAngleToTarget(playerLocation, translatedTarget) - 90)
    }
  }

  override fun postUpdates() {
    if (targetData.isEmpty()) {
      bossBar?.let {
        player.hideBossBar(it)
        bossBar = null
      }
      return
    } else {
      if (bossBar === null) {
        bossBar =
            BossBar.bossBar(Component.empty(), 1f, config.barColor, config.barStyle).also {
              player.showBossBar(it)
            }
      }
    }
    counter = (counter + 1) % config.recalculateEveryNthInterval

    val playerAngle = normalizeAngleTo360(playerLocation.yaw)
    val offset = (playerAngle / 360 * rawTitle.length).roundToInt()
    val orientedTitle = config.title.loopingSubstring(rawTitle.length - 1, offset)

    val trackableIndices =
        targetData
            .map { (_, data) ->
              // Subtract 1 from the index to, because otherwise the indicator is always one too
              // much to the
              // right
              val indicatorIndex = (data.angle / 360 * rawTitle.length).roundToInt() - 1
              Math.floorMod(indicatorIndex - offset, rawTitle.length) to data.color
            }
            .sortedBy { it.first }

    bossBar?.name(
        textComponent {
          style(config.normalColor)
          var lastIndex = 0
          trackableIndices.forEach { (index, color) ->
            if (lastIndex > index) {
              return@forEach
            }
            append(Component.text(orientedTitle.substring(lastIndex, index)))
            lastIndex = index + 1
            append(
                textComponent {
                  style(config.indicatorStyle)
                  color(color)
                  content(config.indicator)
                })
          }
          append(Component.text(orientedTitle.substring(min(orientedTitle.length, lastIndex))))
        })
  }

  override fun hide(trackable: Trackable, translatedTarget: Location?) {
    targetData.remove(trackable)
    if (targetData.isEmpty()) {
      bossBar?.let { player.hideBossBar(it) }
      bossBar = null
    }
  }

  private class TargetData(var angle: Float, val color: TextColor)

  private fun String.loopingSubstring(size: Int, offset: Int): String {
    val modOffset = Math.floorMod(offset, length)

    val overhang = max(0, (size + modOffset) - length)

    return substring(modOffset, min(length, modOffset + size)) + substring(0, overhang)
  }

  private fun randomColor(seed: Long) =
      TextColor.color(HSVLike.hsvLike(Random(seed).nextFloat(), 1f, 1f))
}
