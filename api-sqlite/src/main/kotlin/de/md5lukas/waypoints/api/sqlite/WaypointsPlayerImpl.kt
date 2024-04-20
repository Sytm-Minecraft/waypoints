package de.md5lukas.waypoints.api.sqlite

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.md5lukas.jdbc.select
import de.md5lukas.jdbc.selectFirst
import de.md5lukas.jdbc.setValues
import de.md5lukas.jdbc.update
import de.md5lukas.waypoints.api.Folder
import de.md5lukas.waypoints.api.OverviewSort
import de.md5lukas.waypoints.api.Type
import de.md5lukas.waypoints.api.Waypoint
import de.md5lukas.waypoints.api.WaypointShare
import de.md5lukas.waypoints.api.WaypointsPlayer
import de.md5lukas.waypoints.api.base.DatabaseManager
import de.md5lukas.waypoints.api.base.getUUID
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.util.UUID
import kotlinx.coroutines.withContext
import org.bukkit.Location

internal class WaypointsPlayerImpl
private constructor(
    dm: DatabaseManager,
    override val id: UUID,
    showGlobals: Boolean,
    sortBy: OverviewSort,
    canBeTracked: Boolean,
    canReceiveTemporaryWaypoints: Boolean,
    enabledPointers: Map<String, Boolean>,
) : WaypointHolderImpl(dm, Type.PRIVATE, id), WaypointsPlayer {

  private companion object {
    private val gson = Gson()
    private val typeToken = object : TypeToken<Map<String, Boolean>>() {}.type

    private fun serializeEnabledPointers(enabledPointers: Map<String, Boolean>): String? {
      if (enabledPointers.isEmpty()) {
        return null
      }
      // Remove entries where the pointers would be explicitly enabled
      return gson.toJson(enabledPointers.filterValues { !it }, typeToken)
    }

    private fun deserializeEnabledPointers(json: String?): Map<String, Boolean> {
      if (json.isNullOrEmpty()) {
        return emptyMap()
      }
      return gson.fromJson(json, typeToken)
    }
  }

  constructor(
      dm: DatabaseManager,
      row: ResultSet
  ) : this(
      dm = dm,
      id = row.getUUID("id")!!,
      showGlobals = row.getBoolean("showGlobals"),
      sortBy = OverviewSort.valueOf(row.getString("sortBy")),
      canBeTracked = row.getBoolean("canBeTracked"),
      canReceiveTemporaryWaypoints = row.getBoolean("canReceiveTemporaryWaypoints"),
      enabledPointers = deserializeEnabledPointers(row.getString("enabledPointers")),
  )

  override var showGlobals: Boolean = showGlobals
    private set

  override suspend fun setShowGlobals(showGlobals: Boolean) {
    this.showGlobals = showGlobals
    set("showGlobals", showGlobals)
  }

  override var sortBy: OverviewSort = sortBy
    private set

  override suspend fun setSortBy(sortBy: OverviewSort) {
    this.sortBy = sortBy
    set("sortBy", sortBy)
  }

  override var canBeTracked: Boolean = canBeTracked
    private set

  override suspend fun setCanBeTracked(canBeTracked: Boolean) {
    this.canBeTracked = canBeTracked
    set("canBeTracked", canBeTracked)
  }

  override var canReceiveTemporaryWaypoints: Boolean = canReceiveTemporaryWaypoints
    private set

  override suspend fun setCanReceiveTemporaryWaypoints(canReceiveTemporaryWaypoints: Boolean) {
    this.canReceiveTemporaryWaypoints = canReceiveTemporaryWaypoints
    set("canReceiveTemporaryWaypoints", canReceiveTemporaryWaypoints)
  }

  override var enabledPointers: Map<String, Boolean> = enabledPointers
    private set

  override suspend fun setEnabledPointers(enabledPointers: Map<String, Boolean>) {
    this.enabledPointers = enabledPointers
    set("enabledPointers", serializeEnabledPointers(enabledPointers))
  }

  override suspend fun getCooldownUntil(type: Type): OffsetDateTime? =
      withContext(dm.asyncDispatcher) {
        dm.connection.selectFirst(
            "SELECT cooldownUntil FROM player_data_typed WHERE playerId = ? AND type = ?;",
            id.toString(),
            type.name,
        ) {
          getString("cooldownUntil")?.let { OffsetDateTime.parse(it) }
        }
      }

  override suspend fun setCooldownUntil(type: Type, cooldownUntil: OffsetDateTime) {
    withContext(dm.asyncDispatcher) {
      val until = cooldownUntil.toString()
      dm.connection.update(
          "INSERT INTO player_data_typed(playerId, type, cooldownUntil) VALUES (?, ?, ?) ON CONFLICT(playerId, type) DO UPDATE SET cooldownUntil = ?;",
          id.toString(),
          type.name,
          until,
          until,
      )
    }
  }

  override suspend fun getTeleportations(type: Type): Int =
      withContext(dm.asyncDispatcher) {
        dm.connection.selectFirst(
            "SELECT teleportations FROM player_data_typed WHERE playerId = ? AND type = ?;",
            id.toString(),
            type.name,
        ) {
          getInt("teleportations")
        } ?: 0
      }

  override suspend fun setTeleportations(type: Type, teleportations: Int) {
    withContext(dm.asyncDispatcher) {
      dm.connection.update(
          "INSERT INTO player_data_typed(playerId, type, teleportations) VALUES (?, ?, ?) ON CONFLICT(playerId, type) DO UPDATE SET teleportations = ?;",
          id.toString(),
          type.name,
          teleportations,
          teleportations,
      )
    }
  }

  override suspend fun addDeathLocation(location: Location) {
    withContext(dm.asyncDispatcher) { super.createWaypointTyped("", location, Type.DEATH) }
  }

  override val deathFolder: Folder = DeathFolderImpl(dm, id)

  override suspend fun getCompassTarget(): Location? =
      withContext(dm.asyncDispatcher) {
        dm.connection.selectFirst(
            "SELECT * FROM compass_storage WHERE playerId = ?;", id.toString()) {
              Location(
                  dm.plugin.server.getWorld(getString("world"))!!,
                  getDouble("x"),
                  getDouble("y"),
                  getDouble("z"),
              )
            }
      }

  override suspend fun setCompassTarget(location: Location) {
    withContext(dm.asyncDispatcher) {
      dm.connection.update(
          "INSERT INTO compass_storage(playerId, world, x, y, z) VALUES (?, ?, ?, ?, ?) ON CONFLICT(playerId) DO UPDATE SET world = ?, x = ?, y = ?, z = ?;",
          id.toString(),
          location.world!!.name,
          location.x,
          location.y,
          location.z,
          location.world!!.name,
          location.x,
          location.y,
          location.z,
      )
    }
  }

  override suspend fun getSelectedWaypoints(): List<Waypoint> =
      withContext(dm.asyncDispatcher) {
        dm.connection
            .select(
                "SELECT * FROM waypoints INNER JOIN selected_waypoints ON waypoints.id = selected_waypoints.waypointId WHERE selected_waypoints.playerId = ?;",
                id.toString()) {
                  getInt("index") to WaypointImpl(dm, this)
                }
            .apply { sortWith(compareBy { it.first }) }
            .map { it.second }
      }

  override suspend fun setSelectedWaypoints(selected: List<Waypoint>) {
    withContext(dm.asyncDispatcher) {
      dm.connection.update("DELETE FROM selected_waypoints WHERE playerId = ?;", id.toString())
      dm.connection.prepareStatement("INSERT INTO selected_waypoints VALUES (?, ?, ?);").also {
        selected.forEachIndexed { index, waypoint ->
          it.setValues(id.toString(), waypoint.id.toString(), index)
          it.addBatch()
        }
        it.executeBatch()
      }
    }
  }

  override suspend fun getSharingWaypoints(): List<WaypointShare> =
      withContext(dm.asyncDispatcher) {
        dm.connection.select(
            "SELECT * FROM waypoint_shares WHERE owner = ? AND (expires IS NULL OR datetime(expires) > datetime(?));",
            id.toString(),
            OffsetDateTime.now().toString(),
        ) {
          WaypointShareImpl(dm, this)
        }
      }

  override suspend fun hasSharedWaypoints(): Boolean =
      withContext(dm.asyncDispatcher) {
        dm.connection.selectFirst(
            "SELECT EXISTS(SELECT 1 FROM waypoint_shares WHERE sharedWith = ? AND (expires IS NULL OR datetime(expires) > datetime(?)));",
            id.toString(),
            OffsetDateTime.now().toString(),
        ) {
          getInt(1) == 1
        } ?: false
      }

  override suspend fun getSharedWaypoints(): List<WaypointShare> =
      withContext(dm.asyncDispatcher) {
        dm.connection.select(
            "SELECT * FROM waypoint_shares WHERE sharedWith = ? AND (expires IS NULL OR datetime(expires) > datetime(?));",
            id.toString(),
            OffsetDateTime.now().toString(),
        ) {
          WaypointShareImpl(dm, this)
        }
      }

  private suspend fun set(column: String, value: Any?) {
    withContext(dm.asyncDispatcher) {
      dm.connection.update("UPDATE player_data SET $column = ? WHERE id = ?;", value, id)
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as WaypointsPlayer

    return id == other.id
  }

  override fun hashCode(): Int {
    return id.hashCode()
  }

  override fun toString(): String {
    return "WaypointsPlayerImpl(showGlobals=$showGlobals, sortBy=$sortBy, canBeTracked=$canBeTracked) ${super.toString()}"
  }
}
