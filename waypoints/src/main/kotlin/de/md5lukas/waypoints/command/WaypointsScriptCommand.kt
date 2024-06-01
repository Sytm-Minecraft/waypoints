package de.md5lukas.waypoints.command

import com.mojang.brigadier.tree.LiteralCommandNode
import com.okkero.skedule.skedule
import de.md5lukas.commons.paper.isOutOfBounds
import de.md5lukas.commons.paper.placeholder
import de.md5lukas.paper.brigadier.arguments.*
import de.md5lukas.paper.brigadier.executors.*
import de.md5lukas.paper.brigadier.requirements.*
import de.md5lukas.waypoints.WaypointsPermissions
import de.md5lukas.waypoints.WaypointsPlugin
import de.md5lukas.waypoints.command.arguments.BeaconColorArgument
import de.md5lukas.waypoints.command.arguments.WaypointsSuggestionProvider
import de.md5lukas.waypoints.pointers.BeaconColor
import de.md5lukas.waypoints.pointers.TemporaryWaypointTrackable
import de.md5lukas.waypoints.pointers.WaypointTrackable
import de.md5lukas.waypoints.util.labelResolver
import de.md5lukas.waypoints.util.searchWaypoints
import io.papermc.paper.command.brigadier.CommandSourceStack
import java.util.UUID
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Suppress("UnstableApiUsage")
class WaypointsScriptCommand(private val plugin: WaypointsPlugin) {

  private val translations
    get() = plugin.translations

  fun buildCommand(): LiteralCommandNode<CommandSourceStack> =
      command("waypointsscript") {
        requiresPermission(WaypointsPermissions.COMMAND_SCRIPTING)
        executes<CommandSender> { sender, context ->
          val labelResolver = context.labelResolver
          translations.COMMAND_SCRIPT_HELP_HEADER.send(sender)
          translations.COMMAND_SCRIPT_HELP_DESELECT_WAYPOINT.send(sender, labelResolver)
          translations.COMMAND_SCRIPT_HELP_SELECT_WAYPOINT.send(sender, labelResolver)
          translations.COMMAND_SCRIPT_HELP_TEMPORARY_WAYPOINT.send(sender, labelResolver)
          translations.COMMAND_SCRIPT_HELP_UUID.send(sender, labelResolver)
        }
        literal("deselectWaypoint") {
          player("player") {
            executes<CommandSender> { _, context ->
              val player = context.getPlayer("player")

              plugin.pointerManager.disable(player) { true }
            }
          }
        }
        literal("selectWaypoint") {
          player("player") {
            uuid("waypoint-id") {
              executes<CommandSender> { sender, context ->
                val player = context.getPlayer("player")
                val uuid: UUID = context["waypoint-id"]

                plugin.skedule(player) {
                  val waypoint = plugin.api.getWaypointByID(uuid)

                  if (waypoint == null) {
                    translations.COMMAND_SCRIPT_SELECT_WAYPOINT_WAYPOINT_NOT_FOUND.send(
                        sender, "uuid" placeholder uuid.toString())
                    return@skedule
                  }

                  plugin.pointerManager.enable(player, WaypointTrackable(plugin, waypoint))
                }
              }
            }
          }
        }
        literal("temporaryWaypoint") {
          player("player") {
            blockPosition("target") {
              argument("beacon-color", BeaconColorArgument(plugin)) {
                executes<CommandSender> { sender, context ->
                  val player = context.getPlayer("player")
                  temporaryWaypoint(
                      sender,
                      player,
                      context.getBlockPosition("target").toLocation(player.world),
                      context["beacon-color"])
                }
              }
              executes<CommandSender> { sender, context ->
                val player = context.getPlayer("player")
                temporaryWaypoint(
                    sender,
                    player,
                    context.getBlockPosition("target").toLocation(player.world),
                    null)
              }
            }
          }
        }
        literal("uuid") {
          greedyString("query") {
            suggests(WaypointsSuggestionProvider(plugin, textMode = false, allowGlobals = true))
            executes<CommandSender> { sender, context ->
              plugin.skedule {
                val result = searchWaypoints(plugin, sender, context["query"], true)
                if (result.isEmpty()) {
                  translations.COMMAND_SCRIPT_UUID_NO_MATCH.send(sender)
                } else {
                  translations.COMMAND_SCRIPT_UUID_HEADER.send(sender)
                  result.forEach {
                    sender.sendMessage(
                        translations.COMMAND_SCRIPT_UUID_RESULT.withReplacements(
                                "name" placeholder it.name,
                                "folder" placeholder (it.getFolder()?.name ?: "null"),
                            )
                            .clickEvent(
                                ClickEvent.clickEvent(
                                    ClickEvent.Action.COPY_TO_CLIPBOARD, it.id.toString())))
                  }
                }
              }
            }
          }
        }
      }

  private fun temporaryWaypoint(
      sender: CommandSender,
      player: Player,
      target: Location,
      beaconColor: BeaconColor?
  ) {
    if (target.isOutOfBounds) {
      translations.WAYPOINT_CREATE_COORDINATES_OUT_OF_BOUNDS.send(sender)
    } else {
      plugin.pointerManager.enable(player, TemporaryWaypointTrackable(plugin, target, beaconColor))
    }
  }
}
