package de.md5lukas.waypoints.util

import de.md5lukas.commons.paper.isOutOfBounds
import de.md5lukas.commons.paper.placeholder
import de.md5lukas.commons.paper.textComponent
import de.md5lukas.waypoints.WaypointsPermissions
import de.md5lukas.waypoints.WaypointsPlugin
import de.md5lukas.waypoints.api.*
import de.md5lukas.waypoints.config.general.FilterType
import de.md5lukas.waypoints.config.general.LimitConfiguration
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.permissions.Permissible

sealed class CreateResult

object LimitReached : CreateResult()

object NameTaken : CreateResult()

object LocationOutOfBounds : CreateResult()

object WorldUnavailable : CreateResult()

class SuccessWaypoint(val waypoint: Waypoint) : CreateResult()

class SuccessFolder(val folder: Folder) : CreateResult()

fun checkMaterialForCustomIcon(plugin: WaypointsPlugin, material: Material?): Boolean {
  if (material === null) {
    return true
  }
  if (material === Material.AIR) {
    return false
  }

  val filter = plugin.waypointsConfig.general.customIconFilter
  return when (filter.type) {
    FilterType.WHITELIST -> material in filter.materials
    FilterType.BLACKLIST -> material !in filter.materials
  }
}

fun getAllowedItemsForCustomIconMessage(plugin: WaypointsPlugin): Component {
  val filter = plugin.waypointsConfig.general.customIconFilter

  val materialsComponent = textComponent {
    if (filter.type === FilterType.BLACKLIST) {
      append(Component.translatable(Material.AIR))
      append(Component.text(", "))
    }
    filter.materials.forEachIndexed { index, material ->
      if (index > 0) {
        append(Component.text(", "))
      }
      append(Component.translatable(material))
    }
  }

  return when (filter.type) {
    FilterType.WHITELIST -> plugin.translations.MESSAGE_ALLOWED_ICONS_WHITELIST
    FilterType.BLACKLIST -> plugin.translations.MESSAGE_ALLOWED_ICONS_BLACKLIST
  }.withReplacements("items" placeholder materialsComponent)
}

fun checkWorldAvailability(plugin: WaypointsPlugin, world: World): Boolean {
  val config = plugin.waypointsConfig.general.availableWorlds
  return when (config.type) {
    FilterType.WHITELIST -> world.name.lowercase() in config.worlds
    FilterType.BLACKLIST -> world.name.lowercase() !in config.worlds
  }
}

suspend fun createWaypointPrivate(
    plugin: WaypointsPlugin,
    player: Player,
    name: String,
    location: Location = player.location
): CreateResult {
  creationPreChecks(plugin, player, location)?.let {
    return it
  }

  val waypointsPlayer = plugin.api.getWaypointPlayer(player.uniqueId)

  if (limitReached(
      player,
      waypointsPlayer.getWaypointsAmount(),
      WaypointsPermissions.LIMIT_PREFIX_WAYPOINTS,
      plugin.waypointsConfig.general.waypoints,
      false)) {
    plugin.translations.WAYPOINT_LIMIT_REACHED_PRIVATE.send(player)
    return LimitReached
  }
  if (!checkWaypointName(plugin, waypointsPlayer, name)) {
    plugin.translations.WAYPOINT_NAME_DUPLICATE_PRIVATE.send(player)
    return NameTaken
  }

  val waypoint = waypointsPlayer.createWaypoint(name, location)
  plugin.translations.WAYPOINT_SET_SUCCESS_PRIVATE.send(player)

  checkVisited(plugin, waypoint, player)

  player.playSound(plugin.waypointsConfig.sounds.waypointCreated)

  return SuccessWaypoint(waypoint)
}

suspend fun createWaypointPublic(
    plugin: WaypointsPlugin,
    player: Player,
    name: String,
    location: Location = player.location
): CreateResult {
  creationPreChecks(plugin, player, location)?.let {
    return it
  }

  if (limitReached(
      player,
      plugin.api.publicWaypoints.getWaypointsAmount(player.uniqueId),
      WaypointsPermissions.LIMIT_PREFIX_PUBLIC_WAYPOINTS,
      plugin.waypointsConfig.general.waypoints,
      true)) {
    plugin.translations.WAYPOINT_LIMIT_REACHED_PUBLIC.send(player)
    return LimitReached
  }

  if (!checkWaypointName(plugin, plugin.api.publicWaypoints, name)) {
    plugin.translations.WAYPOINT_NAME_DUPLICATE_PUBLIC.send(player)
    return NameTaken
  }

  val waypoint = plugin.api.publicWaypoints.createWaypoint(name, location, player.uniqueId)
  plugin.translations.WAYPOINT_SET_SUCCESS_PUBLIC.send(player)

  checkVisited(plugin, waypoint, player)

  player.playSound(plugin.waypointsConfig.sounds.waypointCreated)

  return SuccessWaypoint(waypoint)
}

suspend fun createWaypointPermission(
    plugin: WaypointsPlugin,
    player: Player,
    name: String,
    permission: String,
    location: Location = player.location
): CreateResult {
  creationPreChecks(plugin, player, location)?.let {
    return it
  }

  if (!checkWaypointName(plugin, plugin.api.permissionWaypoints, name)) {
    plugin.translations.WAYPOINT_NAME_DUPLICATE_PERMISSION.send(player)
    return NameTaken
  }

  val waypoint =
      plugin.api.permissionWaypoints.createWaypoint(name, location).also {
        it.setPermission(permission)
      }
  plugin.translations.WAYPOINT_SET_SUCCESS_PERMISSION.send(player)

  checkVisited(plugin, waypoint, player)

  player.playSound(plugin.waypointsConfig.sounds.waypointCreated)

  return SuccessWaypoint(waypoint)
}

private fun creationPreChecks(
    plugin: WaypointsPlugin,
    player: Player,
    location: Location
): CreateResult? {
  if (!player.hasPermission(WaypointsPermissions.MODIFY_ANYWHERE) &&
      !checkWorldAvailability(plugin, location.world!!)) {
    plugin.translations.WAYPOINT_CREATE_WORLD_UNAVAILABLE.send(player)
    return WorldUnavailable
  }

  if (location.isOutOfBounds) {
    plugin.translations.WAYPOINT_CREATE_COORDINATES_OUT_OF_BOUNDS.send(player)
    return LocationOutOfBounds
  }
  return null
}

private suspend fun checkVisited(plugin: WaypointsPlugin, waypoint: Waypoint, player: Player) {
  if (player.world === waypoint.location.world &&
      player.location.distanceSquared(waypoint.location) <=
          plugin.waypointsConfig.general.teleport.visitedRadiusSquared) {
    waypoint.getWaypointMeta(player.uniqueId).setVisited(true)
  }
}

suspend fun createFolderPrivate(
    plugin: WaypointsPlugin,
    player: Player,
    name: String
): CreateResult {
  val waypointsPlayer = plugin.api.getWaypointPlayer(player.uniqueId)

  if (limitReached(
      player,
      waypointsPlayer.getFoldersAmount(),
      WaypointsPermissions.LIMIT_PREFIX_FOLDERS,
      plugin.waypointsConfig.general.folders,
      false)) {
    plugin.translations.FOLDER_LIMIT_REACHED_PRIVATE.send(player)
    return LimitReached
  }
  if (!checkFolderName(plugin, waypointsPlayer, name)) {
    plugin.translations.FOLDER_NAME_DUPLICATE_PRIVATE.send(player)
    return NameTaken
  }

  val folder = waypointsPlayer.createFolder(name)
  plugin.translations.FOLDER_CREATE_SUCCESS_PRIVATE.send(player)

  return SuccessFolder(folder)
}

suspend fun createFolderPublic(
    plugin: WaypointsPlugin,
    player: Player,
    name: String
): CreateResult {
  if (!checkFolderName(plugin, plugin.api.publicWaypoints, name)) {
    plugin.translations.FOLDER_NAME_DUPLICATE_PUBLIC.send(player)
    return NameTaken
  }

  if (limitReached(
      player,
      plugin.api.publicWaypoints.getFoldersAmount(player.uniqueId),
      WaypointsPermissions.LIMIT_PREFIX_PUBLIC_FOLDERS,
      plugin.waypointsConfig.general.folders,
      true)) {
    plugin.translations.FOLDER_LIMIT_REACHED_PUBLIC.send(player)
    return LimitReached
  }

  val folder = plugin.api.publicWaypoints.createFolder(name, player.uniqueId)
  plugin.translations.FOLDER_CREATE_SUCCESS_PUBLIC.send(player)

  return SuccessFolder(folder)
}

suspend fun createFolderPermission(
    plugin: WaypointsPlugin,
    player: Player,
    name: String
): CreateResult {
  if (!checkFolderName(plugin, plugin.api.permissionWaypoints, name)) {
    plugin.translations.FOLDER_NAME_DUPLICATE_PERMISSION.send(player)
    return NameTaken
  }

  val folder = plugin.api.permissionWaypoints.createFolder(name)
  plugin.translations.FOLDER_CREATE_SUCCESS_PERMISSION.send(player)

  return SuccessFolder(folder)
}

suspend fun checkWaypointName(
    plugin: WaypointsPlugin,
    holder: WaypointHolder,
    name: String
): Boolean {
  if (when (holder.type) {
    Type.PRIVATE -> plugin.waypointsConfig.general.waypoints.allowDuplicateNamesPrivate
    Type.PUBLIC -> plugin.waypointsConfig.general.waypoints.allowDuplicateNamesPublic
    Type.PERMISSION -> plugin.waypointsConfig.general.waypoints.allowDuplicateNamesPermission
    else -> throw IllegalArgumentException("Waypoints of the type ${holder.type} have no name")
  }) {
    return true
  }

  return !holder.isDuplicateWaypointName(name)
}

suspend fun checkFolderName(
    plugin: WaypointsPlugin,
    holder: WaypointHolder,
    name: String
): Boolean {
  if (when (holder.type) {
    Type.PRIVATE -> plugin.waypointsConfig.general.folders.allowDuplicateNamesPrivate
    Type.PUBLIC -> plugin.waypointsConfig.general.folders.allowDuplicateNamesPublic
    Type.PERMISSION -> plugin.waypointsConfig.general.folders.allowDuplicateNamesPermission
    else -> throw IllegalArgumentException("Folders of the type ${holder.type} have no name")
  }) {
    return true
  }

  return !holder.isDuplicateFolderName(name)
}

private suspend fun searchWaypoints0(
    plugin: WaypointsPlugin,
    sender: CommandSender,
    query: String,
    allowGlobals: Boolean
): List<SearchResult<out Waypoint>> {
  val publicPrefix = plugin.translations.COMMAND_SEARCH_PREFIX_PUBLIC.rawText + "/"
  val permissionPrefix = plugin.translations.COMMAND_SEARCH_PREFIX_PERMISSION.rawText + "/"

  val (strippedQuery, holder) =
      if (allowGlobals && query.startsWith(publicPrefix)) {
        query.removePrefix(publicPrefix) to plugin.api.publicWaypoints
      } else if (allowGlobals && query.startsWith(permissionPrefix)) {
        query.removePrefix(permissionPrefix) to plugin.api.permissionWaypoints
      } else if (sender is Player) {
        query to plugin.api.getWaypointPlayer(sender.uniqueId)
      } else {
        return emptyList()
      }

  return holder.searchWaypoints(strippedQuery, sender)
}

suspend fun searchWaypoints(
    plugin: WaypointsPlugin,
    sender: CommandSender,
    query: String,
    allowGlobals: Boolean
): List<Waypoint> = searchWaypoints0(plugin, sender, query, allowGlobals).map { it.t }.toList()

suspend fun searchWaypoint(
    plugin: WaypointsPlugin,
    sender: CommandSender,
    query: String,
    allowGlobals: Boolean
): Waypoint? = searchWaypoints0(plugin, sender, query, allowGlobals).firstOrNull()?.t

suspend fun Waypoint.copyFieldsTo(waypoint: Waypoint) {
  waypoint.setDescription(this.description)
  waypoint.setIcon(this.icon)
  waypoint.setBeaconColor(this.beaconColor)
}

private fun limitReached(
    permissible: Permissible,
    currentAmount: Int,
    permissionPrefix: String,
    limitConfiguration: LimitConfiguration,
    public: Boolean,
): Boolean {
  val limit = if (public) limitConfiguration.publicLimit else limitConfiguration.limit
  val limits =
      if (public) limitConfiguration.publicPermissionLimits else limitConfiguration.permissionLimits
  val permission =
      if (public) WaypointsPermissions.MODIFY_PUBLIC else WaypointsPermissions.UNLIMITED

  if (limit == 0 || currentAmount < limit || permissible.hasPermission(permission)) return false

  limits.forEach {
    if (permissible.hasPermission(permissionPrefix + it) && currentAmount < it) {
      return false
    }
  }

  return true
}
