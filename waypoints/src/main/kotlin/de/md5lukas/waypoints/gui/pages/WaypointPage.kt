package de.md5lukas.waypoints.gui.pages

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.switchContext
import com.okkero.skedule.withSynchronizationContext
import de.md5lukas.commons.paper.placeholder
import de.md5lukas.kinvs.GUIPattern
import de.md5lukas.kinvs.items.GUIItem
import de.md5lukas.signgui.SignGUI
import de.md5lukas.waypoints.WaypointsPermissions
import de.md5lukas.waypoints.api.Type
import de.md5lukas.waypoints.api.Waypoint
import de.md5lukas.waypoints.gui.WaypointsGUI
import de.md5lukas.waypoints.integrations.DynMapIntegration
import de.md5lukas.waypoints.integrations.Pl3xMapIntegration
import de.md5lukas.waypoints.integrations.SquareMapIntegration
import de.md5lukas.waypoints.pointers.BeaconColor
import de.md5lukas.waypoints.pointers.WaypointTrackable
import de.md5lukas.waypoints.util.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.wesjd.anvilgui.AnvilGUI

class WaypointPage(wpGUI: WaypointsGUI, private val waypoint: Waypoint) :
    BasePage(wpGUI, wpGUI.extendApi { waypoint.type.getBackgroundItem() }) {

  private companion object {
    /**
     * - w = Waypoint Icon
     * - g = Change icon
     * - i = Get UUID (Global waypoints only)
     * - u = Move to public folder
     * - e = Move to permission folder
     * - p = Change permission
     * - s = Select
     * - y = WebMap custom icon
     * - c = Select beacon color
     * - f = Move to folder
     * - r = rename
     * - o = Edit custom description
     * - d = Delete
     * - t = Teleport
     * - h = Share
     * - b = Back
     */
    val waypointPattern =
        GUIPattern(
            "u_p_w_y_i",
            "e_c___r__",
            "_f__s__g_",
            "__h___o__",
            "d___t___b",
        )
  }

  override val title: Component =
      wpGUI.translations.INVENTORY_TITLE_WAYPOINT.withReplacements(
          "waypoint" placeholder waypoint.name)

  private val isNotDeathWaypoint = waypoint.type !== Type.DEATH

  private val canModifyWaypoint =
      when (waypoint.type) {
        Type.PRIVATE,
        Type.DEATH ->
            wpGUI.viewerData.id == waypoint.owner ||
                wpGUI.targetData.id == waypoint.owner &&
                    wpGUI.viewer.hasPermission(WaypointsPermissions.MODIFY_OTHER)
        Type.PUBLIC ->
            wpGUI.viewer.hasPermission(WaypointsPermissions.MODIFY_PUBLIC) ||
                (wpGUI.plugin.waypointsConfig.general.features.publicOwnershipWaypoints &&
                    wpGUI.viewerData.id == waypoint.owner)
        Type.PERMISSION -> wpGUI.viewer.hasPermission(WaypointsPermissions.MODIFY_PERMISSION)
      }

  override fun update() {
    wpGUI.skedule { updatePage() }
  }

  private suspend fun updatePage(update: Boolean = true) {
    applyPattern(
        waypointPattern,
        0,
        0,
        background,
        'w' to wpGUI.extendApi { GUIItem(waypoint.getItem(wpGUI.viewer)) },
        'g' to
            if (canModifyWaypoint) {
              GUIItem(wpGUI.translations.WAYPOINT_EDIT_ICON.item) {
                val newIcon =
                    if (it.isShiftClick) {
                      null
                    } else {
                      wpGUI.viewer.inventory.itemInMainHand.toIcon()
                    }

                if (checkMaterialForCustomIcon(wpGUI.plugin, newIcon?.material)) {
                  wpGUI.skedule {
                    waypoint.setIcon(newIcon)
                    updatePage()
                  }
                  wpGUI.playSound { clickSuccess }
                } else {
                  wpGUI.playSound { clickError }
                  wpGUI.viewer.sendMessage(
                      wpGUI.translations.MESSAGE_WAYPOINT_NEW_ICON_INVALID.text
                          .appendSpace()
                          .append(getAllowedItemsForCustomIconMessage(wpGUI.plugin)))
                }
              }
            } else {
              background
            },
        'i' to
            if (wpGUI.viewer.hasPermission(WaypointsPermissions.COMMAND_SCRIPTING) &&
                isNotDeathWaypoint) {
              GUIItem(wpGUI.translations.WAYPOINT_GET_UUID.item) {
                wpGUI.playSound { clickSuccess }
                wpGUI.viewer.sendMessage(
                    wpGUI.translations.MESSAGE_WAYPOINT_GET_UUID.withReplacements(
                            "name" placeholder waypoint.name)
                        .clickEvent(
                            ClickEvent.clickEvent(
                                ClickEvent.Action.COPY_TO_CLIPBOARD, waypoint.id.toString())))
                wpGUI.viewer.closeInventory()
              }
            } else {
              background
            },
        'u' to
            if (wpGUI.plugin.waypointsConfig.general.features.globalWaypoints &&
                waypoint.type !== Type.PUBLIC &&
                isNotDeathWaypoint &&
                canModifyWaypoint &&
                wpGUI.viewer.hasPermission(WaypointsPermissions.MODIFY_PUBLIC)) {
              GUIItem(wpGUI.translations.WAYPOINT_MAKE_PUBLIC.item) {
                val nameResolver = "name" placeholder waypoint.name
                wpGUI.open(
                    ConfirmPage(
                        wpGUI,
                        wpGUI.translations.WAYPOINT_MAKE_PUBLIC_CONFIRM_QUESTION.getItem(
                            nameResolver),
                        wpGUI.translations.WAYPOINT_MAKE_PUBLIC_CONFIRM_FALSE.getItem(nameResolver),
                        wpGUI.translations.WAYPOINT_MAKE_PUBLIC_CONFIRM_TRUE.getItem(nameResolver),
                    ) {
                      if (it) {
                        wpGUI.skedule {
                          when (val result =
                              createWaypointPublic(
                                  wpGUI.plugin, wpGUI.viewer, waypoint.name, waypoint.location)) {
                            is SuccessWaypoint -> {
                              waypoint.copyFieldsTo(result.waypoint)
                              waypoint.delete()
                              wpGUI.goBack()
                              wpGUI.goBack()
                            }
                            else -> {
                              wpGUI.playSound { clickError }
                              wpGUI.goBack()
                            }
                          }
                        }
                      } else {
                        wpGUI.playSound { clickDangerAbort }
                        wpGUI.goBack()
                      }
                    })
                wpGUI.playSound { clickDanger }
              }
            } else {
              background
            },
        'e' to
            if (wpGUI.plugin.waypointsConfig.general.features.globalWaypoints &&
                waypoint.type !== Type.PERMISSION &&
                isNotDeathWaypoint &&
                canModifyWaypoint &&
                wpGUI.viewer.hasPermission(WaypointsPermissions.MODIFY_PERMISSION)) {
              GUIItem(wpGUI.translations.WAYPOINT_MAKE_PERMISSION.item) {
                val nameResolver = "name" placeholder waypoint.name
                wpGUI.open(
                    ConfirmPage(
                        wpGUI,
                        wpGUI.translations.WAYPOINT_MAKE_PERMISSION_CONFIRM_QUESTION.getItem(
                            nameResolver),
                        wpGUI.translations.WAYPOINT_MAKE_PERMISSION_CONFIRM_FALSE.getItem(
                            nameResolver),
                        wpGUI.translations.WAYPOINT_MAKE_PERMISSION_CONFIRM_TRUE.getItem(
                            nameResolver),
                    ) {
                      if (it) {
                        AnvilGUI.builder()
                            .plugin(wpGUI.plugin)
                            .text("")
                            .title(wpGUI.translations.WAYPOINT_CREATE_ENTER_PERMISSION.text)
                            .onClickSuspending(wpGUI.scheduler) {
                                slot,
                                (isOutputInvalid, permission) ->
                              if (slot != AnvilGUI.Slot.OUTPUT || isOutputInvalid)
                                  return@onClickSuspending emptyList()

                              when (val result =
                                  createWaypointPermission(
                                      wpGUI.plugin,
                                      wpGUI.viewer,
                                      waypoint.name,
                                      permission,
                                      waypoint.location)) {
                                is SuccessWaypoint -> {
                                  waypoint.copyFieldsTo(result.waypoint)
                                  waypoint.delete()
                                  wpGUI.goBack()
                                  wpGUI.goBack()
                                }
                                else -> {
                                  wpGUI.playSound { clickError }
                                  wpGUI.goBack()
                                }
                              }

                              return@onClickSuspending listOf(AnvilGUI.ResponseAction.close())
                            }
                            .onClose {
                              (wpGUI.gui.activePage as BasePage).update()
                              wpGUI.schedule { wpGUI.gui.open() }
                            }
                            .open(wpGUI.viewer)
                      } else {
                        wpGUI.playSound { clickDangerAbort }
                        wpGUI.goBack()
                      }
                    })
                wpGUI.playSound { clickDanger }
              }
            } else {
              background
            },
        'p' to
            if (waypoint.type === Type.PERMISSION && canModifyWaypoint) {
              GUIItem(
                  wpGUI.translations.WAYPOINT_EDIT_PERMISSION.getItem(
                      "permission" placeholder (waypoint.permission ?: ""))) {
                    AnvilGUI.builder()
                        .plugin(wpGUI.plugin)
                        .text(waypoint.permission ?: "")
                        .title(wpGUI.translations.WAYPOINT_EDIT_ENTER_PERMISSION.text)
                        .onClickSuspending(wpGUI.scheduler) { slot, (isOutputInvalid, permission) ->
                          if (slot != AnvilGUI.Slot.OUTPUT || isOutputInvalid)
                              return@onClickSuspending emptyList()

                          waypoint.setPermission(permission)
                          wpGUI.playSound { clickSuccess }
                          return@onClickSuspending listOf(AnvilGUI.ResponseAction.close())
                        }
                        .onClose {
                          wpGUI.skedule {
                            updatePage()
                            switchContext(SynchronizationContext.SYNC)
                            wpGUI.gui.open()
                          }
                        }
                        .open(wpGUI.viewer)
                    wpGUI.playSound { clickNormal }
                  }
            } else {
              background
            },
        's' to
            if (waypoint.location.world !== null) {
              if (wpGUI.plugin.pointerManager.getCurrentTargets(wpGUI.viewer).any {
                (it as? WaypointTrackable)?.waypoint == waypoint
              }) {
                GUIItem(wpGUI.translations.WAYPOINT_DESELECT.item) {
                  wpGUI.plugin.pointerManager.disable(wpGUI.viewer) {
                    WaypointTrackable.Extract(it) == waypoint
                  }
                  wpGUI.playSound { clickSuccess }
                  wpGUI.skedule { updatePage() }
                }
              } else {
                GUIItem(wpGUI.translations.WAYPOINT_SELECT.item) {
                  wpGUI.viewer.closeInventory()
                  wpGUI.plugin.pointerManager.enable(
                      wpGUI.viewer, WaypointTrackable(wpGUI.plugin, waypoint))
                  wpGUI.playSound { waypointSelected }
                }
              }
            } else {
              background
            },
        'y' to
            if (canModifyWaypoint && waypoint.type === Type.PUBLIC) {
              when {
                wpGUI.plugin.dynMapIntegrationAvailable -> {
                  createChangeCustomMapIconItem(
                      DynMapIntegration.CUSTOM_DATA_KEY,
                      wpGUI.plugin.waypointsConfig.integrations.dynmap.icon)
                }
                wpGUI.plugin.squareMapIntegrationAvailable -> {
                  createChangeCustomMapIconItem(
                      SquareMapIntegration.CUSTOM_DATA_KEY,
                      wpGUI.plugin.waypointsConfig.integrations.squaremap.icon)
                }
                wpGUI.plugin.pl3xMapIntegrationAvailable -> {
                  createChangeCustomMapIconItem(
                      Pl3xMapIntegration.CUSTOM_DATA_KEY,
                      wpGUI.plugin.waypointsConfig.integrations.pl3xmap.icon)
                }
                else -> {
                  background
                }
              }
            } else {
              background
            },
        'c' to
            if (canModifyWaypoint &&
                isNotDeathWaypoint &&
                wpGUI.plugin.waypointsConfig.pointers.beacon.enabled) {
              GUIItem(
                  wpGUI.translations.WAYPOINT_SELECT_BEACON_COLOR.getItem(
                      waypoint.beaconColor
                          ?: (wpGUI.plugin.waypointsConfig.pointers.beacon.getDefaultColor(
                                  waypoint.type) ?: BeaconColor.CLEAR)
                              .material)) {
                    wpGUI.open(SelectBeaconColorPage(wpGUI, waypoint))
                    wpGUI.playSound { clickNormal }
                  }
            } else {
              background
            },
        'f' to
            if (canModifyWaypoint &&
                isNotDeathWaypoint &&
                (waypoint.type != Type.PUBLIC ||
                    wpGUI.viewer.hasPermission(WaypointsPermissions.MODIFY_PUBLIC) ||
                    wpGUI.plugin.waypointsConfig.general.features.publicOwnershipFolders)) {
              GUIItem(wpGUI.translations.WAYPOINT_MOVE_TO_FOLDER.item) {
                wpGUI.skedule {
                  val page = MoveToFolderPage(wpGUI, waypoint).apply { init() }
                  switchContext(SynchronizationContext.SYNC)
                  wpGUI.open(page)
                  wpGUI.playSound { clickNormal }
                }
              }
            } else {
              background
            },
        'r' to
            if (canModifyWaypoint && isNotDeathWaypoint) {
              GUIItem(wpGUI.translations.WAYPOINT_RENAME.item) {
                wpGUI.viewer.closeInventory()
                AnvilGUI.builder()
                    .plugin(wpGUI.plugin)
                    .text(waypoint.name)
                    .title(wpGUI.translations.WAYPOINT_EDIT_ENTER_NAME.text)
                    .onClickSuspending(wpGUI.scheduler) { slot, (isOutputInvalid, newName) ->
                      if (slot != AnvilGUI.Slot.OUTPUT || isOutputInvalid)
                          return@onClickSuspending emptyList()

                      val holder = wpGUI.getHolderForType(waypoint.type)

                      if (checkWaypointName(wpGUI.plugin, holder, newName)) {
                        waypoint.setName(newName)

                        wpGUI.playSound { clickSuccess }
                        updatePage()
                      } else {
                        when (waypoint.type) {
                          Type.PRIVATE -> wpGUI.translations.WAYPOINT_NAME_DUPLICATE_PRIVATE
                          Type.PUBLIC -> wpGUI.translations.WAYPOINT_NAME_DUPLICATE_PUBLIC
                          Type.PERMISSION -> wpGUI.translations.WAYPOINT_NAME_DUPLICATE_PERMISSION
                          else ->
                              throw IllegalArgumentException(
                                  "Waypoints of the type ${waypoint.type} have no name")
                        }.send(wpGUI.viewer)
                        wpGUI.playSound { clickError }
                        return@onClickSuspending listOf(replaceInputText(newName))
                      }

                      return@onClickSuspending listOf(AnvilGUI.ResponseAction.close())
                    }
                    .onClose { wpGUI.schedule { wpGUI.gui.open() } }
                    .open(wpGUI.viewer)
                wpGUI.playSound { clickNormal }
              }
            } else {
              background
            },
        'o' to
            if (canModifyWaypoint &&
                isNotDeathWaypoint &&
                minecraftVersionAtLeast(wpGUI.plugin, 20, 1) &&
                wpGUI.plugin.server.pluginManager.isPluginEnabled("ProtocolLib")) {
              GUIItem(wpGUI.translations.WAYPOINT_EDIT_DESCRIPTION.item) {
                wpGUI.viewer.closeInventory()
                val builder =
                    SignGUI.newBuilder().plugin(wpGUI.plugin).player(wpGUI.viewer).onClose { lines
                      ->
                      wpGUI.skedule {
                        if (lines.all(String::isBlank)) {
                          waypoint.setDescription(null)
                        } else {
                          waypoint.setDescription(lines.joinToString("\n"))
                        }
                        updatePage()
                        switchContext(SynchronizationContext.SYNC)
                        wpGUI.playSound { clickSuccess }
                        wpGUI.gui.open()
                      }
                    }
                waypoint.description?.let { description -> builder.lines(description.split('\n')) }
                wpGUI.playSound { clickNormal }
                builder.open()
              }
            } else {
              background
            },
        'd' to
            if (canModifyWaypoint) {
              GUIItem(wpGUI.translations.WAYPOINT_DELETE.item) {
                val nameResolver = "name" placeholder waypoint.name
                wpGUI.open(
                    ConfirmPage(
                        wpGUI,
                        wpGUI.translations.WAYPOINT_DELETE_CONFIRM_QUESTION.getItem(nameResolver),
                        wpGUI.translations.WAYPOINT_DELETE_CONFIRM_FALSE.getItem(nameResolver),
                        wpGUI.translations.WAYPOINT_DELETE_CONFIRM_TRUE.getItem(nameResolver),
                    ) {
                      if (it) {
                        wpGUI.skedule {
                          waypoint.delete()
                          switchContext(SynchronizationContext.SYNC)
                          wpGUI.goBack()
                          wpGUI.goBack()
                          wpGUI.playSound { clickSuccess }
                        }
                      } else {
                        wpGUI.goBack()
                        wpGUI.playSound { clickDangerAbort }
                      }
                    })
                wpGUI.playSound { clickDanger }
              }
            } else {
              background
            },
        't' to
            if (wpGUI.plugin.waypointsConfig.general.features.teleportation &&
                (wpGUI.viewer.hasPermission(
                    wpGUI.plugin.teleportManager.getTeleportPermission(waypoint)) ||
                    wpGUI.plugin.teleportManager.isTeleportEnabled(wpGUI.targetData, waypoint)) &&
                waypoint.location.world !== null) {
              GUIItem(
                  wpGUI.translations.WAYPOINT_TELEPORT.getItem().also { stack ->
                    val currentLore = stack.lore() ?: mutableListOf()
                    wpGUI.plugin.teleportManager
                        .getTeleportCostDescription(wpGUI.viewer, waypoint)
                        ?.let { currentLore += it }
                    if (!wpGUI.plugin.teleportManager.isAllowedToTeleportToWaypoint(
                        wpGUI.viewer, waypoint)) {
                      currentLore += wpGUI.translations.WAYPOINT_TELEPORT_MUST_VISIT.text
                    }
                    stack.lore(currentLore)
                  }) {
                    wpGUI.skedule {
                      if (wpGUI.plugin.teleportManager.isAllowedToTeleportToWaypoint(
                          wpGUI.viewer, waypoint)) {
                        wpGUI.playSound { clickNormal }
                        withSynchronizationContext(SynchronizationContext.SYNC) {
                          wpGUI.viewer.closeInventory()
                        }
                        wpGUI.plugin.teleportManager.teleportPlayerToWaypoint(
                            wpGUI.viewer, waypoint)
                      } else {
                        wpGUI.playSound { clickError }
                      }
                    }
                  }
            } else {
              background
            },
        'h' to
            if (canModifyWaypoint && waypoint.type === Type.PRIVATE) {
              GUIItem(wpGUI.translations.WAYPOINT_SHARE.item) {
                wpGUI.skedule {
                  val page = ShareWaypointPage(wpGUI, waypoint).apply { init() }
                  switchContext(SynchronizationContext.SYNC)
                  wpGUI.open(page)
                }
                wpGUI.playSound { clickNormal }
              }
            } else {
              background
            },
        'b' to
            GUIItem(wpGUI.translations.GENERAL_BACK.item) {
              wpGUI.playSound { clickNormal }
              wpGUI.goBack()
            },
    )

    if (update) {
      withSynchronizationContext(SynchronizationContext.SYNC) { wpGUI.gui.update() }
    }
  }

  private fun createChangeCustomMapIconItem(customDataKey: String, defaultIcon: String) =
      GUIItem(wpGUI.translations.WAYPOINT_CHANGE_MAP_ICON.item) {
        wpGUI.skedule {
          val builder =
              AnvilGUI.builder()
                  .plugin(wpGUI.plugin)
                  .text(waypoint.getCustomData(customDataKey) ?: defaultIcon)
                  .title(wpGUI.translations.WAYPOINT_EDIT_ENTER_WEB_MAP_ICON.text)
                  .onClickSuspending(wpGUI.scheduler) { slot, (isOutputInvalid, newIcon) ->
                    if (slot != AnvilGUI.Slot.OUTPUT || isOutputInvalid)
                        return@onClickSuspending emptyList()

                    waypoint.setCustomData(customDataKey, newIcon.ifBlank { null })
                    wpGUI.playSound { clickSuccess }
                    return@onClickSuspending listOf(AnvilGUI.ResponseAction.close())
                  }
                  .onClose { wpGUI.schedule { wpGUI.gui.open() } }
          switchContext(SynchronizationContext.SYNC)
          wpGUI.viewer.closeInventory()
          wpGUI.playSound { clickNormal }
          builder.open(wpGUI.viewer)
        }
      }

  suspend fun init() {
    updatePage(false)
  }
}
