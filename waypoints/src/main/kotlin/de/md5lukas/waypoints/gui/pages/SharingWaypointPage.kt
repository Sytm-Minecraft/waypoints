package de.md5lukas.waypoints.gui.pages

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.switchContext
import de.md5lukas.commons.collections.PaginationList
import de.md5lukas.commons.paper.editMeta
import de.md5lukas.commons.paper.placeholder
import de.md5lukas.kinvs.GUIPattern
import de.md5lukas.kinvs.items.GUIContent
import de.md5lukas.kinvs.items.GUIItem
import de.md5lukas.waypoints.api.Waypoint
import de.md5lukas.waypoints.api.WaypointShare
import de.md5lukas.waypoints.gui.WaypointsGUI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.inventory.meta.SkullMeta

class SharingWaypointPage(
    wpGUI: WaypointsGUI,
    private val waypoint: Waypoint,
) : ListingPage<WaypointShare>(wpGUI, wpGUI.extendApi { waypoint.type.getBackgroundItem() }) {

  override val title: Component =
      wpGUI.translations.INVENTORY_TITLE_SHARING.withReplacements(
          "waypoint" placeholder waypoint.name)

  override suspend fun getContent() =
      PaginationList<WaypointShare>(PAGINATION_LIST_PAGE_SIZE).also { list ->
        list.addAll(waypoint.getSharedWith())
      }

  override suspend fun toGUIContent(value: WaypointShare): GUIContent {
    val player = Bukkit.getOfflinePlayer(value.sharedWith)

    val profile = player.playerProfile

    if (!player.isOnline) {
      withContext(Dispatchers.IO) {
        // Do not use update() because that dispatches to a Thread pool of size 2
        player.playerProfile.complete()
      }
    }

    return GUIItem(
        wpGUI.translations.SHARING_PLAYER_DELETE.getItem("name" placeholder profile.name!!).also {
            stack ->
          stack.editMeta<SkullMeta> { playerProfile = profile }
        }) {
          wpGUI.skedule {
            wpGUI.playSound { clickDanger }
            value.delete()
            updateListingContent()
          }
        }
  }

  private companion object {
    /** p = previous b = Back n = Next */
    val controlsPattern = GUIPattern("p_____b_n")
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

  override suspend fun init() {
    super.init()
    updateListingInInventory()
    updateControls(false)
  }
}
