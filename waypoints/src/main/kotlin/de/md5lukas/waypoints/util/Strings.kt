package de.md5lukas.waypoints.util

import net.md_5.bungee.api.ChatColor
import java.util.regex.Matcher
import java.util.regex.Pattern

private val minecraftUsernamePattern = Pattern.compile("^\\w{3,16}$")
private val aotVariablePattern = Pattern.compile("\\$\\[(\\w+)]")
private val runtimeVariablePattern = Pattern.compile("\\$\\{(\\w+)}")

fun String.translateColorCodes(): String = ChatColor.translateAlternateColorCodes('&', this)

fun String.aotReplace(map: Map<String, String>): String {
    return replace(aotVariablePattern, map)
}

fun String.runtimeReplace(map: Map<String, String>): String {
    return replace(runtimeVariablePattern, map)
}

fun String.isMinecraftUsername() = minecraftUsernamePattern.matcher(this).matches()

private fun String.replace(pattern: Pattern, map: Map<String, String>): String {
    return pattern.matcher(this).replaceAll {
        Matcher.quoteReplacement(map[it.group(1)] ?: it.group(0))
    }
}