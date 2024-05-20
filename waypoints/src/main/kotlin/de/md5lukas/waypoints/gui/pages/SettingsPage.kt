package de.md5lukas.waypoints.gui.pages

import de.md5lukas.kinvs.GUIPattern
import de.md5lukas.kinvs.items.GUIItem
import de.md5lukas.waypoints.api.Type
import de.md5lukas.waypoints.gui.WaypointsGUI
import de.md5lukas.waypoints.gui.items.ToggleGlobalsItem
import de.md5lukas.waypoints.gui.items.TogglePointerItem
import de.md5lukas.waypoints.gui.items.ToggleTemporaryWaypointsItem
import de.md5lukas.waypoints.pointers.variants.PointerVariant
import net.kyori.adventure.text.Component

class SettingsPage(wpGUI: WaypointsGUI) :
    BasePage(wpGUI, wpGUI.extendApi { Type.PRIVATE.getBackgroundItem() }) {
  private companion object {
    /**
     * - p = Title for pointer settings
     * - 0-8 = Pointer settings
     * - g = Global waypoints toggle
     * - t = Temporary waypoints toggle
     * - b = Back
     */
    val settingsPattern =
        GUIPattern(
            "____p____",
            "753102468",
            "_________",
            "___g_t___",
            "________b",
        )
  }

  override val title: Component
    get() = wpGUI.translations.INVENTORY_TITLE_SETTINGS.text

  private fun updatePage(update: Boolean = true) {
    val mappings =
        mutableMapOf(
            'p' to GUIItem(wpGUI.translations.SETTINGS_POINTERS_TITLE.item),
            'b' to
                GUIItem(wpGUI.translations.GENERAL_BACK.item) {
                  wpGUI.playSound { clickNormal }
                  wpGUI.goBack()
                },
            'g' to
                if (wpGUI.plugin.waypointsConfig.general.features.globalWaypoints) {
                  ToggleGlobalsItem(wpGUI)
                } else background,
            't' to ToggleTemporaryWaypointsItem(wpGUI))

    val enabledPointers =
        PointerVariant.entries.mapNotNull {
          if (it.isEnabled(wpGUI.plugin.waypointsConfig.pointers)) {
            it
          } else {
            null
          }
        }
    var counter =
        if (enabledPointers.size % 2 == 0) {
          1 // When the amount is even do not occupy the center
        } else {
          0
        }
    enabledPointers.forEach {
      mappings[counter.toString().first()] =
          TogglePointerItem(wpGUI, it) {
            wpGUI.playSound { clickNormal }
            wpGUI.plugin.pointerManager.reapplyConfiguration(wpGUI.viewer)
          }
      counter++
    }

    applyPattern(
        settingsPattern,
        0,
        0,
        background,
        mappings,
    )

    if (update) {
      wpGUI.gui.update()
    }
  }

  fun init() {
    updatePage(false)
  }
}
