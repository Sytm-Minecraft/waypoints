package de.md5lukas.waypoints.pointers.variants

import de.md5lukas.schedulers.AbstractScheduler
import de.md5lukas.waypoints.pointers.Pointer
import de.md5lukas.waypoints.pointers.PointerManager
import de.md5lukas.waypoints.pointers.config.PointerConfiguration
import org.bukkit.entity.Player

enum class PointerVariant(
    val key: String,
    val isEnabled: (PointerConfiguration) -> Boolean,
    internal val create: (PointerManager, Player, AbstractScheduler) -> Pointer
) {
  ACTION_BAR("actionBar", { it.actionBar.enabled }, ::ActionBarPointer),
  BEACON("beacon", { it.beacon.enabled }, ::BeaconPointer),
  BLINKING_BLOCK("blinkingBlock", { it.blinkingBlock.enabled }, ::BlinkingBlockPointer),
  BOSS_BAR("bossBar", { it.bossBar.enabled }, ::BossBarPointer),
  COMPASS("compass", { it.compass.enabled }, ::CompassPointer),
  HOLOGRAM("hologram", { it.hologram.enabled }, ::HologramPointer),
  PARTICLE("particle", { it.particle.enabled }, ::ParticlePointer),
  TRAIL("trail", { it.trail.enabled }, ::TrailPointer);

  internal fun canUse(enabledPointerVariants: Set<PointerVariant>) =
      enabledPointerVariants.isEmpty() || this in enabledPointerVariants
}
