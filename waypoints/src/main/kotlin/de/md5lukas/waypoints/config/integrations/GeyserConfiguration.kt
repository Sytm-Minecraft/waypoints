package de.md5lukas.waypoints.config.integrations

import de.md5lukas.konfig.ConfigPath
import de.md5lukas.konfig.Configurable

@Configurable
class GeyserConfiguration {

  var enabled: Boolean = false
    private set

  @ConfigPath("icon.accept")
  var acceptIcon: String = ""
    private set

  @ConfigPath("icon.decline")
  var declineIcon: String = ""
    private set
}
