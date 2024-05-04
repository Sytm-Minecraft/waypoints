package de.md5lukas.waypoints.config.integrations

import de.md5lukas.konfig.Configurable

@Configurable
class IntegrationsConfiguration {

  val geyser = GeyserConfiguration()

  val dynmap = DynMapConfiguration()
  val squaremap = SquareMapConfiguration()
  val bluemap = BlueMapConfiguration()
  val pl3xmap = Pl3xMapConfiguration()
}
