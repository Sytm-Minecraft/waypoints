package de.md5lukas.waypoints.lang

import de.md5lukas.waypoints.api.OverviewSort
import de.md5lukas.waypoints.pointers.BeaconColor
import de.md5lukas.waypoints.pointers.variants.PointerVariant
import org.bukkit.Material

@Suppress("PropertyName")
class Translations(tl: TranslationLoader) {
  val PREFIX = Translation(tl, "prefix")
  val SCRIPT_PREFIX = Translation(tl, "scriptPrefix")

  val UPDATE_COULD_NOT_CHECK = Translation(tl, "update.error")
  val UPDATE_USING_LATEST_VERSION = Translation(tl, "update.upToDate")
  val UPDATE_NEW_VERSION_AVAILABLE = Translation(tl, "update.outdated")

  val COMMAND_NOT_A_PLAYER = Translation(tl, "command.notAPlayer", PREFIX)

  val COMMAND_SCRIPT_HELP_HEADER = Translation(tl, "command.script.help.header", SCRIPT_PREFIX)
  val COMMAND_SCRIPT_HELP_DESELECT_WAYPOINT =
      Translation(tl, "command.script.help.deselectWaypoint")
  val COMMAND_SCRIPT_HELP_SELECT_WAYPOINT = Translation(tl, "command.script.help.selectWaypoint")
  val COMMAND_SCRIPT_HELP_TEMPORARY_WAYPOINT =
      Translation(tl, "command.script.help.temporaryWaypoint")
  val COMMAND_SCRIPT_HELP_UUID = Translation(tl, "command.script.help.uuid")

  val COMMAND_SCRIPT_SELECT_WAYPOINT_WAYPOINT_NOT_FOUND =
      Translation(tl, "command.script.selectWaypoint.waypointNotFound", SCRIPT_PREFIX)

  val COMMAND_SCRIPT_TEMPORARY_WAYPOINT_BEACON_COLOR_NOT_FOUND =
      Translation(tl, "command.script.temporaryWaypoint.beaconColorNotFound", SCRIPT_PREFIX)

  val COMMAND_SCRIPT_UUID_NO_MATCH = Translation(tl, "command.script.uuid.noMatch", SCRIPT_PREFIX)
  val COMMAND_SCRIPT_UUID_HEADER = Translation(tl, "command.script.uuid.header", SCRIPT_PREFIX)
  val COMMAND_SCRIPT_UUID_RESULT = Translation(tl, "command.script.uuid.result")

  val COMMAND_HELP_HEADER = Translation(tl, "command.help.header", PREFIX)
  val COMMAND_HELP_GUI = Translation(tl, "command.help.gui")
  val COMMAND_HELP_HELP = Translation(tl, "command.help.help")
  val COMMAND_HELP_SELECT = Translation(tl, "command.help.select")
  val COMMAND_HELP_DESELECT = Translation(tl, "command.help.deselect")
  val COMMAND_HELP_TELEPORT = Translation(tl, "command.help.teleport")
  val COMMAND_HELP_SET_PRIVATE = Translation(tl, "command.help.set.private")
  val COMMAND_HELP_SET_PUBLIC = Translation(tl, "command.help.set.public")
  val COMMAND_HELP_SET_PERMISSION = Translation(tl, "command.help.set.permission")
  val COMMAND_HELP_SET_TEMPORARY = Translation(tl, "command.help.set.temporary")
  val COMMAND_HELP_OTHER = Translation(tl, "command.help.other")
  val COMMAND_HELP_STATISTICS = Translation(tl, "command.help.statistics")
  val COMMAND_HELP_RELOAD = Translation(tl, "command.help.reload")

  val COMMAND_SEARCH_PREFIX_PUBLIC = Translation(tl, "command.search.prefix.public")
  val COMMAND_SEARCH_PREFIX_PERMISSION = Translation(tl, "command.search.prefix.permission")
  val COMMAND_SEARCH_TOOLTIP = Translation(tl, "command.search.tooltip")
  val COMMAND_SEARCH_NOT_FOUND_WAYPOINT =
      Translation(tl, "command.search.notFound.waypoint", PREFIX)

  val COMMAND_SELECT_SELECTED = Translation(tl, "command.select.selected", PREFIX)

  val COMMAND_DESELECT_DONE = Translation(tl, "command.deselect.done", PREFIX)

  val COMMAND_OTHER_PLAYER_NO_WAYPOINTS = Translation(tl, "command.other.playerNoWaypoints", PREFIX)

  val COMMAND_STATISTICS_MESSAGE = Translation(tl, "command.statistics.message", PREFIX)

  val COMMAND_RELOAD_FINISHED = Translation(tl, "command.reload.finished", PREFIX)

  val POINTERS_ACTION_BAR_WRONG_WORLD = Translation(tl, "pointers.actionBar.wrongWorld")

  val POINTERS_ACTION_BAR_DISTANCE = Translation(tl, "pointers.actionBar.distance")

  val POINTERS_HOLOGRAM_PRIVATE = Translation(tl, "pointers.hologram.private")
  val POINTERS_HOLOGRAM_DEATH = Translation(tl, "pointers.hologram.death")
  val POINTERS_HOLOGRAM_PUBLIC = Translation(tl, "pointers.hologram.public")
  val POINTERS_HOLOGRAM_PERMISSION = Translation(tl, "pointers.hologram.permission")
  val POINTERS_HOLOGRAM_TEMPORARY = Translation(tl, "pointers.hologram.temporary")
  val POINTERS_HOLOGRAM_PLAYER_TRACKING = Translation(tl, "pointers.hologram.playerTracking")

  val TEXT_DURATION_SECOND = Translation(tl, "text.duration.second")
  val TEXT_DURATION_SECONDS = Translation(tl, "text.duration.seconds")
  val TEXT_DURATION_MINUTE = Translation(tl, "text.duration.minute")
  val TEXT_DURATION_MINUTES = Translation(tl, "text.duration.minutes")
  val TEXT_DURATION_HOUR = Translation(tl, "text.duration.hour")
  val TEXT_DURATION_HOURS = Translation(tl, "text.duration.hours")
  val TEXT_DURATION_DAY = Translation(tl, "text.duration.day")
  val TEXT_DURATION_DAYS = Translation(tl, "text.duration.days")

  val TEXT_WORLD_NOT_FOUND = Translation(tl, "text.worldNotFound")

  val TEXT_DISTANCE_OTHER_WORLD = Translation(tl, "text.distance.otherWorld")

  val TEXT_BEACON_COLORS =
      BeaconColor.entries.map {
        it to
            Translation(
                tl, "text.beaconColors.${it.name.lowercase()}", miniMessage = tl.itemMiniMessage)
      }

  val WAYPOINT_CREATE_WORLD_UNAVAILABLE =
      Translation(tl, "message.waypoint.create.worldUnavailable", PREFIX)
  val WAYPOINT_CREATE_COORDINATES_OUT_OF_BOUNDS =
      Translation(tl, "message.waypoint.create.coordinates.outOfBounds", PREFIX)
  val WAYPOINT_CREATE_COORDINATES_INVALID_FORMAT =
      Translation(tl, "message.waypoint.create.coordinates.invalidFormat", PREFIX)

  val WAYPOINT_LIMIT_REACHED_PRIVATE =
      Translation(tl, "message.waypoint.limitReached.private", PREFIX)
  val WAYPOINT_NAME_DUPLICATE_PRIVATE =
      Translation(tl, "message.waypoint.nameDuplicate.private", PREFIX)
  val WAYPOINT_SET_SUCCESS_PRIVATE = Translation(tl, "message.waypoint.setSuccess.private", PREFIX)

  val WAYPOINT_LIMIT_REACHED_PUBLIC =
      Translation(tl, "message.waypoint.limitReached.public", PREFIX)
  val WAYPOINT_NAME_DUPLICATE_PUBLIC =
      Translation(tl, "message.waypoint.nameDuplicate.public", PREFIX)
  val WAYPOINT_SET_SUCCESS_PUBLIC = Translation(tl, "message.waypoint.setSuccess.public", PREFIX)

  val WAYPOINT_NAME_DUPLICATE_PERMISSION =
      Translation(tl, "message.waypoint.nameDuplicate.permission", PREFIX)
  val WAYPOINT_SET_SUCCESS_PERMISSION =
      Translation(tl, "message.waypoint.setSuccess.permission", PREFIX)

  val MESSAGE_WAYPOINT_GET_UUID = Translation(tl, "message.waypoint.getUuid", PREFIX)
  val MESSAGE_WAYPOINT_NEW_ICON_INVALID = Translation(tl, "message.waypoint.newIconInvalid", PREFIX)

  val FOLDER_LIMIT_REACHED_PRIVATE = Translation(tl, "message.folder.limitReached.private", PREFIX)
  val FOLDER_NAME_DUPLICATE_PRIVATE =
      Translation(tl, "message.folder.nameDuplicate.private", PREFIX)
  val FOLDER_CREATE_SUCCESS_PRIVATE =
      Translation(tl, "message.folder.createSuccess.private", PREFIX)
  val FOLDER_NEW_ICON_INVALID = Translation(tl, "message.folder.newIconInvalid", PREFIX)

  val FOLDER_LIMIT_REACHED_PUBLIC = Translation(tl, "message.folder.limitReached.public", PREFIX)
  val FOLDER_NAME_DUPLICATE_PUBLIC = Translation(tl, "message.folder.nameDuplicate.public", PREFIX)
  val FOLDER_CREATE_SUCCESS_PUBLIC = Translation(tl, "message.folder.createSuccess.public", PREFIX)

  val FOLDER_NAME_DUPLICATE_PERMISSION =
      Translation(tl, "message.folder.nameDuplicate.permission", PREFIX)
  val FOLDER_CREATE_SUCCESS_PERMISSION =
      Translation(tl, "message.folder.createSuccess.permission", PREFIX)

  val MESSAGE_ALLOWED_ICONS_WHITELIST = Translation(tl, "message.allowedIcons.whitelist")
  val MESSAGE_ALLOWED_ICONS_BLACKLIST = Translation(tl, "message.allowedIcons.blacklist")

  val MESSAGE_TEMPORARY_WAYPOINTS_BLOCKED =
      Translation(tl, "message.temporaryWaypoints.blocked", PREFIX)

  val MESSAGE_TELEPORT_ON_COOLDOWN = Translation(tl, "message.teleport.onCooldown", PREFIX)
  val MESSAGE_TELEPORT_NOT_ALLOWED = Translation(tl, "message.teleport.notAllowed", PREFIX)
  val MESSAGE_TELEPORT_NOT_ENOUGH_XP = Translation(tl, "message.teleport.notEnough.xp", PREFIX)
  val MESSAGE_TELEPORT_NOT_ENOUGH_XP_POINTS =
      Translation(tl, "message.teleport.notEnough.xpPoints", PREFIX)
  val MESSAGE_TELEPORT_NOT_ENOUGH_BALANCE =
      Translation(tl, "message.teleport.notEnough.balance", PREFIX)
  val MESSAGE_TELEPORT_STAND_STILL_NOTICE =
      Translation(tl, "message.teleport.standStill.notice", PREFIX)
  val MESSAGE_TELEPORT_STAND_STILL_MOVED =
      Translation(tl, "message.teleport.standStill.moved", PREFIX)

  val MESSAGE_TRACKING_PLAYER_NO_LONGER_ONLINE =
      Translation(tl, "message.tracking.playerNoLongerOnline", PREFIX)
  val MESSAGE_TRACKING_TRACKABLE_REQUIRED =
      Translation(tl, "message.tracking.trackableRequired", PREFIX)
  val MESSAGE_TRACKING_REQUEST_SENT = Translation(tl, "message.tracking.request.sent", PREFIX)
  val MESSAGE_TRACKING_REQUEST_REQUEST = Translation(tl, "message.tracking.request.request", PREFIX)
  val MESSAGE_TRACKING_REQUEST_GEYSER_TITLE =
      Translation(tl, "message.tracking.request.geyser.title")
  val MESSAGE_TRACKING_REQUEST_GEYSER_MESSAGE =
      Translation(tl, "message.tracking.request.geyser.message")
  val MESSAGE_TRACKING_REQUEST_GEYSER_ACCEPT =
      Translation(tl, "message.tracking.request.geyser.accept")
  val MESSAGE_TRACKING_REQUEST_GEYSER_DECLINE =
      Translation(tl, "message.tracking.request.geyser.decline")
  val MESSAGE_TRACKING_NOTIFICATION = Translation(tl, "message.tracking.notification", PREFIX)

  val MESSAGE_SHARING_ALREADY_SHARED = Translation(tl, "message.sharing.alreadyShared", PREFIX)
  val MESSAGE_SHARING_SUCCESS = Translation(tl, "message.sharing.success", PREFIX)

  val INVENTORY_TITLE_SELF = Translation(tl, "inventory.title.self")
  val INVENTORY_TITLE_OTHER = Translation(tl, "inventory.title.other")
  val INVENTORY_TITLE_PUBLIC = Translation(tl, "inventory.title.public")
  val INVENTORY_TITLE_PERMISSION = Translation(tl, "inventory.title.permission")
  val INVENTORY_TITLE_FOLDER = Translation(tl, "inventory.title.folder")

  val INVENTORY_TITLE_SETTINGS = Translation(tl, "inventory.title.settings")
  val INVENTORY_TITLE_WAYPOINT = Translation(tl, "inventory.title.waypoint")
  val INVENTORY_TITLE_PLAYER_TRACKING = Translation(tl, "inventory.title.playerTracking")
  val INVENTORY_TITLE_SHARE = Translation(tl, "inventory.title.sharing.selectPlayer")
  val INVENTORY_TITLE_SHARED = Translation(tl, "inventory.title.sharing.sharedWaypoints")
  val INVENTORY_TITLE_SHARING = Translation(tl, "inventory.title.sharing.sharedWith")
  val INVENTORY_TITLE_CONFIRM = Translation(tl, "inventory.title.confirm")
  val INVENTORY_TITLE_SELECT_FOLDER = Translation(tl, "inventory.title.selectFolder")
  val INVENTORY_TITLE_SELECT_BEACON_COLOR = Translation(tl, "inventory.title.selectBeaconColor")

  val GENERAL_PREVIOUS = ItemTranslation(tl, "inventory.general.previous")
  val GENERAL_NEXT = ItemTranslation(tl, "inventory.general.next")
  val GENERAL_BACK = ItemTranslation(tl, "inventory.general.back")

  val BACKGROUND_PRIVATE = ItemTranslation(tl, "inventory.background.private")
  val BACKGROUND_DEATH = ItemTranslation(tl, "inventory.background.death")
  val BACKGROUND_PUBLIC = ItemTranslation(tl, "inventory.background.public")
  val BACKGROUND_PERMISSION = ItemTranslation(tl, "inventory.background.permission")

  val OVERVIEW_CYCLE_SORT = ItemTranslation(tl, "inventory.overview.cycleSort")
  val OVERVIEW_CYCLE_SORT_ACTIVE_COLOR =
      Translation(tl, "inventory.overview.cycleSort.activeColor", miniMessage = tl.itemMiniMessage)
  val OVERVIEW_CYCLE_SORT_INACTIVE_COLOR =
      Translation(
          tl, "inventory.overview.cycleSort.inactiveColor", miniMessage = tl.itemMiniMessage)
  val OVERVIEW_CYCLE_SORT_OPTIONS =
      OverviewSort.entries.map { it to Translation(tl, "text.sortOptions.${it.name.lowercase()}") }
  val OVERVIEW_SETTINGS = ItemTranslation(tl, "inventory.overview.settings")
  val OVERVIEW_DESELECT = ItemTranslation(tl, "inventory.overview.deselect")
  val OVERVIEW_DESELECT_SELECTED = InventoryTranslation(tl, "inventory.overview.deselect.selected")
  val OVERVIEW_DESELECT_NAMES_WAYPOINT_PRIVATE =
      Translation(
          tl,
          "inventory.overview.deselect.names.waypoint.private",
          miniMessage = tl.itemMiniMessage)
  val OVERVIEW_DESELECT_NAMES_WAYPOINT_DEATH =
      Translation(
          tl, "inventory.overview.deselect.names.waypoint.death", miniMessage = tl.itemMiniMessage)
  val OVERVIEW_DESELECT_NAMES_WAYPOINT_PUBLIC =
      Translation(
          tl, "inventory.overview.deselect.names.waypoint.public", miniMessage = tl.itemMiniMessage)
  val OVERVIEW_DESELECT_NAMES_WAYPOINT_PERMISSION =
      Translation(
          tl,
          "inventory.overview.deselect.names.waypoint.permission",
          miniMessage = tl.itemMiniMessage)
  val OVERVIEW_DESELECT_NAMES_WAYPOINT_TEMPORARY =
      Translation(
          tl,
          "inventory.overview.deselect.names.waypoint.temporary",
          miniMessage = tl.itemMiniMessage)
  val OVERVIEW_DESELECT_NAMES_PLAYER_TRACKING =
      Translation(
          tl, "inventory.overview.deselect.names.playerTracking", miniMessage = tl.itemMiniMessage)
  val OVERVIEW_DESELECT_NAMES_UNKNOWN =
      Translation(tl, "inventory.overview.deselect.names.unknown", miniMessage = tl.itemMiniMessage)
  val OVERVIEW_SET_WAYPOINT = ItemTranslation(tl, "inventory.overview.setWaypoint")
  val OVERVIEW_CREATE_FOLDER = ItemTranslation(tl, "inventory.overview.createFolder")

  val SETTINGS_TOGGLE_GLOBALS_VISIBLE =
      ItemTranslation(tl, "inventory.settings.toggleGlobals.visible")
  val SETTINGS_TOGGLE_GLOBALS_HIDDEN =
      ItemTranslation(tl, "inventory.settings.toggleGlobals.hidden")
  val SETTINGS_TEMPORARY_WAYPOINTS_RECEIVABLE =
      ItemTranslation(tl, "inventory.settings.temporaryWaypoints.receivable")
  val SETTINGS_TEMPORARY_WAYPOINTS_BLOCKED =
      ItemTranslation(tl, "inventory.settings.temporaryWaypoints.blocked")
  val SETTINGS_POINTERS_TITLE = ItemTranslation(tl, "inventory.settings.pointers.title")
  // "on" and "off" in the keys are also interpreted as booleans by yaml. whyyy
  val SETTINGS_POINTERS_ON = ItemTranslation(tl, "inventory.settings.pointers.true")
  val SETTINGS_POINTERS_OFF = ItemTranslation(tl, "inventory.settings.pointers.false")

  val SETTINGS_POINTERS_NAMES: Map<PointerVariant, Translation> =
      mutableMapOf<PointerVariant, Translation>().also { map ->
        map.putAll(
            PointerVariant.entries.map {
              it to Translation(tl, "inventory.settings.pointers.${it.key}.name")
            })
      }
  val SETTINGS_POINTERS_DESCRIPTIONS: Map<PointerVariant, InventoryTranslation> =
      mutableMapOf<PointerVariant, InventoryTranslation>().also { map ->
        map.putAll(
            PointerVariant.entries.map {
              it to InventoryTranslation(tl, "inventory.settings.pointers.${it.key}.description")
            })
      }

  val ICON_PUBLIC = ItemTranslation(tl, "inventory.listing.public")
  val ICON_PERMISSION = ItemTranslation(tl, "inventory.listing.permission")
  val ICON_TRACKING = ItemTranslation(tl, "inventory.listing.tracking")
  val ICON_SHARED = ItemTranslation(tl, "inventory.listing.shared")

  val WAYPOINT_ICON_PRIVATE = ItemTranslation(tl, "inventory.waypoint.icon.private")
  val WAYPOINT_ICON_PRIVATE_CUSTOM_DESCRIPTION =
      InventoryTranslation(tl, "inventory.waypoint.icon.private.customDescription")
  val WAYPOINT_ICON_DEATH = ItemTranslation(tl, "inventory.waypoint.icon.death")
  val WAYPOINT_ICON_PUBLIC = ItemTranslation(tl, "inventory.waypoint.icon.public")
  val WAYPOINT_ICON_PUBLIC_CUSTOM_DESCRIPTION =
      InventoryTranslation(tl, "inventory.waypoint.icon.public.customDescription")
  val WAYPOINT_ICON_PUBLIC_OWNER = InventoryTranslation(tl, "inventory.waypoint.icon.public.owner")
  val WAYPOINT_ICON_PERMISSION = ItemTranslation(tl, "inventory.waypoint.icon.permission")
  val WAYPOINT_ICON_PERMISSION_CUSTOM_DESCRIPTION =
      InventoryTranslation(tl, "inventory.waypoint.icon.permission.customDescription")

  val WAYPOINT_SELECT = ItemTranslation(tl, "inventory.waypoint.select")
  val WAYPOINT_DESELECT = ItemTranslation(tl, "inventory.waypoint.deselect")
  val WAYPOINT_DELETE = ItemTranslation(tl, "inventory.waypoint.delete", true)
  val WAYPOINT_DELETE_CONFIRM_QUESTION = ItemTranslation(tl, "inventory.waypoint.delete.question")
  val WAYPOINT_DELETE_CONFIRM_TRUE = ItemTranslation(tl, "inventory.waypoint.delete.confirm")
  val WAYPOINT_DELETE_CONFIRM_FALSE = ItemTranslation(tl, "inventory.waypoint.delete.cancel")
  val WAYPOINT_RENAME = ItemTranslation(tl, "inventory.waypoint.rename")
  val WAYPOINT_EDIT_ICON = ItemTranslation(tl, "inventory.waypoint.editIcon")
  val WAYPOINT_EDIT_DESCRIPTION = ItemTranslation(tl, "inventory.waypoint.editDescription")
  val WAYPOINT_MOVE_TO_FOLDER = ItemTranslation(tl, "inventory.waypoint.moveToFolder")
  val WAYPOINT_TELEPORT = ItemTranslation(tl, "inventory.waypoint.teleport")
  val WAYPOINT_TELEPORT_XP_LEVEL = InventoryTranslation(tl, "inventory.waypoint.teleport.xpLevel")
  val WAYPOINT_TELEPORT_XP_POINTS = InventoryTranslation(tl, "inventory.waypoint.teleport.xpPoints")
  val WAYPOINT_TELEPORT_BALANCE = InventoryTranslation(tl, "inventory.waypoint.teleport.balance")
  val WAYPOINT_TELEPORT_MUST_VISIT =
      InventoryTranslation(tl, "inventory.waypoint.teleport.mustVisit")
  val WAYPOINT_SELECT_BEACON_COLOR = ItemTranslation(tl, "inventory.waypoint.selectBeaconColor")
  val WAYPOINT_GET_UUID = ItemTranslation(tl, "inventory.waypoint.getUuid")
  val WAYPOINT_SHARE = ItemTranslation(tl, "inventory.waypoint.share")
  val WAYPOINT_EDIT_PERMISSION = ItemTranslation(tl, "inventory.waypoint.editPermission")
  val WAYPOINT_MAKE_PUBLIC = ItemTranslation(tl, "inventory.waypoint.make.public", true)
  val WAYPOINT_MAKE_PUBLIC_CONFIRM_QUESTION =
      ItemTranslation(tl, "inventory.waypoint.make.public.question")
  val WAYPOINT_MAKE_PUBLIC_CONFIRM_TRUE =
      ItemTranslation(tl, "inventory.waypoint.make.public.confirm")
  val WAYPOINT_MAKE_PUBLIC_CONFIRM_FALSE =
      ItemTranslation(tl, "inventory.waypoint.make.public.cancel")
  val WAYPOINT_MAKE_PERMISSION = ItemTranslation(tl, "inventory.waypoint.make.permission", true)
  val WAYPOINT_MAKE_PERMISSION_CONFIRM_QUESTION =
      ItemTranslation(tl, "inventory.waypoint.make.permission.question")
  val WAYPOINT_MAKE_PERMISSION_CONFIRM_TRUE =
      ItemTranslation(tl, "inventory.waypoint.make.permission.confirm")
  val WAYPOINT_MAKE_PERMISSION_CONFIRM_FALSE =
      ItemTranslation(tl, "inventory.waypoint.make.permission.cancel")
  val WAYPOINT_CHANGE_MAP_ICON = ItemTranslation(tl, "inventory.waypoint.changeMapIcon")

  val FOLDER_ICON_PRIVATE = ItemTranslation(tl, "inventory.folder.icon.private")
  val FOLDER_ICON_PRIVATE_CUSTOM_DESCRIPTION =
      InventoryTranslation(tl, "inventory.folder.icon.private.customDescription")
  val FOLDER_ICON_DEATH = ItemTranslation(tl, "inventory.folder.icon.death")
  val FOLDER_ICON_PUBLIC = ItemTranslation(tl, "inventory.folder.icon.public")
  val FOLDER_ICON_PUBLIC_CUSTOM_DESCRIPTION =
      InventoryTranslation(tl, "inventory.folder.icon.public.customDescription")
  val FOLDER_ICON_PUBLIC_OWNER = InventoryTranslation(tl, "inventory.folder.icon.public.owner")
  val FOLDER_ICON_PERMISSION = ItemTranslation(tl, "inventory.folder.icon.permission")
  val FOLDER_ICON_PERMISSION_CUSTOM_DESCRIPTION =
      InventoryTranslation(tl, "inventory.folder.icon.permission.customDescription")

  val FOLDER_DELETE = ItemTranslation(tl, "inventory.folder.delete", true)
  val FOLDER_DELETE_CONFIRM_QUESTION = ItemTranslation(tl, "inventory.folder.delete.question")
  val FOLDER_DELETE_CONFIRM_TRUE = ItemTranslation(tl, "inventory.folder.delete.confirm")
  val FOLDER_DELETE_CONFIRM_FALSE = ItemTranslation(tl, "inventory.folder.delete.cancel")
  val FOLDER_DELETE_DEATH_NAME = Translation(tl, "inventory.folder.delete.deathName")
  val FOLDER_RENAME = ItemTranslation(tl, "inventory.folder.rename")
  val FOLDER_EDIT_DESCRIPTION = ItemTranslation(tl, "inventory.folder.editDescription")

  val SELECT_FOLDER_NO_FOLDER = ItemTranslation(tl, "inventory.selectFolder.noFolder")

  val SELECT_BEACON_COLOR_MOVE_LEFT = ItemTranslation(tl, "inventory.selectBeaconColor.moveLeft")
  val SELECT_BEACON_COLOR_MOVE_RIGHT = ItemTranslation(tl, "inventory.selectBeaconColor.moveRight")

  val WAYPOINT_CREATE_ENTER_NAME = Translation(tl, "inventory.waypoint.create.enterName")
  val WAYPOINT_CREATE_ENTER_COORDINATES =
      Translation(tl, "inventory.waypoint.create.enterCoordinates")
  val WAYPOINT_CREATE_ENTER_PERMISSION =
      Translation(tl, "inventory.waypoint.create.enterPermission")
  val WAYPOINT_EDIT_ENTER_NAME = Translation(tl, "inventory.waypoint.edit.enterName")
  val WAYPOINT_EDIT_ENTER_PERMISSION = Translation(tl, "inventory.waypoint.edit.enterPermission")
  val WAYPOINT_EDIT_ENTER_WEB_MAP_ICON = Translation(tl, "inventory.waypoint.edit.enterWebMapIcon")

  val FOLDER_CREATE_ENTER_NAME = Translation(tl, "inventory.folder.create.enterName")
  val FOLDER_EDIT_ENTER_NAME = Translation(tl, "inventory.folder.edit.enterName")

  val CONFIRM_BACKGROUND = ItemTranslation(tl, "inventory.confirm.background")

  val INTEGRATIONS_MAPS_LABEL = Translation(tl, "integrations.maps.label")

  val PLAYER_LIST_REFRESH_LISTING = ItemTranslation(tl, "inventory.playerList.refresh")

  val TRACKING_BACKGROUND = ItemTranslation(tl, "inventory.tracking.background")
  val TRACKING_TRACKABLE_ENABLED = ItemTranslation(tl, "inventory.tracking.trackable.enabled")
  val TRACKING_TRACKABLE_DISABLED = ItemTranslation(tl, "inventory.tracking.trackable.disabled")
  val TRACKING_PLAYER =
      ItemTranslation(tl, "inventory.tracking.player", fixedMaterial = Material.PLAYER_HEAD)

  val SHARING_PLAYER_SELECT =
      ItemTranslation(tl, "inventory.sharing.player.select", fixedMaterial = Material.PLAYER_HEAD)
  val SHARING_PLAYER_DELETE =
      ItemTranslation(tl, "inventory.sharing.player.delete", fixedMaterial = Material.PLAYER_HEAD)
  val SHARING_VIEW_SHARING = ItemTranslation(tl, "inventory.sharing.viewSharing")
  val SHARING_SHARED_BY = InventoryTranslation(tl, "inventory.sharing.sharedBy")
  val SHARING_UNKNOWN_PLAYER = Translation(tl, "inventory.sharing.unknownPlayer")
}
