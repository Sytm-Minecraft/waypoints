package de.md5lukas.examplej;

import de.md5lukas.waypoints.api.WaypointsAPI;
import de.md5lukas.waypoints.api.WaypointsPlayer;
import de.md5lukas.waypoints.api.WaypointsPointerManager;
import de.md5lukas.waypoints.pointers.PointerManager;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class ExamplePlugin extends JavaPlugin {

  private WaypointsAPI api;
  private WaypointsPointerManager waypointsPointerManager;
  private PointerManager pointers;

  @Override
  public void onEnable() {
    api = getServer().getServicesManager().load(WaypointsAPI.class);
    if (api == null) {
      getServer().getPluginManager().disablePlugin(this);
      return;
    }
    waypointsPointerManager = getServer().getServicesManager().load(WaypointsPointerManager.class);
    pointers = getServer().getServicesManager().load(PointerManager.class);

    api.getPublicWaypoints().getAllWaypointsCF().thenAccept(waypoints -> {
      for (var waypoint : waypoints) {
        System.out.printf(
            "Public waypoint %s is at position %s%n", waypoint.getName(), waypoint.getLocation());
      }
    });
  }

  @Override
  public boolean onCommand(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String label,
      @NotNull String[] args) {
    if (sender instanceof Player player) {
      if (args.length == 0) {
        api.getWaypointPlayerCF(player.getUniqueId())
            .thenCompose(WaypointsPlayer::getAllWaypointsCF)
            .thenAccept(waypoints -> {
              for (var waypoint : waypoints) {
                waypoint
                    .getFolderCF()
                    .thenAccept(folder -> sender.sendMessage(Component.text(String.format(
                        "You have the waypoint %s in the folder %s",
                        waypoint.getName(), folder != null ? folder.getName() : "none"))));
              }
            });
      } else {
        api.getWaypointByIDCF(UUID.fromString(args[0])).thenAccept(waypoint -> {
          if (waypoint != null) {
            waypointsPointerManager.enable(player, waypoint);
          }
        });
      }
    }

    return true;
  }
}
