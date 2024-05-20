package de.md5lukas.waypoints.gui.pages

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.switchContext
import de.md5lukas.commons.collections.PaginationList
import de.md5lukas.commons.paper.appendLore
import de.md5lukas.commons.paper.placeholder
import de.md5lukas.kinvs.GUIPattern
import de.md5lukas.kinvs.items.GUIContent
import de.md5lukas.kinvs.items.GUIItem
import de.md5lukas.waypoints.api.OverviewSort
import de.md5lukas.waypoints.api.Type
import de.md5lukas.waypoints.api.Waypoint
import de.md5lukas.waypoints.api.WaypointShare
import de.md5lukas.waypoints.gui.WaypointsGUI
import de.md5lukas.waypoints.gui.items.CycleSortItem
import kotlinx.coroutines.future.await
import net.kyori.adventure.text.Component

class SharedWaypointsPage(
    wpGUI: WaypointsGUI,
) :
    ListingPage<Pair<WaypointShare, Waypoint>>(
        wpGUI, wpGUI.extendApi { Type.PRIVATE.getBackgroundItem() }) {

  override val title: Component
    get() = wpGUI.translations.INVENTORY_TITLE_SHARED.text

  override suspend fun getContent() =
      PaginationList<Pair<WaypointShare, Waypoint>>(PAGINATION_LIST_PAGE_SIZE).also { list ->
        list.addAll(wpGUI.targetData.getSharedWaypoints().map { it to it.getWaypoint() })
        sortContent(list, wpGUI.viewerData.sortBy)
      }

  override suspend fun toGUIContent(value: Pair<WaypointShare, Waypoint>): GUIContent {
    val (share, waypoint) = value
    return GUIItem(
        wpGUI.extendApi {
          waypoint.getItem(wpGUI.viewer).also { stack ->
            val playerName =
                wpGUI.plugin.uuidUtils.getNameAsync(share.owner).await().let { result ->
                  result?.let { Component.text(it) }
                      ?: wpGUI.translations.SHARING_UNKNOWN_PLAYER.text
                }
            stack.appendLore(
                wpGUI.translations.SHARING_SHARED_BY.withReplacements(
                    "name" placeholder playerName))
          }
        }) {
          wpGUI.skedule {
            if (it.isShiftClick) {
              wpGUI.playSound { clickDanger }
              share.delete()
              updateListingContent()
            } else {
              wpGUI.playSound { clickNormal }
              wpGUI.openWaypoint(waypoint)
            }
          }
        }
  }

  private companion object {
    /** p = previous s = sort mode b = Back n = Next */
    val controlsPattern = GUIPattern("p_s___b_n")
  }

  override fun update() {
    wpGUI.skedule {
      updateListingContent()
      switchContext(SynchronizationContext.SYNC)
      updateControls()
    }
  }

  private fun updateControls(update: Boolean = true) {
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
        's' to
            CycleSortItem(wpGUI) {
              sortContent(listingContent, it)
              updateListingContent()
            },
        'b' to
            GUIItem(wpGUI.translations.GENERAL_BACK.item) {
              wpGUI.playSound { clickNormal }
              wpGUI.goBack()
            },
        'n' to
            GUIItem(wpGUI.translations.GENERAL_NEXT.item) {
              wpGUI.playSound { clickNormal }
              nextPage()
            },
    )

    if (update) {
      wpGUI.gui.update()
    }
  }

  private fun sortContent(content: MutableList<Pair<WaypointShare, Waypoint>>, sort: OverviewSort) {
    content.sortWith(
        compareBy<Pair<WaypointShare, Waypoint>> { it.first.owner }
            .thenBy(sort, Pair<*, Waypoint>::second))
  }

  override suspend fun init() {
    super.init()
    updateListingInInventory()
    updateControls(false)
  }
}
