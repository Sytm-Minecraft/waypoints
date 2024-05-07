package de.md5lukas.waypoints.util

import de.md5lukas.commons.paper.appendLore
import de.md5lukas.commons.paper.editMeta
import de.md5lukas.commons.paper.placeholder
import de.md5lukas.commons.paper.placeholderIgnoringArguments
import de.md5lukas.waypoints.WaypointsPermissions
import de.md5lukas.waypoints.WaypointsPlugin
import de.md5lukas.waypoints.api.*
import de.md5lukas.waypoints.api.gui.GUIDisplayable
import de.md5lukas.waypoints.api.gui.GUIFolder
import de.md5lukas.waypoints.gui.PlayerTrackingDisplayable
import de.md5lukas.waypoints.gui.SharedDisplayable
import de.md5lukas.waypoints.lang.InventoryTranslation
import kotlinx.coroutines.future.await
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class APIExtensions(private val plugin: WaypointsPlugin) {
  private val translations
    get() = plugin.translations

  private val worldTranslations
    get() = plugin.worldTranslations

  suspend fun GUIDisplayable.getItem(player: Player) =
      when (this) {
        is GUIFolder -> getItem(player)
        is Waypoint -> getItem(player)
        is PlayerTrackingDisplayable -> translations.ICON_TRACKING.item
        is SharedDisplayable -> translations.ICON_SHARED.item
        else -> throw IllegalStateException("Unknown GUIDisplayable subclass")
      }

  suspend fun Waypoint.getItem(player: Player): ItemStack {
    val stack =
        when (type) {
          Type.DEATH -> translations.WAYPOINT_ICON_DEATH
          Type.PRIVATE -> translations.WAYPOINT_ICON_PRIVATE
          Type.PUBLIC -> translations.WAYPOINT_ICON_PUBLIC
          Type.PERMISSION -> translations.WAYPOINT_ICON_PERMISSION
        }.getItem(icon?.material, *getResolvers(player))

    icon?.customModelData?.let { customModelData ->
      stack.editMeta<ItemMeta> { setCustomModelData(customModelData) }
    }

    when (type) {
      Type.DEATH -> null
      Type.PRIVATE -> translations.WAYPOINT_ICON_PRIVATE_CUSTOM_DESCRIPTION
      Type.PUBLIC -> translations.WAYPOINT_ICON_PUBLIC_CUSTOM_DESCRIPTION
      Type.PERMISSION -> translations.WAYPOINT_ICON_PERMISSION_CUSTOM_DESCRIPTION
    }?.let { stack.applyDescription(it, description) }

    val owner = this.owner
    if (plugin.waypointsConfig.general.features.publicOwnershipWaypoints && owner != null) {
      val ownerName = plugin.uuidUtils.getNameAsync(owner).await()
      if (ownerName != null) {
        stack.appendLore(
            translations.WAYPOINT_ICON_PUBLIC_OWNER.withReplacements("owner" placeholder ownerName))
      }
    }

    return stack
  }

  fun Waypoint.getResolvers(player: Player?, translatedTarget: Location = this.location) =
      arrayOf(
          "name" placeholder name,
          "description" placeholder (description ?: ""),
          "created_at" placeholder createdAt,
          "world" placeholder
              (location.world?.let { worldTranslations.getWorldName(it) }
                  ?: translations.TEXT_WORLD_NOT_FOUND.text),
          "x" placeholder location.x,
          "y" placeholder location.y,
          "z" placeholder location.z,
          "block_x" placeholder location.blockX,
          "block_y" placeholder location.blockY,
          "block_z" placeholder location.blockZ,
          if (player !== null && player.world === translatedTarget.world) {
            "distance" placeholder player.location.distance(translatedTarget)
          } else {
            "distance" placeholderIgnoringArguments translations.TEXT_DISTANCE_OTHER_WORLD.text
          },
      )

  fun Waypoint.getIconStack(): ItemStack =
      icon?.let { icon ->
        ItemStack(icon.material).also { stack ->
          icon.customModelData?.let { customModelData ->
            stack.editMeta<ItemMeta> { setCustomModelData(customModelData) }
          }
        }
      }
          ?: when (type) {
            Type.DEATH -> translations.WAYPOINT_ICON_DEATH
            Type.PRIVATE -> translations.WAYPOINT_ICON_PRIVATE
            Type.PUBLIC -> translations.WAYPOINT_ICON_PUBLIC
            Type.PERMISSION -> translations.WAYPOINT_ICON_PERMISSION
          }.rawStack

  fun Waypoint.getHologramTranslations() =
      when (type) {
        Type.PRIVATE -> plugin.translations.POINTERS_HOLOGRAM_PRIVATE
        Type.DEATH -> plugin.translations.POINTERS_HOLOGRAM_DEATH
        Type.PUBLIC -> plugin.translations.POINTERS_HOLOGRAM_PUBLIC
        Type.PERMISSION -> plugin.translations.POINTERS_HOLOGRAM_PERMISSION
      }

  suspend fun GUIFolder.getItem(player: Player) =
      when (this) {
        is WaypointHolder -> getItem(player)
        is Folder -> getItem(player)
        else -> throw IllegalStateException("Unknown GUIFolder subclass")
      }

  suspend fun WaypointHolder.getItem(player: Player): ItemStack {
    val amountVisibleToPlayer =
        if (player.hasPermission(WaypointsPermissions.MODIFY_PERMISSION)) {
          getWaypointsAmount()
        } else {
          getWaypointsVisibleForPlayer(player)
        }

    val itemStack =
        when (type) {
          Type.PUBLIC ->
              translations.ICON_PUBLIC.getItem("amount" placeholder amountVisibleToPlayer)
          Type.PERMISSION ->
              translations.ICON_PERMISSION.getItem("amount" placeholder amountVisibleToPlayer)
          else -> throw IllegalStateException("A waypoint holder for a player cannot be a GUI item")
        }

    itemStack.amountClamped = amountVisibleToPlayer

    return itemStack
  }

  suspend fun Folder.getItem(player: Player): ItemStack {
    if (type === Type.DEATH) {
      return getItemDeath()
    }

    val fetchedAmount =
        if (player.hasPermission(WaypointsPermissions.MODIFY_PERMISSION)) {
          getAmount()
        } else {
          getAmountVisibleForPlayer(player)
        }

    val stack =
        when (type) {
          Type.PRIVATE -> translations.FOLDER_ICON_PRIVATE
          Type.PUBLIC -> translations.FOLDER_ICON_PUBLIC
          Type.PERMISSION -> translations.FOLDER_ICON_PERMISSION
          else -> throw IllegalStateException("An folder with the type $type should not exist")
        }.getItem(
            icon?.material,
            "name" placeholder name,
            "description" placeholder (description ?: ""),
            "created_at" placeholder createdAt,
            "amount" placeholder fetchedAmount)

    icon?.customModelData?.let { customModelData ->
      stack.editMeta<ItemMeta> { setCustomModelData(customModelData) }
    }

    stack.amountClamped = fetchedAmount

    when (type) {
      Type.DEATH -> null
      Type.PRIVATE -> translations.FOLDER_ICON_PRIVATE_CUSTOM_DESCRIPTION
      Type.PUBLIC -> translations.FOLDER_ICON_PUBLIC_CUSTOM_DESCRIPTION
      Type.PERMISSION -> translations.FOLDER_ICON_PERMISSION_CUSTOM_DESCRIPTION
    }?.let { stack.applyDescription(it, description) }

    val owner = this.owner
    if (plugin.waypointsConfig.general.features.publicOwnershipFolders && owner != null) {
      val ownerName = plugin.uuidUtils.getNameAsync(owner).await()
      if (ownerName != null) {
        stack.appendLore(
            translations.FOLDER_ICON_PUBLIC_OWNER.withReplacements("owner" placeholder ownerName))
      }
    }

    return stack
  }

  fun Type.getBackgroundItem() =
      when (this) {
        Type.PRIVATE -> plugin.translations.BACKGROUND_PRIVATE
        Type.DEATH -> plugin.translations.BACKGROUND_DEATH
        Type.PUBLIC -> plugin.translations.BACKGROUND_PUBLIC
        Type.PERMISSION -> plugin.translations.BACKGROUND_PERMISSION
      }.item

  private suspend fun Folder.getItemDeath(): ItemStack {
    val fetchedAmount = getAmount()
    val stack = translations.FOLDER_ICON_DEATH.getItem("amount" placeholder fetchedAmount)

    stack.amount = fetchedAmount.coerceIn(1, 64)

    return stack
  }

  private fun ItemStack.applyDescription(translation: InventoryTranslation, description: String?) {
    description?.let {
      val (line1, line2, line3, line4) = it.split('\n')
      val customDescription = mutableListOf<Component>(Component.empty())
      customDescription +=
          translation.withReplacements(
              "description1" placeholder "$line1 $line2".trim(),
              "description2" placeholder "$line3 $line4".trim(),
          )
      editMeta { meta -> meta.lore((meta.lore() ?: emptyList()) + customDescription) }
    }
  }
}

fun ItemStack.toIcon(): Icon {
  val itemMeta = this.itemMeta
  return Icon(
      type,
      if (itemMeta !== null && itemMeta.hasCustomModelData()) itemMeta.customModelData else null,
  )
}
