package de.md5lukas.waypoints.api

import java.util.*
import org.bukkit.Location

interface PublicWaypointHolder : WaypointHolder {

  @JvmSynthetic suspend fun getWaypointsAmount(creator: UUID): Int

  fun getWaypointsAmountCF(creator: UUID) = future { getWaypointsAmount(creator) }

  @JvmSynthetic suspend fun getFoldersAmount(creator: UUID): Int

  fun getFoldersAmountCF(creator: UUID) = future { getFoldersAmount(creator) }

  /**
   * Creates a new Waypoint in this holder with the given name and location, created by an arbitrary
   * creator
   *
   * @param name The name of the waypoint
   * @param location The location of the waypoint
   * @param creator The player that created this waypoint
   * @return The newly created waypoint
   */
  @JvmSynthetic
  suspend fun createWaypoint(name: String, location: Location, creator: UUID): Waypoint

  fun createWaypointCF(name: String, location: Location, creator: UUID) = future {
    createWaypoint(name, location, creator)
  }

  /**
   * Creates a new folder in this holder with the given name
   *
   * @param name The name of the folder
   * @return The newly created folder
   */
  @JvmSynthetic suspend fun createFolder(name: String, creator: UUID): Folder

  fun createFolderCF(name: String, creator: UUID) = future { createFolder(name, creator) }
}
