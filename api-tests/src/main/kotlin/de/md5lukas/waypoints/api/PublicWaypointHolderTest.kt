package de.md5lukas.waypoints.api

import java.util.*
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking

abstract class PublicWaypointHolderTest : TestBase() {

  @GlobalTypes
  fun newHolderEmpty(type: Type) = runBlocking {
    val holder = api.holderOfType(type) as PublicWaypointHolder

    val uuid = UUID.randomUUID()

    assertEquals(0, holder.getWaypointsAmount(uuid))
    assertEquals(0, holder.getFoldersAmount(uuid))
  }

  @GlobalTypes
  fun createOwnedWaypoint(type: Type) = runBlocking {
    val holder = api.holderOfType(type) as PublicWaypointHolder

    val uuid = UUID.randomUUID()
    api.getWaypointPlayer(uuid)

    val location = server.createLocation("world", 1, 2, 3)

    holder.createWaypoint("Test", location, uuid)

    assertEquals(1, holder.getWaypointsAmount())
    assertEquals(1, holder.getWaypointsAmount(uuid))
  }

  @GlobalTypes
  fun mixOwnedWaypoints(type: Type) = runBlocking {
    val holder = api.holderOfType(type) as PublicWaypointHolder

    val uuid1 = UUID.randomUUID()
    api.getWaypointPlayer(uuid1)
    val uuid2 = UUID.randomUUID()
    api.getWaypointPlayer(uuid2)

    val location1 = server.createLocation("world1", 1, 2, 3)
    val location2 = server.createLocation("world2", 1, 2, 3)
    val location3 = server.createLocation("world3", 1, 2, 3)

    holder.createWaypoint("Test1", location1, uuid1)
    assertEquals(0, holder.getWaypointsAmount(uuid2))

    holder.createWaypoint("Test2", location2, uuid2)
    holder.createWaypoint("Test3", location3)

    assertEquals(3, holder.getWaypointsAmount())
    assertEquals(1, holder.getWaypointsAmount(uuid1))
    assertEquals(1, holder.getWaypointsAmount(uuid2))
  }

  @GlobalTypes
  fun createFolderOwned(type: Type) = runBlocking {
    val holder = api.holderOfType(type) as PublicWaypointHolder

    val uuid = UUID.randomUUID()
    api.getWaypointPlayer(uuid)

    holder.createFolder("Test", uuid)

    assertEquals(1, holder.getFoldersAmount())
    assertEquals(1, holder.getFoldersAmount(uuid))
  }

  @GlobalTypes
  fun mixOwnedFolders(type: Type) = runBlocking {
    val holder = api.holderOfType(type) as PublicWaypointHolder

    val uuid1 = UUID.randomUUID()
    api.getWaypointPlayer(uuid1)
    val uuid2 = UUID.randomUUID()
    api.getWaypointPlayer(uuid2)

    holder.createFolder("Test1", uuid1)
    assertEquals(0, holder.getFoldersAmount(uuid2))
    holder.createFolder("Test2", uuid2)
    holder.createFolder("Test3")

    assertEquals(3, holder.getFoldersAmount())
    assertEquals(1, holder.getFoldersAmount(uuid1))
    assertEquals(1, holder.getFoldersAmount(uuid2))
  }
}
