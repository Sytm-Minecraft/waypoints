package de.md5lukas.waypoints.config.pointers

import de.md5lukas.konfig.ConfigPath
import de.md5lukas.konfig.Configurable
import de.md5lukas.waypoints.pointers.config.TrailConfiguration
import org.bukkit.Particle

@Configurable
class TrailConfigurationImpl : RepeatingPointerConfigurationImpl(), TrailConfiguration {

  @ConfigPath("pathing.maxLength")
  override var pathingMaxLength: Int = 0
    private set

  @ConfigPath("pathing.maxIterations")
  override var pathingMaxIterations: Int = 0
    private set

  @ConfigPath("pathing.allowChunkLoading")
  override var pathingAllowChunkLoading: Boolean = false
    private set

  @ConfigPath("pathing.allowChunkGeneration")
  override var pathingAllowChunkGeneration: Boolean = false
    private set

  @ConfigPath("pathing.swimPenalty")
  override var pathingSwimPenalty: Double = 1.0
    private set

  @ConfigPath("pathing.heuristicWeight")
  override var pathingHeuristicWeight: Double = 1.0
    private set

  @ConfigPath("pathInvalidationDistance")
  override var pathInvalidationDistanceSquared: Int = 0
    private set(value) {
      field = value * value
    }

  @ConfigPath("pathCalculateAheadDistance")
  override var pathCalculateAheadDistanceSquared: Int = 0
    private set(value) {
      field = value * value
    }

  @ConfigPath("retainMaxPlayerDistance")
  override var retainMaxPlayerDistanceSquared: Int = 0
    private set(value) {
      field = value * value
    }

  @ConfigPath("particle.spread")
  override var particleSpread: Double = 0.0
    private set

  @ConfigPath("particle.amount")
  override var particleAmount: Int = 0
    private set

  @ConfigPath("particle.normal")
  override var particleNormal: Particle = Particle.WAX_ON
    private set

  @ConfigPath("particle.highlight")
  override var particleHighlight: Particle = Particle.WAX_OFF
    private set

  override var highlightDistance: Int = 0
    private set
}
