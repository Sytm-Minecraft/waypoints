package de.md5lukas.waypoints.api.sqlite

import de.md5lukas.jdbc.selectFirst
import de.md5lukas.waypoints.api.Folder
import de.md5lukas.waypoints.api.PublicWaypointHolder
import de.md5lukas.waypoints.api.Type
import de.md5lukas.waypoints.api.Waypoint
import de.md5lukas.waypoints.api.base.DatabaseManager
import de.md5lukas.waypoints.api.gui.GUIType
import java.util.UUID
import kotlinx.coroutines.withContext
import org.bukkit.Location

internal class PublicWaypointHolderImpl(dm: DatabaseManager, type: Type) :
    WaypointHolderImpl(dm, type, null), PublicWaypointHolder {

  override suspend fun getWaypointsAmount(creator: UUID): Int =
      withContext(dm.asyncDispatcher) {
        dm.connection.selectFirst(
            "SELECT COUNT(*) FROM waypoints WHERE type = ? AND owner IS ?;",
            type.name,
            creator.toString()) {
              getInt(1)
            }!!
      }

  override suspend fun getFoldersAmount(creator: UUID): Int =
      withContext(dm.asyncDispatcher) {
        dm.connection.selectFirst(
            "SELECT COUNT(*) FROM folders WHERE type = ? AND owner IS ?;",
            type.name,
            creator.toString()) {
              getInt(1)
            }!!
      }

  override suspend fun createWaypoint(name: String, location: Location, creator: UUID): Waypoint =
      super.createWaypointTyped(name, location, type, creator)

  override suspend fun createFolder(name: String, creator: UUID): Folder =
      super.createFolder0(name, creator)

  override val guiType: GUIType
    get() =
        when (type) {
          Type.PUBLIC -> GUIType.PUBLIC_HOLDER
          Type.PERMISSION -> GUIType.PERMISSION_HOLDER
          else -> throw IllegalStateException()
        }

  override fun toString(): String {
    return "PublicWaypointHolderImpl(type=$type)"
  }
}
