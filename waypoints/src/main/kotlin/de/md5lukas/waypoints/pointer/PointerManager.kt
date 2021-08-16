package de.md5lukas.waypoints.pointer

import de.md5lukas.waypoints.WaypointsPlugin
import de.md5lukas.waypoints.api.Waypoint
import de.md5lukas.waypoints.config.pointers.PointerConfiguration
import de.md5lukas.waypoints.pointer.variants.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PointerManager(
    private val plugin: WaypointsPlugin
) : Listener {

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    private val availablePointers: List<(PointerConfiguration) -> Pointer?> = listOf(
        {
            with(it.actionBarConfiguration) {
                if (enabled) {
                    ActionBarPointer(this, plugin)
                } else {
                    null
                }
            }
        }, {
            with(it.beaconConfiguration) {
                if (enabled) {
                    BeaconPointer(this)
                } else {
                    null
                }
            }
        }, {
            with(it.blinkingBlockConfiguration) {
                if (enabled) {
                    BlinkingBlockPointer(this)
                } else {
                    null
                }
            }
        }, {
            with(it.compassConfiguration) {
                if (enabled) {
                    CompassPointer(this, plugin)
                } else {
                    null
                }
            }
        }, {
            with(it.particleConfiguration) {
                if (enabled) {
                    ParticlePointer(this)
                } else {
                    null
                }
            }
        }
    )

    private val enabledPointers: MutableList<Pointer> = ArrayList()

    private val activePointers: MutableMap<Player, Waypoint> = HashMap()


    private fun setupPointers() {
        availablePointers.forEach { supplier ->
            supplier(plugin.waypointsConfig.pointerConfiguration)?.let { pointer ->
                enabledPointers.add(pointer)

                if (pointer.interval > 0) {
                    plugin.server.scheduler.runTaskTimer(plugin, PointerTask(pointer, activePointers), 0, pointer.interval.toLong())
                }
            }
        }
    }

    fun enable(player: Player, waypoint: Waypoint) {
        activePointers.put(player, waypoint)?.let {
            hide(player, it)
        }
        show(player, waypoint)
    }

    fun disable(player: Player) {
        activePointers.remove(player)?.let {
            hide(player, it)
        }
    }

    private fun show(player: Player, target: Waypoint) {
        enabledPointers.forEach {
            it.show(player, target)
        }
    }

    private fun hide(player: Player, target: Waypoint) {
        enabledPointers.forEach {
            it.hide(player, target)
        }
    }

    @EventHandler
    private fun onQuit(e: PlayerQuitEvent) {
        disable(e.player)
    }
}