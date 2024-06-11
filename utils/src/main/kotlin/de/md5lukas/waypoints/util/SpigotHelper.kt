package de.md5lukas.waypoints.util

import de.md5lukas.commons.paper.editMeta
import java.net.URI
import java.util.*
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.plugin.Plugin

fun parseLocationString(player: Player, input: String): Location? {
  val parts = input.split(' ')

  if (parts.size != 3) {
    return null
  }

  return try {
    Location(player.world, parts[0].toDouble(), parts[1].toDouble(), parts[2].toDouble())
  } catch (_: NumberFormatException) {
    null
  }
}

var ItemStack.amountClamped: Int
  get() = amount
  set(value) {
    amount = value.coerceIn(1, maxStackSize)
  }

private lateinit var parsedVersion: IntArray

fun minecraftVersionAtLeast(plugin: Plugin, minor: Int, patch: Int = 0): Boolean {
  if (!::parsedVersion.isInitialized) {
    parsedVersion = IntArray(3)
    plugin.server.minecraftVersion.split('.').forEachIndexed { index, s ->
      parsedVersion[index] = s.toInt()
    }
  }
  return parsedVersion[1] >= minor && parsedVersion[2] >= patch
}

fun createCustomPlayerHead(plugin: Plugin, textureId: String): ItemStack {
  val profile = plugin.server.createProfile(UUID.randomUUID(), "CUSTOM_HEAD")

  profile.setTextures(
      profile.textures.also {
        it.skin = URI.create("https://textures.minecraft.net/texture/$textureId").toURL()
      })

  val stack = ItemStack(Material.PLAYER_HEAD)
  stack.editMeta<SkullMeta> { playerProfile = profile }
  return stack
}
