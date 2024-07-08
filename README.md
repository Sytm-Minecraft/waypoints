# Waypoints

[![Support Discord](https://img.shields.io/static/v1?message=Support%20Discord&color=7289da&style=flat-square&logo=discord&logoColor=white&label)](https://discord.gg/4xY9TcHQja)
![Modrinth Downloads](https://img.shields.io/modrinth/dt/waypoints?label=Modrinth%20Downloads&style=flat-square)
![Spiget Downloads](https://img.shields.io/spiget/downloads/66647?label=Spigot%20Downloads&style=flat-square)

Ever had the problem, that you were on a long trip and can't find your base?

You could set a warp with a conventional plugin at your home and simply teleport back. But if you want to keep the survival experience of not being able to
teleport like that, this plugin could be just what your server needs!

## Direction Indicators

So, instead of just bringing the player back where he started, this plugin shows him the direction he has to walk, so he can explore the world even more. To
show the player the direction you can configure multiple direction indicators to guide the player to his destination.

### Bossbar Indicator

A compass will be visible at the top of the screen, with a highlighter marking in which direction the waypoint is

### Beacon

When the waypoint is in render distance, a beacon beam will appear at the location of the waypoint to make it easier to pinpoint the location of the waypoint.

### Trail

The plugin attempts to find a suitable path for you to walk and then shows it to you with particles.

### Hologram

Floating text like in the mods.

### Blinking Block

When the player is closer to the waypoint, the beacon will be replaced with a block sequence that will repeat. This is to help him get to the exact location.

### Compass

It won't get any simpler than that if the player owns a compass. It will have the waypoint as it's target and points into that direction.

### Actionbar Indicator

This is comparable to the compass but doesn't require one. It will show the rough direction with small sections in the action bar of the player. If the player
is too far left, the sections in the right will light up and vice versa.

### Particles

Particles appear at the feet of the player pointing into the direction of the waypoint

## Player tracking

Although disabled by
default, [when enabled](https://github.com/Sytm/waypoints/blob/616d2f6db741d656edb8f5d98bb2d18f32cbe2ca/waypoints/src/main/resources/config.yml#L120) adds a
menu to the GUI where the player can choose another player to track. Now the compass, actionbar indicator and particles will help guide the player to the
tracked player.

- Players can choose to hide themselves in the menu to others (if enabled)
- Players must show themselves to others in order to track a player (if enabled)
- The player that gets tracked receives a notification who started tracking him (if enabled)

## Languages
The plugin is shipping translations for the following languages:
- English
- German
- Italian (by marcotech81)
- Simplified Chinese (partial, by SnowCutieOwO)
- Brazilian Portuguese (partial, by gaugt980131gg2)

And if your language is not there, the plugin is fully translatable

## Other notable features

- Open the GUI by sneaking and right-clicking with a compass (by default)
- Set custom icons for waypoints and folders by clicking on their icon. The item in your main hand will be used
- Limit the amount of waypoints and folders a player can have
- Customizable teleport prices and price increases the more the player teleports (With Vault support)
- Direction indicators customizable
- Items in GUI fully customizable
- SQLite data storage

## Integrations

- Public waypoints are added to:
  - [Dynmap](https://www.spigotmc.org/resources/dynmap%C2%AE.274/)
  - [SquareMap](https://github.com/jpenilla/squaremap)
  - [BlueMap](https://bluemap.bluecolored.de/)
  - [Pl3xMap](https://modrinth.com/plugin/pl3xmap)

## Videos

DonKolia made a small video series of this plugins functionality where he demonstrates the basic functionality and all the available pointers.
It is in Spanish but with English subtitles: [Link to playlist](https://www.youtube.com/watch?v=mt3lmGMKZV8&list=PLop1Uc5d4Z6pTJIBRwTFumdL7cInrE7fL)

## Commands

`/waypoints` - Opens the GUI

`/waypoints select <Name>` - Select an additional waypoint

`/waypoints deselectAll` - Deselect all selected waypoints

`/waypoints teleport <Name>` - Teleport to waypoints (if allowed)

`/waypoints set <Name>` - Create a waypoint only the player himself can see

`/waypoints setPublic <Name>` - Create a waypoint visible for everyone

`/waypoints setPermission <Permission> <Name>` - Create a waypoint only visible for players with the given permission

`/waypoints setTemporary <X> <Y> <Z>` - Create a waypoint that is only visible for the time he is online

`/waypoints other <Name>` - View the waypoints of another player

`/waypoints statistics` - Look at some rudimentary statistics about the database

`/waypoints reload` - Reload the configuration. (`/reload` **is not supported**)

### Scripting

`/waypointsscript deselectWaypoint <Player-Name>` - Deselect the current waypoints of a player with a command

`/waypointsscript selectWaypoint <Player-Name> <Waypoint-UUID>` - Set the waypoint selection of a player with a command

`/waypointsscript temporaryWaypoint <Player-Name> <X> <Y> <Z> [Beacon-Color]` - Create a temporary waypoint for the player

`/waypointsscript uuid <Search-Query>` - Search for waypoints by their name to get the uuid of them

## Permissions

`waypoints.command.use`* - Allows the player to use the `/waypoints` command and open the GUI

`waypoints.command.other` - Allows the player to use the `/waypoints other` command

`waypoints.command.statistics` - Allows the player to use the `/waypoints statistics` command

`waypoints.command.reload` - Allows the player to use the `/waypoints reload` command

`waypoints.command.scripting` - Allows the player to use the `/waypointsscript` command and get the UUID of waypoints

`waypoints.modify.private`* - Allows the player to create, alter and delete private waypoints

`waypoints.modify.public` - Allows the player to create, alter and delete public waypoints

`waypoints.modify.permission` - Allows the player to create, alter, delete and always see permission waypoints

`waypoints.modify.anywhere` - Allows the player to place waypoints wherever they want, ignoring disabled worlds

`waypoints.unlimited` - Allows the player to create more waypoints and folders than the limit in the config

`waypoints.temporaryWaypoint`* - Allows the player to create a waypoint that is only visible for the time he is online

`waypoints.teleport.private` - Allows the player to teleport to private waypoints

`waypoints.teleport.public` - Allows the player to teleport to public waypoints

`waypoints.teleport.permission` - Allows the player to teleport to permission waypoints

`waypoints.tracking.enabled`* - Allows the player to use the player tracking feature

`waypoints.tracking.trackAll` - Allows the player to track players that are hidden

`waypoints.updateNotification` - The player receives a notification after a server restart if a new Version is found

*: These permissions are granted to all players by default

## Configuration

Almost everything is configurable. To have a look at the current configuration file
click [here](https://github.com/Sytm/waypoints/blob/v4/master/waypoints/src/main/resources/config.yml)

Also, you can change every piece of text to other languages or just change the colors.
[English Translation File](https://github.com/Sytm/waypoints/blob/v4/master/waypoints/src/main/resources/lang/en.yml)

## Additional notes

### Metrics

This plugin uses [bStats](https://bstats.org/) to collect basic metrics about the usage of this plugin. You can, of course, opt-out of this if you wish. For
further information have a look at "What data is collected?"
and "Disabling bStats" over [here](https://bstats.org/getting-started).

To view the metrics of this plugin, go [here](https://bstats.org/plugin/bukkit/waypoints2/6864).
<!-- modrinth_exclude.start -->
### License

This plugin is licensed under the MIT license. For more information on it, you can read [this](https://choosealicense.com/licenses/mit/).

### Supported Minecraft versions

Because I don't have the time or motivation to support a lot of versions, and it is generally advised to update to the latest Minecraft/Spigot versions only the
latest Minecraft version is supported
<!-- modrinth_exclude.end -->
### Java version

Java 17 is the minimum required version.
