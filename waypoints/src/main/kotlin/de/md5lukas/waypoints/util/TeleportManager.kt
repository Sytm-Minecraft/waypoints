package de.md5lukas.waypoints.util

import de.md5lukas.commons.paper.fuzzyEquals
import de.md5lukas.commons.paper.placeholder
import de.md5lukas.commons.paper.registerEvents
import de.md5lukas.commons.paper.teleportKeepOrientation
import de.md5lukas.waypoints.WaypointsPermissions
import de.md5lukas.waypoints.WaypointsPlugin
import de.md5lukas.waypoints.api.Type
import de.md5lukas.waypoints.api.Waypoint
import de.md5lukas.waypoints.api.WaypointsPlayer
import de.md5lukas.waypoints.config.general.TeleportPaymentType
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.ceil
import kotlin.math.min
import kotlinx.coroutines.future.await
import kotlinx.coroutines.time.delay as delayTime
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent

class TeleportManager(private val plugin: WaypointsPlugin) : Listener {

  private val teleportIds = AtomicInteger()
  private val pendingTeleports = ConcurrentHashMap<Player, Int>()

  init {
    plugin.registerEvents(this)
  }

  fun getTeleportPermission(waypoint: Waypoint) =
      when (waypoint.type) {
        Type.PRIVATE,
        Type.DEATH -> WaypointsPermissions.TELEPORT_PRIVATE
        Type.PUBLIC -> WaypointsPermissions.TELEPORT_PUBLIC
        Type.PERMISSION -> WaypointsPermissions.TELEPORT_PERMISSION
      }

  private fun getTeleportConfig(waypoint: Waypoint) =
      when (waypoint.type) {
        Type.PRIVATE -> plugin.waypointsConfig.general.teleport.private
        Type.DEATH -> plugin.waypointsConfig.general.teleport.death
        Type.PUBLIC -> plugin.waypointsConfig.general.teleport.public
        Type.PERMISSION -> plugin.waypointsConfig.general.teleport.permission
      }

  suspend fun isTeleportEnabled(
      bukkitPlayer: Player,
      player: WaypointsPlayer,
      waypoint: Waypoint,
  ): Boolean {
    val config = getTeleportConfig(waypoint)
    return when {
      config.paymentType === TeleportPaymentType.DISABLED -> false
      waypoint.type !== Type.DEATH || config.onlyLastWaypoint == false -> {
        config.differentWorldAllow || bukkitPlayer.world == waypoint.location.world
      }
      else -> player.deathFolder.getWaypoints().maxByOrNull { it.createdAt } == waypoint
    }
  }

  private suspend fun getTeleportationPrice(player: Player, waypoint: Waypoint): Double {
    val config = getTeleportConfig(waypoint)

    val teleportations =
        if (config.perCategory) {
          plugin.api.getWaypointPlayer(player.uniqueId).getTeleportations(waypoint.type)
        } else {
          waypoint.getWaypointMeta(player.uniqueId).teleportations
        }

    val distance =
        if (player.world == waypoint.location.world) {
          player.location.distance(waypoint.location)
        } else {
          config.differentWorldDistance
        }

    return min(
        config.maxCost.toDouble(),
        config.formula.eval(
            mapOf(
                "n" to teleportations.toDouble(),
                "distance" to distance,
            )))
  }

  suspend fun getTeleportCostDescription(player: Player, waypoint: Waypoint): List<Component>? {
    val config = getTeleportConfig(waypoint)

    if (player.hasPermission(getTeleportPermission(waypoint))) {
      return null
    }

    return when (config.paymentType) {
      TeleportPaymentType.XP ->
          plugin.translations.WAYPOINT_TELEPORT_XP_LEVEL.withReplacements(
              "levels" placeholder ceil(getTeleportationPrice(player, waypoint)).toInt())
      TeleportPaymentType.XP_POINTS ->
          plugin.translations.WAYPOINT_TELEPORT_XP_POINTS.withReplacements(
              "points" placeholder ceil(getTeleportationPrice(player, waypoint)).toInt())
      TeleportPaymentType.VAULT ->
          plugin.translations.WAYPOINT_TELEPORT_BALANCE.withReplacements(
              "balance" placeholder getTeleportationPrice(player, waypoint))
      else -> null
    }
  }

  suspend fun isAllowedToTeleportToWaypoint(player: Player, waypoint: Waypoint): Boolean {
    if (player.hasPermission(getTeleportPermission(waypoint))) return true
    if (!isTeleportEnabled(player, plugin.api.getWaypointPlayer(player.uniqueId), waypoint))
        return false
    val config = getTeleportConfig(waypoint)
    if (config.mustVisit?.not() != false) return true
    return waypoint.getWaypointMeta(player.uniqueId).visited
  }

  suspend fun teleportPlayerToWaypoint(player: Player, waypoint: Waypoint) {
    cancelRunningTeleport(player)
    if (player.hasPermission(getTeleportPermission(waypoint))) {
      if (player.teleportKeepOrientation(waypoint.location).await()) {
        player.playSound(plugin.waypointsConfig.sounds.teleport)
      }
      return
    }

    val playerData = plugin.api.getWaypointPlayer(player.uniqueId)

    playerData.getCooldownUntil(waypoint.type)?.let { cooldownUntil ->
      val remainingMillis = Duration.between(Instant.now(), cooldownUntil).toMillis()
      if (remainingMillis > 0) {
        val remainingCooldown =
            plugin.durationFormatter.formatDuration(Duration.ofMillis(remainingMillis))

        plugin.translations.MESSAGE_TELEPORT_ON_COOLDOWN.send(
            player, "remaining_cooldown" placeholder remainingCooldown)

        return
      }
    }

    val config = getTeleportConfig(waypoint)

    val price = getTeleportationPrice(player, waypoint)

    fun canTeleport() =
        when (config.paymentType) {
          TeleportPaymentType.DISABLED -> false
          TeleportPaymentType.FREE -> true
          TeleportPaymentType.XP -> {
            val canTeleport = player.level >= price
            if (!canTeleport) {
              plugin.translations.MESSAGE_TELEPORT_NOT_ENOUGH_XP.send(
                  player,
                  "current_level" placeholder player.level,
                  "required_level" placeholder ceil(price).toInt())
            }
            canTeleport
          }
          TeleportPaymentType.XP_POINTS -> {
            val points = player.calculateTotalExperiencePoints()
            val canTeleport = points >= price
            if (!canTeleport) {
              plugin.translations.MESSAGE_TELEPORT_NOT_ENOUGH_XP_POINTS.send(
                  player,
                  "current_points" placeholder points,
                  "required_points" placeholder ceil(price).toInt())
            }
            canTeleport
          }
          TeleportPaymentType.VAULT -> {
            val balance = plugin.vaultIntegration.getBalance(player)
            val canTeleport = balance >= price
            if (!canTeleport) {
              plugin.translations.MESSAGE_TELEPORT_NOT_ENOUGH_BALANCE.send(
                  player,
                  "current_balance" placeholder balance,
                  "required_balance" placeholder price)
            }
            canTeleport
          }
        }

    if (canTeleport()) {
      val standStillTime = plugin.waypointsConfig.general.teleport.standStillTime

      if (standStillTime.toSeconds() > 0) {
        plugin.translations.MESSAGE_TELEPORT_STAND_STILL_NOTICE.send(
            player, "duration" placeholder plugin.durationFormatter.formatDuration(standStillTime))
        val id = teleportIds.getAndIncrement()
        pendingTeleports[player] = id
        delayTime(standStillTime)
        if (pendingTeleports[player] != id) {
          return
        }
        pendingTeleports.remove(player)
      }

      if (canTeleport() &&
          when (config.paymentType) {
            TeleportPaymentType.DISABLED -> false
            TeleportPaymentType.FREE -> true
            TeleportPaymentType.XP -> {
              modifyTeleportations(player, waypoint, 1)
              player.giveExpLevels((-ceil(price)).toInt())
              true
            }
            TeleportPaymentType.XP_POINTS -> {
              modifyTeleportations(player, waypoint, 1)
              player.setExperienceLevelAndProgress(
                  player.calculateTotalExperiencePoints() - ceil(price).toInt())
              true
            }
            TeleportPaymentType.VAULT -> {
              modifyTeleportations(player, waypoint, 1)
              plugin.vaultIntegration.withdraw(player, price)
            }
          }) {
        val success = player.teleportKeepOrientation(waypoint.location).await()

        if (success) {
          player.playSound(plugin.waypointsConfig.sounds.teleport)
          val cooldown = config.cooldown

          if (cooldown.toSeconds() > 0) {
            val cooldownUntil = OffsetDateTime.now().plus(cooldown)
            playerData.setCooldownUntil(waypoint.type, cooldownUntil)
            config.alsoApplyCooldownTo.forEach {
              if (it === waypoint.type) return@forEach
              playerData.setCooldownUntil(it, cooldownUntil)
            }
          }
        } else {
          when (config.paymentType) {
            TeleportPaymentType.XP -> {
              player.giveExpLevels(ceil(price).toInt())
            }
            TeleportPaymentType.XP_POINTS -> {
              player.giveExp(ceil(price).toInt())
              modifyTeleportations(player, waypoint, -1)
            }
            TeleportPaymentType.VAULT -> {
              modifyTeleportations(player, waypoint, -1)
              plugin.vaultIntegration.deposit(player, price)
            }
            else -> {}
          }
        }
      }
    }
  }

  private suspend fun modifyTeleportations(player: Player, waypoint: Waypoint, delta: Int) {
    if (getTeleportConfig(waypoint).perCategory) {
      val playerData = plugin.api.getWaypointPlayer(player.uniqueId)
      playerData.setTeleportations(
          waypoint.type, playerData.getTeleportations(waypoint.type) + delta)
    } else {
      val meta = waypoint.getWaypointMeta(player.uniqueId)
      meta.setTeleportations(meta.teleportations + delta)
    }
  }

  @EventHandler
  fun onPlayerLeave(e: PlayerQuitEvent) {
    cancelRunningTeleport(e.player)
  }

  @EventHandler
  fun onPlayerMove(e: PlayerMoveEvent) {
    // Need to perform a fuzzy equals, because after a player stopped moving and first looks around
    // he "snaps" to some double value that's nearby
    if (e.from.fuzzyEquals(e.to, 0.1)) {
      return
    }

    if (cancelRunningTeleport(e.player)) {
      plugin.translations.MESSAGE_TELEPORT_STAND_STILL_MOVED.send(e.player)
    }
  }

  private fun cancelRunningTeleport(player: Player): Boolean =
      pendingTeleports.remove(player) != null
}
