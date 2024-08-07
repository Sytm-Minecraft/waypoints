package de.md5lukas.waypoints.config.general

import de.md5lukas.konfig.Configurable

@Configurable
class GeneralConfiguration {

  var language: String = ""
    private set

  var updateChecker: Boolean = true
    private set

  var worldNotFound: WorldNotFoundAction = WorldNotFoundAction.SHOW
    private set

  var hideWaypointsFromDifferentWorlds: Boolean = false
    private set

  val features = FeaturesConfiguration()

  val commands = CommandsConfiguration()

  val waypoints = LimitConfiguration()

  val folders = LimitConfiguration()

  val customIconFilter = CustomIconFilterConfiguration()

  val openWithItem = OpenWithItemConfiguration()

  val teleport = TeleportConfiguration()

  val availableWorlds = AvailableWorldsConfiguration()

  val pointToDeathWaypointOnDeath = PointToDeathWaypointOnDeathConfiguration()
}
