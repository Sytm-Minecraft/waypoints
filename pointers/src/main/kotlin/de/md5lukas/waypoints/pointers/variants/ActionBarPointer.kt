package de.md5lukas.waypoints.pointers.variants

import de.md5lukas.waypoints.pointers.Pointer
import de.md5lukas.waypoints.pointers.PointerManager
import de.md5lukas.waypoints.pointers.Trackable
import de.md5lukas.waypoints.pointers.config.ActionBarConfiguration
import de.md5lukas.waypoints.pointers.util.getAngleToTarget
import de.md5lukas.waypoints.pointers.util.textComponent
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.entity.Player
import kotlin.math.roundToInt

internal class ActionBarPointer(
    pointerManager: PointerManager,
    private val config: ActionBarConfiguration,
) : Pointer(pointerManager, config.interval) {

    override fun update(player: Player, trackable: Trackable, translatedTarget: Location?) {
        player.sendActionBar(
            if (translatedTarget !== null) {
                if (config.showDistanceEnabled && player.isSneaking) {
                    pointerManager.hooks.actionBarHooks.formatDistanceMessage(
                        player,
                        player.location.distance(translatedTarget),
                        player.location.y - trackable.location.y
                    )
                } else {
                    // TODO properly convert to adventure
                    generateDirectionIndicator(
                        deltaAngleToTarget(
                            player.location,
                            translatedTarget
                        )
                    )
                }
            } else {
                pointerManager.hooks.actionBarHooks.formatWrongWorldMessage(player, player.world, trackable.location.world!!)
            }
        )
    }

    private fun generateDirectionIndicator(angle: Double): Component {
        if (angle > config.range) {
            return textComponent {
                style(config.indicatorColor)
                content(config.leftArrow)
                append(textComponent {
                    style(config.normalColor)
                    content(config.section.repeat(config.amountOfSections) + config.rightArrow)
                })
            }
        }
        if (-angle > config.range) {
            return textComponent {
                style(config.normalColor)
                content(config.leftArrow + config.section.repeat(config.amountOfSections))
                append(textComponent {
                    style(config.indicatorColor)
                    content(config.rightArrow)
                })
            }
        }

        val percent: Double = -(angle / config.range)

        var nthSection = ((config.amountOfSections - 1).toDouble() / 2 * percent).roundToInt()

        nthSection += (config.amountOfSections.toDouble() / 2).roundToInt()

        return textComponent {
            style(config.normalColor)
            content(config.leftArrow + config.section.repeat(nthSection - 1))
            append(textComponent {
                style(config.indicatorColor)
                content(config.section)
            })
            append(textComponent {
                content(config.section.repeat(config.amountOfSections - nthSection) + config.rightArrow)
            })
        }
    }

    /**
     * The returned values range from -180 to 180 degrees, whereas negative numbers mean you look too much left and
     * positive numbers you look too much right
     *
     * @param location The location to calculate the angle from
     * @param target   The target when looked at the angle is 0
     * @return The delta angle
     */
    private fun deltaAngleToTarget(location: Location, target: Location): Double {
        var playerAngle = (location.yaw + 90).toDouble()

        while (playerAngle < 0) {
            playerAngle += 360.0
        }

        var angle = playerAngle - getAngleToTarget(location, target) + 180

        while (angle > 360) {
            angle -= 360.0
        }

        if (angle > 180) {
            angle = -(360 - angle)
        }

        return angle
    }
}