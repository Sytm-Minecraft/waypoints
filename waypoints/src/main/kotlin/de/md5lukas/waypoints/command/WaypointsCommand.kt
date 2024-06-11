package de.md5lukas.waypoints.command

import com.mojang.brigadier.tree.LiteralCommandNode
import com.okkero.skedule.skedule
import de.md5lukas.commons.paper.isOutOfBounds
import de.md5lukas.commons.paper.placeholder
import de.md5lukas.paper.brigadier.arguments.*
import de.md5lukas.paper.brigadier.executors.*
import de.md5lukas.paper.brigadier.requirements.*
import de.md5lukas.paper.brigadier.suggestions.SuggestionProviders
import de.md5lukas.waypoints.WaypointsPermissions
import de.md5lukas.waypoints.WaypointsPlugin
import de.md5lukas.waypoints.command.arguments.WaypointsSuggestionProvider
import de.md5lukas.waypoints.gui.WaypointsGUI
import de.md5lukas.waypoints.pointers.TemporaryWaypointTrackable
import de.md5lukas.waypoints.pointers.WaypointTrackable
import de.md5lukas.waypoints.util.createWaypointPermission
import de.md5lukas.waypoints.util.createWaypointPrivate
import de.md5lukas.waypoints.util.createWaypointPublic
import de.md5lukas.waypoints.util.humanReadableByteCountBin
import de.md5lukas.waypoints.util.labelResolver
import de.md5lukas.waypoints.util.searchWaypoint
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class WaypointsCommand(private val plugin: WaypointsPlugin) {

  private val translations
    get() = plugin.translations

  @Suppress("UnstableApiUsage")
  fun buildCommand(): LiteralCommandNode<CommandSourceStack> =
      command("waypoints") {
        requiresPermission(WaypointsPermissions.COMMAND_PERMISSION)
        executes<CommandSender> { sender, _ -> translations.COMMAND_NOT_A_PLAYER.send(sender) }
        executes<Player> { player, _ -> WaypointsGUI(plugin, player, player.uniqueId) }
        literal("help") {
          executes<CommandSender> { sender, context ->
            val labelResolver = context.labelResolver
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
        literal("select") {
          requiresPlayer()
          greedyString("name") {
            suggests(WaypointsSuggestionProvider(plugin, textMode = false, allowGlobals = true))
            executes<Player> { player, context ->
              plugin.skedule(player) {
                val waypoint = searchWaypoint(plugin, player, context["name"], true)
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
          }
        }
        literal("deselectAll") {
          requiresPlayer()
          executes<Player> { player, _ ->
            plugin.pointerManager.disable(player) { true }
            translations.COMMAND_DESELECT_DONE.send(player)
          }
        }
        literal("teleport") {
          requires { plugin.waypointsConfig.general.features.teleportation }
          requiresPlayer()
          greedyString("name") {
            suggests(
                WaypointsSuggestionProvider(plugin, textMode = false, allowGlobals = true) {
                    sender,
                    waypoint ->
                  plugin.teleportManager.isAllowedToTeleportToWaypoint(sender as Player, waypoint)
                })
            executes<Player> { player, context ->
              plugin.skedule(player) {
                val waypoint = searchWaypoint(plugin, player, context["name"], true)
                if (waypoint == null) {
                  translations.COMMAND_SEARCH_NOT_FOUND_WAYPOINT.send(player)
                } else if (plugin.teleportManager.isAllowedToTeleportToWaypoint(player, waypoint)) {
                  plugin.teleportManager.teleportPlayerToWaypoint(player, waypoint)
                } else {
                  translations.MESSAGE_TELEPORT_NOT_ALLOWED.send(player)
                }
              }
            }
          }
        }
        literal("set") {
          requiresPlayer()
          requiresPermission(WaypointsPermissions.MODIFY_PRIVATE)
          greedyString("name") {
            executes<Player> { player, context ->
              plugin.skedule(player) { createWaypointPrivate(plugin, player, context["name"]) }
            }
          }
        }
        literal("setPublic") {
          requires { plugin.waypointsConfig.general.features.globalWaypoints }
          requiresPlayer()
          andRequires {
            val sender = it.sender

            plugin.waypointsConfig.general.features.publicOwnershipWaypoints ||
                sender.hasPermission(WaypointsPermissions.MODIFY_PUBLIC)
          }
          greedyString("name") {
            executes<Player> { player, context ->
              plugin.skedule(player) { createWaypointPublic(plugin, player, context["name"]) }
            }
          }
        }
        literal("setPermission") {
          requires { plugin.waypointsConfig.general.features.globalWaypoints }
          requiresPlayer()
          requiresPermission(WaypointsPermissions.MODIFY_PERMISSION)
          word("permission") {
            greedyString("name") {
              executes<Player> { player, context ->
                plugin.skedule(player) {
                  createWaypointPermission(plugin, player, context["name"], context["permission"])
                }
              }
            }
          }
        }
        literal("setTemporary") {
          requiresPlayer()
          requiresPermission(WaypointsPermissions.TEMPORARY_WAYPOINT)
          blockPosition("target") {
            players("players") {
              requiresPermission(WaypointsPermissions.TEMPORARY_WAYPOINT_OTHERS)
              executes<Player> { player, context ->
                setTemporary(
                    player,
                    context.getBlockPosition("target").toLocation(player.world),
                    context.getPlayers("players"))
              }
            }
            executes<Player> { player, context ->
              setTemporary(
                  player,
                  context.getBlockPosition("target").toLocation(player.world),
                  listOf(player))
            }
          }
        }
        literal("other") {
          requiresPlayer()
          requiresPermission(WaypointsPermissions.COMMAND_OTHER)
          playerProfiles("target") {
            suggests(SuggestionProviders.onlinePlayers(false))
            executes<Player> { player, context ->
              val otherUUID = context.getPlayerProfiles("target").first().id!!

              plugin.skedule(player) {
                if (plugin.api.waypointsPlayerExists(otherUUID)) {
                  WaypointsGUI(plugin, player, otherUUID)
                } else {
                  translations.COMMAND_OTHER_PLAYER_NO_WAYPOINTS.send(player)
                }
              }
            }
          }
        }
        literal("statistics") {
          requiresPermission(WaypointsPermissions.COMMAND_STATISTICS)
          executes<CommandSender> { sender, _ ->
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
        literal("reload") {
          requiresPermission(WaypointsPermissions.COMMAND_RELOAD)
          executes<CommandSender> { sender, _ ->
            plugin.reloadConfiguration()
            translations.COMMAND_RELOAD_FINISHED.send(sender)
          }
        }
      }

  private fun setTemporary(player: Player, location: Location, players: Collection<Player>) {
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
}
