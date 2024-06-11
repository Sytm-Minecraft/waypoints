package de.md5lukas.waypoints.pointers

import com.okkero.skedule.future
import com.okkero.skedule.skedule
import de.md5lukas.commons.paper.placeholder
import de.md5lukas.waypoints.WaypointsPlugin
import de.md5lukas.waypoints.pointers.PointerManager.Hooks
import de.md5lukas.waypoints.pointers.variants.PointerVariant
import java.util.concurrent.CompletableFuture
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player

class PointerManagerHooks(private val plugin: WaypointsPlugin) : Hooks {

  override val actionBarHooks: Hooks.ActionBar = ActionBarPointerHooks()

  override fun saveActiveTrackables(player: Player, tracked: Collection<Trackable>) {
    plugin.skedule {
      plugin.api
          .getWaypointPlayer(player.uniqueId)
          .setSelectedWaypoints(tracked.mapNotNull(WaypointTrackable.Extract))
    }
  }

  override fun loadActiveTrackables(player: Player): CompletableFuture<Collection<Trackable>> =
      plugin.future {
        plugin.api.getWaypointPlayer(player.uniqueId).getSelectedWaypoints().map {
          WaypointTrackable(plugin, it)
        }
      }

  override fun saveCompassTarget(player: Player, location: Location) {
    plugin.skedule { plugin.api.getWaypointPlayer(player.uniqueId).setCompassTarget(location) }
  }

  override fun loadCompassTarget(player: Player) =
      plugin.future { plugin.api.getWaypointPlayer(player.uniqueId).getCompassTarget() }

  override fun loadEnabledPointers(player: Player) =
      plugin.future {
        val result = mutableMapOf<PointerVariant, Boolean>()
        plugin.api.getWaypointPlayer(player.uniqueId).enabledPointers.forEach { (storedKey, value)
          ->
          PointerVariant.entries.firstOrNull { it.key == storedKey }?.let { result[it] = value }
        }
        result
      }

  private inner class ActionBarPointerHooks : Hooks.ActionBar {
    override fun formatDistanceMessage(
        player: Player,
        distance3D: Double,
        heightDifference: Double
    ): Component =
        plugin.translations.POINTERS_ACTION_BAR_DISTANCE.withReplacements(
            "distance" placeholder distance3D,
            "height_difference" placeholder heightDifference,
        )

    override fun formatWrongWorldMessage(
        player: Player,
        current: World,
        correct: World
    ): Component =
        plugin.translations.POINTERS_ACTION_BAR_WRONG_WORLD.withReplacements(
            "current" placeholder plugin.worldTranslations.getWorldName(current),
            "correct" placeholder plugin.worldTranslations.getWorldName(correct),
        )
  }
}
