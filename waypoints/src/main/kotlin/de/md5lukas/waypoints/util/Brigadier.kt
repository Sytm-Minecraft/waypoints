package de.md5lukas.waypoints.util

import com.mojang.brigadier.context.CommandContext
import de.md5lukas.commons.paper.placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

val CommandContext<*>.labelResolver: TagResolver
  get() = "label" placeholder input.substringBefore(' ').removePrefix("/")
