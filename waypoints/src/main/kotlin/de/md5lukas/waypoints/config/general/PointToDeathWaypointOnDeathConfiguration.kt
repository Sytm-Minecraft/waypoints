package de.md5lukas.waypoints.config.general

import de.md5lukas.konfig.Configurable
import java.time.Duration

@Configurable
class PointToDeathWaypointOnDeathConfiguration {

  var enabled = false
    private set

  var autoDeselectAfter: Duration = Duration.ZERO
    private set
}
