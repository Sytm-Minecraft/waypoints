package de.md5lukas.waypoints.command.arguments

import com.mojang.brigadier.Message
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.okkero.skedule.future
import de.md5lukas.commons.containsNonWordCharacter
import de.md5lukas.waypoints.WaypointsPlugin
import de.md5lukas.waypoints.api.Waypoint
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import java.util.concurrent.CompletableFuture
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Suppress("UnstableApiUsage")
class WaypointsSuggestionProvider(
    private val plugin: WaypointsPlugin,
    private val textMode: Boolean,
    private val allowGlobals: Boolean,
    private val filter: (suspend (CommandSender, Waypoint) -> Boolean)? = null,
) : SuggestionProvider<CommandSourceStack> {

  override fun getSuggestions(
      context: CommandContext<CommandSourceStack>,
      builder: SuggestionsBuilder
  ): CompletableFuture<Suggestions> {
    val sender = (context.source as CommandSourceStack).sender

    return plugin.future {
      val query =
          if (textMode) {
            builder.remaining.removePrefix("\"").removeSuffix("\"")
          } else builder.remaining

      if (allowGlobals) {
        val publicPrefix = plugin.translations.COMMAND_SEARCH_PREFIX_PUBLIC.rawText + "/"
        val permissionPrefix = plugin.translations.COMMAND_SEARCH_PREFIX_PERMISSION.rawText + "/"
        arrayOf(publicPrefix, permissionPrefix).forEach {
          if (it.startsWith(query, true)) {
            builder.suggest(formatSuggestion(it))
          }
        }
        plugin.api.publicWaypoints.searchWaypoints(query.removePrefix(publicPrefix)).forEach {
          if (shouldDiscard(sender, it.t)) {
            return@forEach
          }
          builder.suggest(
              formatSuggestion("$publicPrefix${it.indexedName}"), it.t.getTooltip(sender))
        }
        plugin.api.permissionWaypoints
            .searchWaypoints(query.removePrefix(permissionPrefix), sender)
            .forEach {
              if (shouldDiscard(sender, it.t)) {
                return@forEach
              }
              builder.suggest(
                  formatSuggestion("$permissionPrefix${it.indexedName}"), it.t.getTooltip(sender))
            }
      }
      if (sender is Player) {
        plugin.api.getWaypointPlayer(sender.uniqueId).searchWaypoints(query).forEach {
          if (shouldDiscard(sender, it.t)) {
            return@forEach
          }
          builder.suggest(formatSuggestion(it.indexedName), it.t.getTooltip(sender))
        }
      }
      builder.build()
    }
  }

  private fun formatSuggestion(name: String) =
      if (textMode && name.containsNonWordCharacter) {
        "\"$name\""
      } else {
        name
      }

  private fun Waypoint.getTooltip(sender: CommandSender): Message =
      plugin.apiExtensions.run {
        MessageComponentSerializer.message()
            .serialize(
                plugin.translations.COMMAND_SEARCH_TOOLTIP.withReplacements(
                    *getResolvers(sender as? Player)))
      }

  private suspend fun shouldDiscard(sender: CommandSender, waypoint: Waypoint): Boolean {
    if (waypoint.location.world === null) return true
    if (sender is Player && plugin.waypointsConfig.general.hideWaypointsFromDifferentWorlds) {
      if (sender.world != waypoint.location.world) {
        return true
      }
    }
    return filter?.invoke(sender, waypoint) == false
  }
}
