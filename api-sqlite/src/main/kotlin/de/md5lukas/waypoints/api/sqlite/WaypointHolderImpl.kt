package de.md5lukas.waypoints.api.sqlite

import de.md5lukas.jdbc.select
import de.md5lukas.jdbc.selectFirst
import de.md5lukas.jdbc.selectNotNull
import de.md5lukas.jdbc.update
import de.md5lukas.waypoints.api.Folder
import de.md5lukas.waypoints.api.SearchResult
import de.md5lukas.waypoints.api.Type
import de.md5lukas.waypoints.api.Waypoint
import de.md5lukas.waypoints.api.WaypointHolder
import de.md5lukas.waypoints.api.base.DatabaseManager
import de.md5lukas.waypoints.api.event.FolderCreateEvent
import de.md5lukas.waypoints.api.event.WaypointCreateEvent
import de.md5lukas.waypoints.api.gui.GUIType
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.withContext
import org.bukkit.Location
import org.bukkit.permissions.Permissible

internal abstract class WaypointHolderImpl(
    internal val dm: DatabaseManager,
    override val type: Type,
    private val owner: UUID?,
) : WaypointHolder {

  override suspend fun getFolders(): List<Folder> =
      withContext(dm.asyncDispatcher) {
        val ownerString = owner?.toString()
        dm.connection.select(
            "SELECT * FROM folders WHERE type = ? AND (? IS NULL OR owner IS ?);",
            type.name,
            ownerString,
            ownerString) {
              FolderImpl(dm, this)
            }
      }

  override suspend fun getWaypoints(): List<Waypoint> =
      withContext(dm.asyncDispatcher) {
        val ownerString = owner?.toString()
        dm.connection.select(
            "SELECT * FROM waypoints WHERE type = ? AND (? IS NULL OR owner IS ?) AND folder IS NULL;",
            type.name,
            ownerString,
            ownerString) {
              WaypointImpl(dm, this)
            }
      }

  override suspend fun getAllWaypoints(): List<Waypoint> =
      withContext(dm.asyncDispatcher) {
        val ownerString = owner?.toString()
        dm.connection.select(
            "SELECT * FROM waypoints WHERE type = ? AND (? IS NULL OR owner IS ?);",
            type.name,
            ownerString,
            ownerString) {
              WaypointImpl(dm, this)
            }
      }

  override suspend fun getWaypointsAmount(): Int =
      withContext(dm.asyncDispatcher) {
        val ownerString = owner?.toString()
        dm.connection.selectFirst(
            "SELECT COUNT(*) FROM waypoints WHERE type = ? AND (? IS NULL OR owner IS ?);",
            type.name,
            ownerString,
            ownerString) {
              getInt(1)
            }!!
      }

  override suspend fun getFoldersAmount(): Int =
      withContext(dm.asyncDispatcher) {
        val ownerString = owner?.toString()
        dm.connection.selectFirst(
            "SELECT COUNT(*) FROM folders WHERE type = ? AND (? IS NULL OR owner IS ?);",
            type.name,
            ownerString,
            ownerString) {
              getInt(1)
            }!!
      }

  override suspend fun getWaypointsVisibleForPlayer(permissible: Permissible): Int =
      if (type == Type.PERMISSION) {
        withContext(dm.asyncDispatcher) {
          dm.connection
              .select("SELECT permission FROM waypoints WHERE type = ?;", type.name) {
                getString("permission")
              }
              .count { permissible.hasPermission(it) }
        }
      } else {
        getWaypointsAmount()
      }

  override suspend fun createWaypoint(name: String, location: Location): Waypoint {
    return createWaypointTyped(name, location, type, owner)
  }

  protected suspend fun createWaypointTyped(
      name: String,
      location: Location,
      type: Type,
      creator: UUID?,
  ): Waypoint =
      withContext(dm.asyncDispatcher) {
        val id = UUID.randomUUID()
        dm.connection.update(
            "INSERT INTO waypoints(id, createdAt, type, owner, name, world, x, y, z) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);",
            id.toString(),
            OffsetDateTime.now().toString(),
            type.name,
            creator?.toString(),
            name,
            location.world!!.name,
            location.x,
            location.y,
            location.z,
        )
        dm.connection
            .selectFirst("SELECT * FROM waypoints WHERE id = ?;", id.toString()) {
              WaypointImpl(dm, this)
            }!!
            .also { WaypointCreateEvent(!dm.testing, it).callEvent() }
      }

  override suspend fun createFolder(name: String): Folder = createFolder0(name, owner)

  protected suspend fun createFolder0(name: String, creator: UUID?): Folder =
      withContext(dm.asyncDispatcher) {
        val id = UUID.randomUUID()
        dm.connection.update(
            "INSERT INTO folders(id, createdAt, type, owner, name) VALUES (?, ?, ?, ?, ?);",
            id.toString(),
            OffsetDateTime.now().toString(),
            type.name,
            creator?.toString(),
            name,
        )
        dm.connection
            .selectFirst("SELECT * FROM folders WHERE id = ?", id.toString()) {
              FolderImpl(dm, this)
            }!!
            .also { FolderCreateEvent(!dm.testing, it).callEvent() }
      }

  override val createdAt: OffsetDateTime = OffsetDateTime.ofInstant(Instant.EPOCH, ZoneId.of("UTC"))

  override suspend fun isDuplicateWaypointName(name: String): Boolean =
      withContext(dm.asyncDispatcher) {
        val ownerString = owner?.toString()
        dm.connection.selectFirst(
            "SELECT EXISTS(SELECT 1 FROM waypoints WHERE type = ? AND (? IS NULL OR owner IS ?) AND name = ? COLLATE NOCASE);",
            type.name,
            ownerString,
            ownerString,
            name,
        ) {
          getInt(1) == 1
        } ?: false
      }

  override suspend fun isDuplicateFolderName(name: String): Boolean =
      withContext(dm.asyncDispatcher) {
        val ownerString = owner?.toString()
        dm.connection.selectFirst(
            "SELECT EXISTS(SELECT 1 FROM folders WHERE type = ? AND (? IS NULL OR owner IS ?) AND name = ? COLLATE NOCASE);",
            type.name,
            ownerString,
            ownerString,
            name,
        ) {
          getInt(1) == 1
        } ?: false
      }

  override suspend fun searchFolders(
      query: String,
      permissible: Permissible?
  ): List<SearchResult<out Folder>> =
      withContext(dm.asyncDispatcher) {
        val ownerString = owner?.toString()

        val (reducedQuery, taggedIndex) = prepareQuery(query)

        dm.connection
            .selectNotNull<Folder>(
                "SELECT * FROM folders WHERE type = ? AND (? IS NULL OR owner IS ?) AND name LIKE ? ESCAPE '!';",
                type.name,
                ownerString,
                ownerString,
                "$reducedQuery%",
            ) {
              FolderImpl(dm, this).also {
                if (permissible !== null &&
                    this@WaypointHolderImpl.type === Type.PERMISSION &&
                    it.getAmountVisibleForPlayer(permissible) == 0)
                    return@selectNotNull null
              }
            }
            .tagDuplicateSearchResults({ it.name }, reducedQuery, taggedIndex)
      }

  override suspend fun searchWaypoints(
      query: String,
      permissible: Permissible?
  ): List<SearchResult<out Waypoint>> =
      withContext(dm.asyncDispatcher) {
        val typeString = type.name
        val ownerString = owner?.toString()

        val (reducedQuery, taggedIndex) = prepareQuery(query)

        if ('/' in reducedQuery) {
              val result = reducedQuery.split('/', limit = 2)
              val folder = "${result[0]}%"
              val waypoint = "${result[1]}%"

              dm.connection.selectNotNull<Waypoint>(
                  "SELECT * FROM waypoints WHERE type = ? AND (? IS NULL OR owner IS ?) AND name LIKE ? ESCAPE '!' AND folder IN (SELECT id FROM folders WHERE type = ? AND (? IS NULL OR owner IS ?) AND name LIKE ? ESCAPE '!');",
                  typeString,
                  ownerString,
                  ownerString,
                  waypoint,
                  typeString,
                  ownerString,
                  ownerString,
                  folder,
              ) {
                if (permissible !== null &&
                    this@WaypointHolderImpl.type === Type.PERMISSION &&
                    !permissible.hasPermission(getString("permission")))
                    return@selectNotNull null

                WaypointImpl(dm, this)
              }
            } else {
              dm.connection.selectNotNull<Waypoint>(
                  "SELECT * FROM waypoints WHERE type = ? AND (? IS NULL OR owner IS ?) AND name LIKE ? ESCAPE '!';",
                  typeString,
                  ownerString,
                  ownerString,
                  "$reducedQuery%",
              ) {
                if (permissible !== null &&
                    this@WaypointHolderImpl.type === Type.PERMISSION &&
                    !permissible.hasPermission(getString("permission")))
                    return@selectNotNull null

                WaypointImpl(dm, this)
              }
            }
            .tagDuplicateSearchResults(Waypoint::getFullPath, reducedQuery, taggedIndex)
      }

  private fun prepareQuery(query: String): Pair<String, Int?> {
    val taggedIndex = query.substringAfterLast('#').toIntOrNull()

    val reducedQuery =
        if (taggedIndex === null) {
              query
            } else {
              query.substringBeforeLast('#')
            }
            .replace("!", "!!")
            .replace("%", "!%")
            .replace("_", "!_")

    return reducedQuery to taggedIndex
  }

  private suspend fun <T> List<T>.tagDuplicateSearchResults(
      nameSelector: suspend (T) -> String,
      reducedQuery: String,
      taggedIndex: Int?
  ): List<SearchResult<out T>> {
    val keys = mutableMapOf<T, String>()
    this.forEach { keys[it] = nameSelector(it) }

    return keys.entries
        .groupBy { it.value }
        .flatMap { (name, tList) ->
          if (taggedIndex !== null) {
            if (name != reducedQuery) {
              return@flatMap emptyList()
            }
            tList.getOrNull(taggedIndex)?.let { t ->
              return@flatMap listOf(SearchResult(t.key, "$name#$taggedIndex"))
            }
          }
          if (tList.size == 1) {
            listOf(SearchResult(tList[0].key, name))
          } else {
            tList.mapIndexed { index, t -> SearchResult(t.key, "$name#$index") }
          }
        }
  }

  override val guiType: GUIType
    get() =
        when (type) {
          Type.PUBLIC -> GUIType.PUBLIC_HOLDER
          Type.PERMISSION -> GUIType.PERMISSION_HOLDER
          else -> throw IllegalStateException("A waypoint holder for a player cannot be a GUI item")
        }

  override val name: String
    get() = guiType.name

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as WaypointHolder

    return type == other.type
  }

  override fun hashCode(): Int {
    return type.hashCode()
  }

  override fun toString(): String {
    return "WaypointHolderImpl(type=$type, owner=$owner)"
  }
}
