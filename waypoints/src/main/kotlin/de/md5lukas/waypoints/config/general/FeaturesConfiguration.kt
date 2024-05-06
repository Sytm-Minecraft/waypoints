package de.md5lukas.waypoints.config.general

import de.md5lukas.konfig.ConfigPath
import de.md5lukas.konfig.Configurable

@Configurable
class FeaturesConfiguration {

  var globalWaypoints = true
    private set

  var deathWaypoints = true
    private set

  var teleportation = true
    private set

  @ConfigPath("publicOwnership.waypoints")
  var publicOwnershipWaypoints = false
    private set

  @ConfigPath("publicOwnership.folders")
  var publicOwnershipFolders = false
    private set
    get() = field && publicOwnershipWaypoints
}
