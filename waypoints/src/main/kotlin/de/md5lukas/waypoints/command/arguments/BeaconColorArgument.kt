package de.md5lukas.waypoints.command.arguments

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import de.md5lukas.waypoints.WaypointsPlugin
import de.md5lukas.waypoints.pointers.BeaconColor
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import java.util.concurrent.CompletableFuture

@Suppress("UnstableApiUsage")
class BeaconColorArgument(private val plugin: WaypointsPlugin) :
    CustomArgumentType.Converted<BeaconColor, String> {

  private val unknownBeaconColor
    get() =
        SimpleCommandExceptionType(
            MessageComponentSerializer.message()
                .serialize(
                    plugin.translations.COMMAND_SCRIPT_TEMPORARY_WAYPOINT_BEACON_COLOR_NOT_FOUND
                        .text))

  override fun convert(string: String): BeaconColor {
    BeaconColor.entries.forEach {
      if (it.name.equals(string, ignoreCase = true)) {
        return it
      }
    }
    throw unknownBeaconColor.create()
  }

  private val nativeType = StringArgumentType.word()!!

  override fun getNativeType(): ArgumentType<String> = nativeType

  override fun <S> listSuggestions(
      context: CommandContext<S>,
      builder: SuggestionsBuilder
  ): CompletableFuture<Suggestions> {
    BeaconColor.entries.forEach {
      if (it.name.startsWith(builder.remaining, ignoreCase = true)) {
        builder.suggest(it.name)
      }
    }
    return builder.buildFuture()
  }
}
