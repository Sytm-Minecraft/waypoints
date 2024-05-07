package de.md5lukas.waypoints.gui.pages

import de.md5lukas.commons.collections.PaginationList
import de.md5lukas.kinvs.GUIPattern
import de.md5lukas.kinvs.items.GUIItem
import de.md5lukas.waypoints.WaypointsPermissions
import de.md5lukas.waypoints.api.Folder
import de.md5lukas.waypoints.api.Type
import de.md5lukas.waypoints.api.Waypoint
import de.md5lukas.waypoints.gui.WaypointsGUI

class MoveToFolderPage(wpGUI: WaypointsGUI, private val waypoint: Waypoint) :
    ListingPage<Folder>(wpGUI, wpGUI.extendApi { waypoint.type.getBackgroundItem() }) {

  override suspend fun getContent() =
      PaginationList<Folder>(PAGINATION_LIST_PAGE_SIZE).also {
        it.addAll(wpGUI.getHolderForType(waypoint.type).getFolders())

        val viewerId = wpGUI.viewerData.id
        if (wpGUI.plugin.waypointsConfig.general.features.publicOwnershipFolders &&
            waypoint.type == Type.PUBLIC &&
            viewerId == waypoint.owner &&
            !wpGUI.viewer.hasPermission(WaypointsPermissions.MODIFY_PUBLIC)) {
          it.retainAll { folder -> viewerId == folder.owner }
        }
      }

  override suspend fun toGUIContent(value: Folder) =
      wpGUI.extendApi {
        GUIItem(value.getItem(wpGUI.viewer)) {
          wpGUI.playSound { clickSuccess }
          wpGUI.skedule {
            waypoint.setFolder(value)
            wpGUI.goBack()
          }
        }
      }

  private companion object {
    /** p = Previous g = No folder b = Back n = Next */
    val controlsPattern = GUIPattern("p__g_b__n")
  }

  private fun updateControls() {
    applyPattern(
        controlsPattern,
        4,
        0,
        background,
        'p' to
            GUIItem(wpGUI.translations.GENERAL_PREVIOUS.item) {
              wpGUI.playSound { clickNormal }
              previousPage()
            },
        'n' to
            GUIItem(wpGUI.translations.GENERAL_NEXT.item) {
              wpGUI.playSound { clickNormal }
              nextPage()
            },
        'g' to
            GUIItem(wpGUI.translations.SELECT_FOLDER_NO_FOLDER.item) {
              wpGUI.playSound { clickSuccess }
              wpGUI.skedule {
                waypoint.setFolder(null)
                wpGUI.goBack()
              }
            },
        'b' to
            GUIItem(wpGUI.translations.GENERAL_BACK.item) {
              wpGUI.playSound { clickNormal }
              wpGUI.goBack()
            })
  }

  override suspend fun init() {
    super.init()
    updateListingInInventory()
    updateControls()
  }
}
