package de.md5lukas.waypoints.db

import de.md5lukas.jdbc.selectFirst
import de.md5lukas.jdbc.update
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player

class CompassStorage(
    private val dm: DatabaseManager
) {

    fun saveCompassLocation(player: Player, location: Location) {
        dm.connection.update(
            "INSERT OR IGNORE INTO compass_storage(playerId, world, x, y, z) VALUES (?, ?, ?, ?, ?);",
            player.uniqueId.toString(),
            location.world!!.name,
            location.x,
            location.y,
            location.z,
        )
    }

    fun loadCompassLocation(player: Player): Location? =
        dm.connection.selectFirst("SELECT * FROM compass_storage WHERE playerId = ?;", player.uniqueId.toString()) {
            Location(
                Bukkit.getWorld(getString("world"))!!,
                getDouble("x"),
                getDouble("y"),
                getDouble("z"),
            )
        }
}