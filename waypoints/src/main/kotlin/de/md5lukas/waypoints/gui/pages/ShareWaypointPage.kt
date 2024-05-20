package de.md5lukas.waypoints.gui.pages

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.switchContext
import de.md5lukas.commons.collections.PaginationList
import de.md5lukas.commons.paper.editMeta
import de.md5lukas.commons.paper.placeholder
import de.md5lukas.kinvs.GUIPattern
import de.md5lukas.kinvs.items.GUIItem
import de.md5lukas.waypoints.api.Waypoint
import de.md5lukas.waypoints.gui.WaypointsGUI
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.SkullMeta

class ShareWaypointPage(
    wpGUI: WaypointsGUI,
    private val waypoint: Waypoint,
) : ListingPage<Player>(wpGUI, wpGUI.extendApi { waypoint.type.getBackgroundItem() }) {

  override val title: Component =
      wpGUI.translations.INVENTORY_TITLE_SHARE.withReplacements(
          "waypoint" placeholder waypoint.name)

  override suspend fun getContent() =
      PaginationList<Player>(PAGINATION_LIST_PAGE_SIZE).also { list ->
        val sharedWith = waypoint.getSharedWith()
        wpGUI.plugin.server.onlinePlayers.forEach { player ->
          if (player !== wpGUI.viewer && sharedWith.none { it.sharedWith == player.uniqueId }) {
            list.add(player)
          }
        }
      }

  override suspend fun toGUIContent(value: Player) =
      GUIItem(
          wpGUI.translations.SHARING_PLAYER_SELECT.getItem("name" placeholder value.displayName())
              .also { stack -> stack.editMeta<SkullMeta> { owningPlayer = value } }) {
            wpGUI.skedule {
              val id = value.uniqueId
              if (waypoint.getSharedWith().any { it.sharedWith == id }) {
                wpGUI.playSound { clickError }
                wpGUI.translations.MESSAGE_SHARING_ALREADY_SHARED.send(
                    wpGUI.viewer,
                    "name" placeholder value.displayName(),
                )
              } else {
                // Ensure that the player exists
                wpGUI.plugin.api.getWaypointPlayer(id)
                waypoint.shareWith(id)
                wpGUI.playSound { clickSuccess }
                wpGUI.translations.MESSAGE_SHARING_SUCCESS.send(
                    wpGUI.viewer,
                    "name" placeholder value.displayName(),
                )
                updateListingContent()
              }
            }
          }

  private companion object {
    /** p = previous s = sharing r = Refresh players b = Back n = Next */
    val controlsPattern = GUIPattern("p_s_r_b_n")
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
            GUIItem(wpGUI.translations.SHARING_VIEW_SHARING.item) {
              wpGUI.skedule {
                val page = SharingWaypointPage(wpGUI, waypoint).apply { init() }
                switchContext(SynchronizationContext.SYNC)
                wpGUI.playSound { clickNormal }
                wpGUI.open(page)
              }
            },
        'r' to
            GUIItem(wpGUI.translations.PLAYER_LIST_REFRESH_LISTING.item) {
              wpGUI.playSound { clickNormal }
              wpGUI.skedule { updateListingContent() }
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
