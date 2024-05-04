package de.md5lukas.waypoints.util

import de.md5lukas.commons.paper.placeholder
import de.md5lukas.commons.paper.placeholderIgnoringArguments
import de.md5lukas.waypoints.WaypointsPlugin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Location
import org.bukkit.entity.Player

fun Location.getResolvers(plugin: WaypointsPlugin, player: Player, translatedTarget: Location) =
    arrayOf(
        "world" placeholder plugin.worldTranslations.getWorldName(world!!),
        if (player.world === translatedTarget.world) {
          "distance" placeholder player.location.distance(translatedTarget)
        } else {
          "distance" placeholderIgnoringArguments plugin.translations.TEXT_DISTANCE_OTHER_WORLD.text
        },
        "x" placeholder x,
        "y" placeholder y,
        "z" placeholder z,
        "block_x" placeholder blockX,
        "block_y" placeholder blockY,
        "block_z" placeholder blockZ,
    )

fun Component.asPlainText(): String = PlainTextComponentSerializer.plainText().serialize(this)
