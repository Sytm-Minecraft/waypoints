package de.md5lukas.waypoints.command

import com.okkero.skedule.skedule
import de.md5lukas.commons.paper.isOutOfBounds
import de.md5lukas.commons.paper.placeholder
import de.md5lukas.waypoints.WaypointsPermissions
import de.md5lukas.waypoints.WaypointsPlugin
import de.md5lukas.waypoints.gui.WaypointsGUI
import de.md5lukas.waypoints.pointers.TemporaryWaypointTrackable
import de.md5lukas.waypoints.pointers.WaypointTrackable
import de.md5lukas.waypoints.util.createWaypointPermission
import de.md5lukas.waypoints.util.createWaypointPrivate
import de.md5lukas.waypoints.util.createWaypointPublic
import de.md5lukas.waypoints.util.humanReadableByteCountBin
import de.md5lukas.waypoints.util.labelResolver
import de.md5lukas.waypoints.util.searchWaypoint
import dev.jorel.commandapi.arguments.GreedyStringArgument
import dev.jorel.commandapi.arguments.LocationType
import dev.jorel.commandapi.kotlindsl.*
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

class WaypointsCommand(private val plugin: WaypointsPlugin) {

  private val translations
    get() = plugin.translations

  fun register() {
    commandTree("waypoints") {
      withPermission(WaypointsPermissions.COMMAND_PERMISSION)
      withAliases(*plugin.waypointsConfig.general.commands.waypointsAliases.toTypedArray())
      playerExecutor { player, _ -> WaypointsGUI(plugin, player, player.uniqueId) }
      anyExecutor { sender, _ -> translations.COMMAND_NOT_A_PLAYER.send(sender) }
      literalArgument("help") {
        anyExecutor { sender, args ->
          val labelResolver = args.labelResolver
          translations.COMMAND_HELP_HEADER.send(sender)

          if (sender is Player) translations.COMMAND_HELP_GUI.send(sender, labelResolver)

          translations.COMMAND_HELP_HELP.send(sender, labelResolver)

          if (sender is Player) {
            translations.COMMAND_HELP_SELECT.send(sender, labelResolver)
            translations.COMMAND_HELP_DESELECT.send(sender, labelResolver)
            translations.COMMAND_HELP_TELEPORT.send(sender, labelResolver)
            if (sender.hasPermission(WaypointsPermissions.MODIFY_PRIVATE)) {
              translations.COMMAND_HELP_SET_PRIVATE.send(sender, labelResolver)
            }
            if (plugin.waypointsConfig.general.features.globalWaypoints) {
              if (sender.hasPermission(WaypointsPermissions.MODIFY_PUBLIC)) {
                translations.COMMAND_HELP_SET_PUBLIC.send(sender, labelResolver)
              }
              if (sender.hasPermission(WaypointsPermissions.MODIFY_PERMISSION)) {
                translations.COMMAND_HELP_SET_PERMISSION.send(sender, labelResolver)
              }
            }
            if (sender.hasPermission(WaypointsPermissions.TEMPORARY_WAYPOINT)) {
              translations.COMMAND_HELP_SET_TEMPORARY.send(sender, labelResolver)
            }
            if (sender.hasPermission(WaypointsPermissions.COMMAND_OTHER)) {
              translations.COMMAND_HELP_OTHER.send(sender, labelResolver)
            }
          }
          if (sender.hasPermission(WaypointsPermissions.COMMAND_STATISTICS)) {
            translations.COMMAND_HELP_STATISTICS.send(sender, labelResolver)
          }
          if (sender.hasPermission(WaypointsPermissions.COMMAND_RELOAD)) {
            translations.COMMAND_HELP_RELOAD.send(sender, labelResolver)
          }
        }
      }
      literalArgument("select") {
        argument(
            GreedyStringArgument("name")
                .replaceSuggestions(
                    WaypointsArgumentSuggestions(plugin, textMode = false, allowGlobals = true))) {
              playerExecutor { player, args ->
                plugin.skedule(player) {
                  val waypoint = searchWaypoint(plugin, player, args["name"] as String, true)
                  if (waypoint == null) {
                    translations.COMMAND_SEARCH_NOT_FOUND_WAYPOINT.send(player)
                  } else {
                    plugin.pointerManager.enable(player, WaypointTrackable(plugin, waypoint))
                    translations.COMMAND_SELECT_SELECTED.send(
                        player, Placeholder.unparsed("name", waypoint.name))
                    player.playSound(plugin.waypointsConfig.sounds.waypointSelected)
                  }
                }
              }
              anyExecutor { sender, _ -> translations.COMMAND_NOT_A_PLAYER.send(sender) }
            }
      }
      literalArgument("deselectAll") {
        playerExecutor { player, _ ->
          plugin.pointerManager.disable(player) { true }
          translations.COMMAND_DESELECT_DONE.send(player)
        }
        anyExecutor { sender, _ -> translations.COMMAND_NOT_A_PLAYER.send(sender) }
      }
      if (plugin.waypointsConfig.general.features.teleportation) {
        literalArgument("teleport") {
          argument(
              GreedyStringArgument("name")
                  .replaceSuggestions(
                      WaypointsArgumentSuggestions(plugin, textMode = false, allowGlobals = true) {
                          sender,
                          waypoint ->
                        if (sender !is Player) {
                          false
                        } else {
                          plugin.teleportManager.isAllowedToTeleportToWaypoint(sender, waypoint)
                        }
                      })) {
                playerExecutor { player, args ->
                  plugin.skedule(player) {
                    val waypoint = searchWaypoint(plugin, player, args["name"] as String, true)
                    if (waypoint == null) {
                      translations.COMMAND_SEARCH_NOT_FOUND_WAYPOINT.send(player)
                    } else if (plugin.teleportManager.isAllowedToTeleportToWaypoint(
                        player, waypoint)) {
                      plugin.teleportManager.teleportPlayerToWaypoint(player, waypoint)
                    } else {
                      translations.MESSAGE_TELEPORT_NOT_ALLOWED.send(player)
                    }
                  }
                }
                anyExecutor { sender, _ -> translations.COMMAND_NOT_A_PLAYER.send(sender) }
              }
        }
      }
      literalArgument("set") {
        withPermission(WaypointsPermissions.MODIFY_PRIVATE)
        greedyStringArgument("name") {
          playerExecutor { player, args ->
            val name = args["name"] as String

            plugin.skedule(player) { createWaypointPrivate(plugin, player, name) }
          }
          anyExecutor { sender, _ -> translations.COMMAND_NOT_A_PLAYER.send(sender) }
        }
      }
      if (plugin.waypointsConfig.general.features.globalWaypoints) {
        literalArgument("setPublic") {
          withRequirement {
            plugin.waypointsConfig.general.features.publicOwnershipWaypoints ||
                it.hasPermission(WaypointsPermissions.MODIFY_PUBLIC)
          }
          greedyStringArgument("name") {
            playerExecutor { player, args ->
              val name = args["name"] as String

              plugin.skedule(player) { createWaypointPublic(plugin, player, name) }
            }
            anyExecutor { sender, _ -> translations.COMMAND_NOT_A_PLAYER.send(sender) }
          }
        }
        literalArgument("setPermission") {
          withPermission(WaypointsPermissions.MODIFY_PERMISSION)
          stringArgument("permission") {
            greedyStringArgument("name") {
              playerExecutor { player, args ->
                val permission = args["permission"] as String
                val name = args["name"] as String

                plugin.skedule(player) {
                  createWaypointPermission(plugin, player, name, permission)
                }
              }
              anyExecutor { sender, _ -> translations.COMMAND_NOT_A_PLAYER.send(sender) }
            }
          }
        }
      }
      literalArgument("setTemporary") {
        withPermission(WaypointsPermissions.TEMPORARY_WAYPOINT)
        locationArgument("target", LocationType.BLOCK_POSITION) {
          entitySelectorArgumentManyPlayers("players", optional = true) {
            withPermission(WaypointsPermissions.TEMPORARY_WAYPOINT_OTHERS)
            playerExecutor { player, args ->
              val location = args["target"] as Location
              @Suppress("UNCHECKED_CAST")
              val players = args["players"] as? Collection<Player> ?: listOf(player)

              if (location.isOutOfBounds) {
                translations.WAYPOINT_CREATE_COORDINATES_OUT_OF_BOUNDS.send(player)
              } else {
                plugin.skedule {
                  players.forEach {
                    if (it == player ||
                        plugin.api.getWaypointPlayer(it.uniqueId).canReceiveTemporaryWaypoints) {
                      plugin.pointerManager.enable(it, TemporaryWaypointTrackable(plugin, location))
                    } else {
                      translations.MESSAGE_TEMPORARY_WAYPOINTS_BLOCKED.send(
                          player, "name" placeholder it.displayName())
                    }
                  }
                }
              }
            }
            anyExecutor { sender, _ -> translations.COMMAND_NOT_A_PLAYER.send(sender) }
          }
        }
      }
      literalArgument("other") {
        withPermission(WaypointsPermissions.COMMAND_OTHER)
        offlinePlayerArgument("target") {
          playerExecutor { player, args ->
            val otherUUID = (args["target"] as OfflinePlayer).uniqueId

            plugin.skedule(player) {
              if (!plugin.api.waypointsPlayerExists(otherUUID)) {
                translations.COMMAND_OTHER_PLAYER_NO_WAYPOINTS.send(player)
              } else {
                WaypointsGUI(plugin, player, otherUUID)
              }
            }
          }
          anyExecutor { sender, _ -> translations.COMMAND_NOT_A_PLAYER.send(sender) }
        }
      }
      literalArgument("statistics") {
        withPermission(WaypointsPermissions.COMMAND_STATISTICS)
        anyExecutor { sender, _ ->
          with(plugin.api.statistics) {
            translations.COMMAND_STATISTICS_MESSAGE.send(
                sender,
                "db_file_size" placeholder databaseSize.humanReadableByteCountBin(),
                "total_waypoints" placeholder totalWaypoints,
                "private_waypoints" placeholder privateWaypoints,
                "death_waypoints" placeholder deathWaypoints,
                "public_waypoints" placeholder publicWaypoints,
                "permission_waypoints" placeholder permissionWaypoints,
                "total_folders" placeholder totalFolders,
                "private_folders" placeholder privateFolders,
                "public_folders" placeholder publicFolders,
                "permission_folders" placeholder permissionFolders,
            )
          }
        }
      }
      literalArgument("reload") {
        withPermission(WaypointsPermissions.COMMAND_RELOAD)
        anyExecutor { sender, _ ->
          plugin.reloadConfiguration()
          translations.COMMAND_RELOAD_FINISHED.send(sender)
        }
      }
    }
  }
}
