package de.md5lukas.waypoints.api

import org.bukkit.Location
import org.bukkit.entity.Player

/**
 * Simplified PointerManager when working only with Waypoints API trackables like [Waypoint]s,
 * Players and Temporary Waypoints (plain Location)
 */
interface WaypointsPointerManager {

  /**
   * Shows the player a normal waypoint.
   *
   * @param player The player to show the waypoint to
   * @param waypoint The waypoint to show
   */
  fun enable(player: Player, waypoint: Waypoint)

  /**
   * Shows the player a player tracking waypoint.
   *
   * @param player The player to show the waypoint to
   * @param target The player to point towards
   */
  fun enable(player: Player, target: Player)

  /**
   * Shows the player a temporary waypoint.
   *
   * @param player The player to show the waypoint to
   * @param target The location of the temporary waypoint
   */
  fun enable(player: Player, target: Location)

  /**
   * Hides the given waypoint
   *
   * @param player The player to hide the waypoint from
   * @param waypoint The waypoint to hide
   */
  fun disable(player: Player, waypoint: Waypoint)

  /**
   * Hides the given player tracking waypoint
   *
   * @param player The player to hide the waypoint from
   * @param target The player of the tracking waypoint to hide
   */
  fun disable(player: Player, target: Player)

  /**
   * Hides all temporary waypoints
   *
   * @param player The player to hide the temporary waypoints from
   */
  fun disableAllTemporaryWaypoints(player: Player)

  /**
   * Hides all waypoints
   *
   * @param player The player to hide the waypoints from
   */
  fun disableAll(player: Player)

  /**
   * Get all waypoints that the player is currently seeing
   *
   * @param player The player to get the waypoints from
   * @return The waypoints that the player sees
   */
  fun getWaypoints(player: Player): Collection<Waypoint>

  /**
   * Get all players that the player is currently tracking
   *
   * @param player The player to get the tracked players from
   * @return The players that the player tracks
   */
  fun getTrackedPlayers(player: Player): Collection<Player>

  /**
   * Get all temporary waypoints that the player is currently seeing
   *
   * @param player The player to get the temporary waypoints from
   * @return The temporary waypoints that the player sees
   */
  fun getTemporaryWaypoints(player: Player): Collection<Location>
}
