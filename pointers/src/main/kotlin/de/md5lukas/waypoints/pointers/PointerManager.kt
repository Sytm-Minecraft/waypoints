package de.md5lukas.waypoints.pointers

import de.md5lukas.waypoints.pointers.config.PointerConfiguration
import de.md5lukas.waypoints.pointers.variants.PointerVariant
import de.md5lukas.waypoints.pointers.variants.TrailPointer
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.plugin.Plugin

/**
 * The PointerManager handles the creation of the selected PointerTypes and manages their tasks
 *
 * @constructor Creates a new PointerManager
 * @property plugin The plugin to register the tasks for
 * @property hooks The callbacks this library requires to be implemented by the caller
 * @property configuration The configuration for the pointers
 */
class PointerManager(
    internal val plugin: Plugin,
    internal val hooks: Hooks,
    internal var configuration: PointerConfiguration,
) : Listener {

  init {
    plugin.server.pluginManager.registerEvents(this, plugin)
  }

  private val players = ConcurrentHashMap<UUID, ManagedPlayer>()

  /**
   * Safely shuts down all pointers, recreates them based on the new configuration and restarts them
   *
   * @param newConfiguration The new configuration to use
   */
  fun applyNewConfiguration(newConfiguration: PointerConfiguration) {
    configuration = newConfiguration
    TrailPointer.resetPathfinder()
    players.values.forEach { it.reapplyConfiguration() }
  }

  /**
   * This method should be called when a player edits his enabled pointers to apply the changes
   * immediately
   *
   * @param player the player to restart the pointers for
   * @see Hooks.loadEnabledPointers
   */
  fun reapplyConfiguration(player: Player) {
    players[player.uniqueId]?.reapplyConfiguration()
  }

  /**
   * Enables the pointer for a player towards the provided trackable.
   *
   * This will call [Hooks.saveActiveTrackables] to save this new active trackable
   */
  fun enable(player: Player, trackable: Trackable): Unit = enable(player, trackable, true)

  private fun enable(player: Player, trackable: Trackable, save: Boolean) {
    val managedPlayer = players.computeIfAbsent(player.uniqueId) { ManagedPlayer(this, player) }
    managedPlayer.show(trackable)
    if (save) {
      hooks.saveActiveTrackables(player, managedPlayer.readOnlyTracked)
    }
  }

  /**
   * Disables the pointer for the given player.
   *
   * This will call [Hooks.saveActiveTrackables]
   */
  fun disable(player: Player, predicate: TrackablePredicate): Unit =
      disable(player, predicate, true)

  private fun disable(player: Player, predicate: TrackablePredicate, save: Boolean) {
    players[player.uniqueId]?.let { managedPlayer ->
      managedPlayer.readOnlyTracked.filter(predicate).forEach { managedPlayer.hide(it) }
      if (save) {
        hooks.saveActiveTrackables(player, managedPlayer.readOnlyTracked)
      }
      if (managedPlayer.canBeDiscarded) {
        players -= player.uniqueId
      }
    }
  }

  /**
   * Disables all pointers where the trackable matches the [predicate].
   *
   * This will call [Hooks.saveActiveTrackables] for every player.
   */
  fun disableAll(predicate: TrackablePredicate) {
    players.keys.forEach { uuid -> plugin.server.getPlayer(uuid)?.let { disable(it, predicate) } }
  }

  /** Gets the current trackables for the player */
  fun getCurrentTargets(player: Player): Collection<Trackable> =
      players[player.uniqueId]?.readOnlyTracked ?: emptyList()

  @EventHandler
  internal fun onPlayerJoin(e: PlayerJoinEvent) {
    hooks.loadActiveTrackables(e.player).thenAccept { trackables ->
      trackables.forEach { enable(e.player, it, false) }
    }
  }

  @EventHandler
  internal fun onQuit(e: PlayerQuitEvent) {
    players.remove(e.player.uniqueId)?.immediateCleanup()
  }

  @EventHandler
  internal fun onMove(e: PlayerMoveEvent) {
    val trackables = getCurrentTargets(e.player)

    val disableWhenReachedRadius = configuration.disableWhenReachedRadiusSquared

    if (trackables.isEmpty() || disableWhenReachedRadius == 0) {
      return
    }

    trackables.forEach {
      if (e.player.world === it.location.world) {
        val distance = e.player.location.distanceSquared(it.location)

        if (distance <= disableWhenReachedRadius) {
          disable(e.player, it.asPredicate())
        }
      }
    }
  }

  @EventHandler
  internal fun onPluginDisable(e: PluginDisableEvent) {
    if (e.plugin !== plugin) return

    players.keys.forEach { uuid ->
      plugin.server.getPlayer(uuid)?.let { disable(it, { true }, false) }
    }
  }

  /** Hooks that get called by the [PointerManager] and some pointers */
  interface Hooks {
    /** Hooks required by the action bar pointer */
    val actionBarHooks: ActionBar

    /**
     * Save the provided trackables, if possible, to a non-volatile storage.
     *
     * The ordering of the trackables must be preserved.
     *
     * @param player The player that had the trackable enabled
     * @param tracked The new trackable or <code>null</code> if it has been disabled
     */
    fun saveActiveTrackables(player: Player, tracked: Collection<Trackable>) {}

    /**
     * Load the last active trackables from non-volatile storage.
     *
     * This is called when a player joins the server.
     *
     * @param player The player that had the trackable enabled
     * @return The last trackable or <code>null</code> if it has been disabled
     */
    fun loadActiveTrackables(player: Player): CompletableFuture<Collection<Trackable>> {
      return CompletableFuture.completedFuture(emptyList())
    }

    /**
     * Save the last active compass target of a player to non-volatile storage.
     *
     * @param player The player to store the compass location for
     * @param location The compass location the player previously had set
     */
    fun saveCompassTarget(player: Player, location: Location) {}

    /**
     * Load the last compass target from non-volatile storage.
     *
     * @param player The player to load the compass location for
     * @return The previous compass target or <code>null</code> if there is none
     */
    fun loadCompassTarget(player: Player): CompletableFuture<Location?> {
      return CompletableFuture.completedFuture(null)
    }

    /**
     * Load the pointers the player has enabled for himself.
     *
     * If a [PointerVariant] is not present in the map the default of `true` is used.
     *
     * @param player The player to load the enabled pointers for
     * @return The enabled pointers
     */
    fun loadEnabledPointers(player: Player): CompletableFuture<out Map<PointerVariant, Boolean>> {
      return CompletableFuture.completedFuture(emptyMap())
    }

    interface ActionBar {
      /**
       * Format a message for the player to show him the distance to his target. Only called if
       * [de.md5lukas.waypoints.pointers.config.ActionBarConfiguration.showDistanceEnabled] is set
       * to true.
       *
       * @param player The player that will see this message
       * @param distance3D The distance between the player and the target taking every axis into
       *   account
       * @param heightDifference The height difference between the player and the target. Positive
       *   if the player is higher up
       * @return The formatted message
       */
      fun formatDistanceMessage(
          player: Player,
          distance3D: Double,
          heightDifference: Double
      ): Component

      /**
       * Format a message for the player to show him if he is in an incorrect world.
       *
       * @param player The player that will see this message
       * @param current The world the player is in at the moment
       * @param correct The world the player must travel to
       */
      fun formatWrongWorldMessage(player: Player, current: World, correct: World): Component
    }
  }
}
