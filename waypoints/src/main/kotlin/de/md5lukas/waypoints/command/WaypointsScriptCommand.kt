package de.md5lukas.waypoints.command

import com.okkero.skedule.skedule
import de.md5lukas.waypoints.WaypointsPermissions
import de.md5lukas.waypoints.WaypointsPlugin
import de.md5lukas.waypoints.pointers.BeaconColor
import de.md5lukas.waypoints.pointers.TemporaryWaypointTrackable
import de.md5lukas.waypoints.pointers.WaypointTrackable
import de.md5lukas.waypoints.util.isLocationOutOfBounds
import de.md5lukas.waypoints.util.labelResolver
import de.md5lukas.waypoints.util.placeholder
import de.md5lukas.waypoints.util.searchWaypoints
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.GreedyStringArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.*
import java.util.*
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.Location
import org.bukkit.entity.Player

class WaypointsScriptCommand(private val plugin: WaypointsPlugin) {

  private val translations
    get() = plugin.translations

  fun register() {
    commandTree("waypointsscript") {
      withPermission(WaypointsPermissions.COMMAND_SCRIPTING)
      withAliases(*plugin.waypointsConfig.general.commands.waypointsScriptAliases.toTypedArray())
      anyExecutor { sender, args ->
        val labelResolver = args.labelResolver
        translations.COMMAND_SCRIPT_HELP_HEADER.send(sender)
        translations.COMMAND_SCRIPT_HELP_DESELECT_WAYPOINT.send(sender, labelResolver)
        translations.COMMAND_SCRIPT_HELP_SELECT_WAYPOINT.send(sender, labelResolver)
        translations.COMMAND_SCRIPT_HELP_TEMPORARY_WAYPOINT.send(sender, labelResolver)
        translations.COMMAND_SCRIPT_HELP_UUID.send(sender, labelResolver)
      }
      literalArgument("deselectWaypoint") {
        playerArgument("player") {
          anyExecutor { _, args ->
            val player = args[0] as Player

            plugin.pointerManager.disable(player) { true }
          }
        }
      }
      literalArgument("selectWaypoint") {
        playerArgument("player") {
          uuidArgument("waypoint-id") {
            anyExecutor { sender, args ->
              val player = args[0] as Player
              val uuid = args[1] as UUID

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
      literalArgument("temporaryWaypoint") {
        playerArgument("player") {
          locationArgument("target") {
            optionalArgument(
                StringArgument("beaconcolor")
                    .replaceSuggestions(
                        ArgumentSuggestions.strings(
                            BeaconColor.values().map { it.name }.toList()))) {
                  anyExecutor { sender, args ->
                    val player = args[0] as Player
                    val target = args[1] as Location
                    val beaconColorString = args[2] as? String

                    val beaconColor =
                        BeaconColor.values().firstOrNull { it.name.equals(beaconColorString, true) }
                    if (beaconColorString !== null && beaconColor === null) {
                      translations.COMMAND_SCRIPT_TEMPORARY_WAYPOINT_BEACON_COLOR_NOT_FOUND.send(
                          sender)
                      return@anyExecutor
                    }

                    if (isLocationOutOfBounds(target)) {
                      translations.WAYPOINT_CREATE_COORDINATES_OUT_OF_BOUNDS.send(sender)
                    } else {
                      plugin.pointerManager.enable(
                          player, TemporaryWaypointTrackable(plugin, target, beaconColor))
                    }
                  }
                }
          }
        }
      }
      literalArgument("uuid") {
        argument(
            GreedyStringArgument("query")
                .replaceSuggestions(
                    WaypointsArgumentSuggestions(plugin, textMode = false, allowGlobals = true))) {
              anyExecutor { sender, args ->
                plugin.skedule {
                  val result = searchWaypoints(plugin, sender, args[0] as String, true)
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
  }
}
