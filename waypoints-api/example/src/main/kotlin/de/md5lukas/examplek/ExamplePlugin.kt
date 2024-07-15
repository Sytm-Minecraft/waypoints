package de.md5lukas.examplek

import de.md5lukas.waypoints.api.WaypointsAPI
import de.md5lukas.waypoints.api.WaypointsPointerManager
import de.md5lukas.waypoints.pointers.PointerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import kotlin.coroutines.EmptyCoroutineContext

class ExamplePlugin : JavaPlugin() {

  private lateinit var api: WaypointsAPI
  private lateinit var waypointsPointerManager: WaypointsPointerManager
  private lateinit var pointers: PointerManager

  override fun onEnable() {
    val localApi = server.servicesManager.load(WaypointsAPI::class.java)
    if (localApi === null) {
      server.pluginManager.disablePlugin(this)
      return
    }
    this.api = localApi
    this.waypointsPointerManager = server.servicesManager.load(WaypointsPointerManager::class.java)!!
    this.pointers = server.servicesManager.load(PointerManager::class.java)!!

    CoroutineScope(EmptyCoroutineContext).launch {
      api.publicWaypoints.getAllWaypoints().forEach { waypoint ->
        println("Public waypoint ${waypoint.name} is at position ${waypoint.location}")
      }
    }
  }

  override fun onCommand(
      sender: CommandSender,
      command: Command,
      label: String,
      args: Array<String>,
  ): Boolean {
    if (sender is Player) {
      if (args.isEmpty()) {
        CoroutineScope(EmptyCoroutineContext).launch {
          api.getWaypointPlayer(sender.uniqueId).getAllWaypoints().forEach { waypoint ->
            sender.sendMessage(
              Component.text(
                "You have the waypoint ${waypoint.name} in the folder ${waypoint.getFolder()?.name ?: "none"}"))
          }
        }
      } else {
        CoroutineScope(EmptyCoroutineContext).launch {
          api.getWaypointByID(UUID.fromString(args[0]))?.let { waypoint ->
            waypointsPointerManager.enable(sender, waypoint)
          }
        }
      }
    }

    return true
  }
}
