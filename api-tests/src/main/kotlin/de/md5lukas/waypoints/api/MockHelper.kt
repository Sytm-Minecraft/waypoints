package de.md5lukas.waypoints.api

import be.seeseemelk.mockbukkit.ServerMock
import java.util.UUID
import org.bukkit.Location
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

fun ServerMock.createLocation(world: String, x: Int, y: Int, z: Int): Location {
  return Location(
      getWorld(world) ?: addSimpleWorld(world), x.toDouble(), y.toDouble(), z.toDouble())
}

suspend fun WaypointsAPI.holderOfType(type: Type) =
    when (type) {
      Type.PUBLIC -> publicWaypoints
      Type.PERMISSION -> permissionWaypoints
      Type.PRIVATE -> getWaypointPlayer(UUID.randomUUID())
      else -> throw IllegalArgumentException("A holder of type $type is not available")
    }

@ParameterizedTest
@EnumSource(value = Type::class, mode = EnumSource.Mode.EXCLUDE, names = ["DEATH"])
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class TypesNoDeath

@ParameterizedTest
@EnumSource(value = Type::class, mode = EnumSource.Mode.INCLUDE, names = ["PUBLIC", "PERMISSION"])
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class GlobalTypes
