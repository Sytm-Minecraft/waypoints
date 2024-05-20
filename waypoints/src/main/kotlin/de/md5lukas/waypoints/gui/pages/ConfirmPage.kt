package de.md5lukas.waypoints.gui.pages

import de.md5lukas.kinvs.GUIPattern
import de.md5lukas.kinvs.items.GUIItem
import de.md5lukas.waypoints.gui.WaypointsGUI
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack

class ConfirmPage(
    wpGUI: WaypointsGUI,
    questionItem: ItemStack,
    falseItem: ItemStack,
    trueItem: ItemStack,
    onClick: (Boolean) -> Unit
) : BasePage(wpGUI, wpGUI.translations.CONFIRM_BACKGROUND.item) {

  private companion object {
    /** q = question f = false t = true */
    val confirmPattern =
        GUIPattern(
            "_________",
            "____q____",
            "_________",
            "_f_____t_",
            "_________",
        )
  }

  override val title: Component
    get() = wpGUI.translations.INVENTORY_TITLE_CONFIRM.text

  init {
    applyPattern(
        confirmPattern,
        0,
        0,
        background,
        'q' to GUIItem(questionItem),
        'f' to GUIItem(falseItem) { onClick(false) },
        't' to GUIItem(trueItem) { onClick(true) },
    )
  }
}
