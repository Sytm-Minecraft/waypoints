package de.md5lukas.waypoints.pointers

import de.md5lukas.schedulers.AbstractScheduledTask
import de.md5lukas.schedulers.Schedulers
import de.md5lukas.waypoints.pointers.variants.PointerVariant
import java.util.Collections
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.floor
import org.bukkit.Location
import org.bukkit.entity.Player

internal class ManagedPlayer(
    private val pointerManager: PointerManager,
    private val player: Player
) {

  private val scheduler = Schedulers.entity(pointerManager.plugin, player)
  private val pointerSetupLock = Any()
  private var pointerSetup: CompletableFuture<*>? = null
  private val pointers = mutableListOf<PlayerPointer>()
  private val tracked = CopyOnWriteArrayList<Trackable>()
  val readOnlyTracked: Collection<Trackable> = Collections.unmodifiableCollection(tracked)

  val canBeDiscarded
    get() = tracked.isEmpty()

  fun show(trackable: Trackable) {
    if (trackable in tracked) {
      if (tracked.last() != trackable) {
        tracked.remove(trackable)
        tracked.add(trackable)
      }
    } else {
      tracked.add(trackable)
    }
    logger.debug("Showing Trackable {}", trackable)

    if (pointers.isEmpty()) {
      scheduleTasks()
    }
  }

  fun hide(trackable: Trackable) {
    tracked.remove(trackable)

    val discard = canBeDiscarded
    logger.debug("Hiding Trackable {} Discard {}", trackable, discard)
    pointers.forEach {
      synchronized(it.hide) { it.hide.add(trackable) }
      it.scheduleKill = discard
    }
    if (discard) {
      pointers.clear()
      pointerSetup = null
    }
  }

  fun immediateCleanup() {
    pointers.forEach {
      it.task.cancel()
      it.immediateCleanup()
    }
    pointers.clear()
  }

  fun reapplyConfiguration() {
    val copy = ArrayList(pointers)

    scheduler.schedule {
      copy.forEach {
        it.task.cancel()
        it.immediateCleanup()
      }
    }

    pointers.clear()
    pointerSetup = null

    scheduleTasks()
  }

  private fun scheduleTasks() {
    if (pointerSetup !== null) return
    synchronized(pointerSetupLock) {
      if (pointerSetup !== null) return
      pointerSetup =
          pointerManager.hooks
              .loadEnabledPointers(player)
              .thenAccept { enabled ->
                PointerVariant.entries
                    .filter {
                      it.isEnabled(pointerManager.configuration) && enabled.getOrDefault(it, true)
                    }
                    .forEach {
                      val pointer = it.create(pointerManager, player, scheduler)

                      logger.debug("Commissioning new PlayerPointer for {}", pointer)
                      val playerPointer = PlayerPointer(pointer)
                      val task =
                          if (pointer.async) {
                            scheduler.scheduleAtFixedRateAsync(
                                interval = pointer.interval.toLong(),
                                delay = 1L,
                                block = playerPointer,
                            )
                          } else {
                            scheduler.scheduleAtFixedRate(
                                interval = pointer.interval.toLong(),
                                delay = 1L,
                                block = playerPointer,
                            )
                          }
                      task?.let {
                        playerPointer.task = task
                        pointers += playerPointer
                      }
                    }
              }
              .exceptionally {
                logger.error("Couldn't load enabled pointer types for player ${player.name}", it)
                null
              }
    }
  }

  inner class PlayerPointer(private val pointer: Pointer) : Runnable {

    lateinit var task: AbstractScheduledTask

    var scheduleKill = false
    val hide = mutableListOf<Trackable>()
    private var previousPrimary: Trackable? = null

    override fun run() {
      synchronized(hide) {
        hide.forEach {
          if (!pointer.variant.canUse(it.enabledPointerVariants)) return@forEach
          logger.debug("Hiding {} on {}", it, pointer)
          if (pointer.supportsMultipleTargets) {
            pointer.hide(it, translateTarget(it))
          } else if (it === previousPrimary) {
            pointer.hide(it, translateTarget(it))
            previousPrimary = null
          }
        }
        hide.clear()
      }
      if (scheduleKill) {
        logger.debug("Killing Task for {}", this)
        task.cancel()
        return
      }

      if (pointer.supportsMultipleTargets) {
        logger.debug("Updating all tracked for {}", this)
        pointer.preUpdates()
        tracked.forEach {
          if (pointer.variant.canUse(it.enabledPointerVariants))
              pointer.update(it, translateTarget(it))
        }
        pointer.postUpdates()
      } else {
        val primary = tracked.lastOrNull { pointer.variant.canUse(it.enabledPointerVariants) }
        var updatedRequired = true
        logger.debug(
            "Updating primary Trackable. Previous {}, current {}", previousPrimary, primary)
        if (previousPrimary !== primary) {
          previousPrimary?.let { pointer.hide(it, translateTarget(it)) }
          primary?.let {
            pointer.show(it, translateTarget(it))
            updatedRequired = false
          }
          previousPrimary = primary
        }
        if (primary !== null && updatedRequired) {
          pointer.update(primary, translateTarget(primary))
        }
      }
    }

    fun immediateCleanup() {
      logger.debug("Performing immediate clean up")
      if (pointer.supportsMultipleTargets) {
        tracked.forEach {
          if (pointer.variant.canUse(it.enabledPointerVariants))
              pointer.immediateCleanup(it, translateTarget(it))
        }
      } else {
        previousPrimary?.let { pointer.immediateCleanup(it, translateTarget(it)) }
      }
    }

    override fun toString(): String {
      return "PlayerPointer(pointer=$pointer, task=$task)"
    }
  }

  private fun translateTarget(trackable: Trackable): Location? {
    if (player.world === trackable.location.world) {
      return trackable.location
    }

    pointerManager.configuration.connectedWorlds.entries.forEach {
      if (it.key == player.world.name && it.value == trackable.location.world?.name ||
          it.value == player.world.name && it.key == trackable.location.world?.name) {
        val target = trackable.location.clone()
        target.world = player.world

        if (player.world.name == it.key) {
          target.x *= 8
          target.z *= 8
        } else {
          target.x = floor(target.x / 8)
          target.z = floor(target.z / 8)
        }

        return target
      }
    }

    return null
  }

  private val logger
    get() = pointerManager.plugin.slF4JLogger
}
