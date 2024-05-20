# Changelog

## [Unreleased]

### Changed
- When using `/waypoints other <...>` to inspect another players waypoints, the waypoints can now be modified and removed when the player has the permission `waypoints.modify.other`
- Disable plugin remapping on 1.20.6+ servers because this plugin does not utilize NMS

### Fixed
- When having public ownership enabled, the owner is also displayed at the bottom of the waypoint and folder description for private ones

## 4.5.0

### Added
- Allow players to create public waypoints and folders that only the creator can manage. Feature must be manually enabled in config

### Fixed
- Fix NullPointerException with cooldowns when teleportations are enabled

## 4.4.3

### Fixed
- More world checks added to the trail pointer logic to prevent error spam on world switching
- Error spam from the hologram pointer when switching worlds outside the spawn chunks

## 4.4.2

### Fixed
- Error messages get printed into the console when the trail pointer is active and the player switches worlds

## 4.4.1

### Added
- Ability to pay for teleportations with experience points in addition to levels

### Fixed
- Thread check is triggered when a player sees the blinking block and teleports away on Folia [Sytm/quest-pointers#1](https://github.com/Sytm/quest-pointers/issues/1)
- An exception is thrown when using the trail pointer to point to a waypoint in another world [#106](https://github.com/Sytm/waypoints/issues/106)

## 4.4.0

### Added
- Additional limits can be specified that check for permissions to allow for staggered waypoints or folder limits

### Changed
- The movement of the hologram pointer has been smoothed, but the plugin now requires at least 1.20.2
- The seeds for sounds are now set by the plugin instead of the server to prevent them breaking in a future Paper update

### Fixed
- NullPointerException is thrown when trying to edit the icon of something with empty hands

## 4.3.2

### Fixed*
- Anvil text input not working with EcoEnchants or UberEnchant installed

## 4.3.1

### Fixed
- Hologram pointer throwing errors on servers running Paper 1.20.1 and earlier.

## 4.3.0

### Added
- The GUI supports the usage of custom player heads
- Custom model data is stored when changing the icon of a Waypoint or folder
- Custom model data can be set for items in the GUI like this: `DIAMOND|1`

### Changed
- **CommandAPI is no longer bundled with Waypoints, but needs to be downloaded separately from [here](https://modrinth.com/plugin/commandapi)**
  - Now Waypoints itself no longer requires updates between different Minecraft versions at all, only CommandAPI
- Partial update of chinese translations by [SnowCutieOwO](https://github.com/SnowCutieOwO)
- The default config has been updated to replace some items with custom player heads
- Holograms are now multiline by default
- The color of the indicators in the boss bar no longer change every settings change or plugin config reload

### Fixed
- Default colors for beacon beams are not applied

## 4.2.0

### Added
- Support for Minecraft 1.20.2
- Death folders can now be deleted in its entirety
- Temporary waypoints can be set for other players with `/waypoints setTemporary x y z [<players>]`
  - `[<players>]` can either be a player name or a selector like `@a`
  - The permission `waypoints.temporaryWaypoint.others` is required to perform this action, but all players have it by default
  - Players can disable the receiving of temporary waypoints by others in their settings

### Changed
- ProtocolLib is no longer required for hologram pointers
- The titles of the anvil text prompts are now customized
- Updated Vault usage metric to better reflect actual usage

### Fixed
- Hologram pointers work across dimensions changes
- Hologram pointers not showing the distance to the target when in a connected world
- Connected worlds not working by default. To apply this fix to existing users:
  - Either regenerate the config.yml
  - Or update `pointers.connectedWorlds.world` from `world_the_nether` to `world_nether`

## 4.1.0

### Added
- Added feature flag for teleportations at `general.features.teleportation`
- Added player tracking requests [#102](https://github.com/Sytm/waypoints/issues/102)
- Added Italian translations provided by marcotech81

### Fixed
- Fixed another incorrect translation mapping

### Removed
- Java interoperability helpers for API

## 4.0.2

### Fixed
- Error when opening the inventory while another player is selected [#101](https://github.com/Sytm/waypoints/issues/101)

## 4.0.1

### Added
- Java interoperability helpers have been added to the API Waypoints provides to allow non-Kotlin plugins to use it

### Fixed
- Plugin not properly loading on Minecraft 1.19.4

### Changed
- SignGUI for description editing uses newer API which has been added in 1.20.1

## 4.0.0
A lot of new features but also a lot of internal changes that leverage new Paper APIs that aren't available on Spigot.
Before updating please make an update of your database to make sure nothing goes missing in case of unforeseen errors.
I have tested this plugin in the current state for quite some time and it worked without issues for me, but if you encounter any
please report them on the GitHub issue tracker or on the Discord server.

### Breaking changes

- Support for Spigot has been dropped, [Paper](https://papermc.io/software/paper) is required
- Only latest Minecraft version is officially supported, although the minimum Version is 1.19.4
- Translation files are now formatted with [MiniMessage](https://docs.advntr.dev/minimessage/format.html) instead of legacy color codes
- Database format has been updated and is not backwards compatible
- When using `/waypointsscript selectWaypoint` it is now mandatory to run `/waypointscript deselectWaypoint` just before if only one waypoint should be active at the same time

### Added

- Command suggestion tooltips are now customizable via the translation files at `command.search.tooltip`
- Update checker that notifies the console on startup and admins once after they first join. Can be disabled in the config at `general.updateChecker`. Does not run periodically
- Database accesses are now completely asynchronous to the main server thread
- Add support for [Folia](https://papermc.io/software/folia)
- Multiple Waypoints can be selected at the same time [#92](https://github.com/Sytm/waypoints/issues/92)
- Integration to [Pl3xMap](https://modrinth.com/plugin/pl3xmap) has been added
- Descriptions have been added to waypoints and folders. To change them [ProtocolLib](https://github.com/dmulloy2/ProtocolLib) needs to be installed
- A Particle Trail pointer has been added
- Players can choose to disable some of the server-wide available pointers (For example only having the bossbar)
- Players can share private waypoints with other players
- Sounds have been added to the GUI and some actions. They can be customized in the config at `sounds`

### Changed
- Command help messages use used alias again
- The Hologram Pointer uses display entities instead of ArmorStands [#93](https://github.com/Sytm/waypoints/issues/93)
- Added extra item in the waypoint gui page for changing the icon of waypoints
- Reloading has always been discouraged but is now explicitly unsupported

### Removed
- Compass pointer nether support has been removed, because any possible implementation is inherently inconsistent

## 3.6.6
This is _the last_ v3 release. v3 is **END OF LIFE**.

All further updates (like v4) will only be available on [Modrinth](https://modrinth.com/plugin/waypoints)

### Added
- Support for Minecraft 1.20.2

## 3.6.5

### Added
- Support for Minecraft 1.20.1

## 3.6.4
Note on future versions:

v3 will only receive updates until Minecraft 1.21 (so basically 1.20.1, ...) but no more after that.

v4 will be released soon with serval enhancements which require the use of Paper due to required additional APIs that Spigot does not provide and overall better performance,
so if you haven't switched yet, [now would be the time to do so](https://papermc.io/software/paper). (Paper is not yet updated to 1.20)

### Added
- Support for Minecraft 1.20

## 3.6.3

### Added
- Option to set custom command aliases in case other plugins overwrite built-in Waypoints commands

## 3.6.2

### Added
- Subcommands for `/waypoints`: `select`, `deselect`, `teleport` [#91](https://github.com/Sytm/waypoints/issues/91)
- Subcommand for `/waypointsscript`: `uuid`

### Fixed
- Possible console spam for missing translation keys
- Bug that items in GUI Overview or Folders appear twice

### Changed
- Commands are now implemented as native Brigadier commands

## 3.6.1

### Added
- Support for Minecraft 1.19.4

## 3.6.0

### Added
- Beneath the hologram the icon of the waypoint is displayed [#88](https://github.com/Sytm/waypoints/issues/88)

### Changed
- The hologram now no longer stands on top of the waypoint, but behaves like in Minimap mods and guides you towards the Waypoint [#87](https://github.com/Sytm/waypoints/issues/87)

### Fixed
- Pointers still work even if the player is in the wrong dimension [#90](https://github.com/Sytm/waypoints/issues/90)

## 3.5.9

### Fixed
- Holograms not working in Minecraft 1.19 with ProtocolLib 5 [#84](https://github.com/Sytm/waypoints/issues/84)

## 3.5.8

### Added
- Option to allow teleportation to a waypoint only if it has been visited before
- `/waypointsscript temporaryWaypoint` command (with optional beacon color)

## 3.5.7

### Added
- BossBar pointer
- BlueMap integration [#83](https://github.com/Sytm/waypoints/issues/83)

### Changed
- ActionBar pointer is now disabled by default
- The cost of teleportations can now be either per single waypoint or per waypoint type

## 3.5.6

### Added
- Option to prevent creation of waypoints in specified worlds [#82](https://github.com/Sytm/waypoints/issues/82)

## 3.5.5

### Added
- Support for Minecraft 1.19.3
- Option to disable DynMap and SquareMap integrations [#75](https://github.com/Sytm/waypoints/issues/75)
- Simplified Chinese by SnowCutieOwO
- Brazilian Portuguese (incomplete) by gaugt980131gg2

### Fixed
- Prevent log-spam because ProtocolLib does not support Minecraft 1.19 properly yet [#79](https://github.com/Sytm/waypoints/issues/79)

## 3.5.4

### Added
- Support for Minecraft 1.19.1
- SquareMap integration [#71](https://github.com/Sytm/waypoints/issues/71)
- Option to automatically select last death waypoint [#72](https://github.com/Sytm/waypoints/issues/72)
- Option to allow players to teleport only to the last waypoint they died at. Cooldown and costs still apply

### Fixed
- Add out-of-bounds check before creation of waypoints [#73](https://github.com/Sytm/waypoints/issues/73)
- Beacons are placed differently again to make the beacon beam actually disappear

## 3.5.3

### Added
- The configuration can be reloaded with `/waypoints reload`

### Fixed
- Every beacon beam has the same color

## 3.5.2

### Changed
- Beacon beam now spawns as low as possible thus working in the nether and looking better in the Overworld because the beacon plus base is now hidden
- New players are no longer trackable by default

## 3.5.1

### Added
- Option to change the color for temporary waypoints and player tracking (They cannot be changed individually)

### Changed
- Made the Waypoint(De)SelectEvent more generic into Trackable(De)SelectEvent

## 3.5.0

### Added
- Possibility to set custom DynMap icons per waypoint
- `/waypoints setTemporary <X> <Y> <Z>` command to create waypoints that are not saved
- More bStats metrics: Used with DynMap, Vault, ProtocolLib; Enabled: global waypoints, death waypoints, player tracking

### Fixed
- Actually check if the player has the permission to use player tracking

## 3.4.1
Support for Minecraft 1.16 has been dropped and Java version 16 is now the minimum

### Added
- Support for Minecraft 1.19

## 3.4.0

### Added
- Player tracking as an optional feature
- The last selected waypoint is saved and selected again when rejoining the server
- DynMap integration

## 3.3.3

### Added
- Holograms
- Text file is generated on legacy import containing mappings from old UUIDs to new UUIDs

### Fixed
- Issue that prevented import of old waypoints

## 3.3.2

### Added
- Support for Minecraft 1.18.2

### Removed
- DynMap integration
- Holograms

## 3.3.1

### Added
- Option for what to do with waypoints in deleted worlds
  - Show the waypoint, but it cannot be selected or teleported to
  - Hide the waypoint
  - Delete the waypoint

### Fixed
- Error when a waypoint is in a deleted world

## 3.3.0

### Added
- When sneaking and the action bar pointer is enabled, the distance to the waypoint is shown instead of the direction indicator
- Overworld/Nether location mapping to point to the translated locations instead
- Hologram pointer. This requires ProtocolLib. The hologram will show a hologram above the waypoint if the player is close enough
- Feature toggle for death waypoints. This will disable the creation of new death waypoints and hides them in the GUI
- Database cleaning procedure that periodically purges the database of old death waypoints. By default if they are 7 days old

## 3.2.1

### Fixed
- DynMap integration preventing the plugin to load

## 3.2.0

### Added
- DynMap integration. Public waypoints appear as markers. Icon and name of marker set can be configured
- Existing waypoints that are not death waypoint can be converted to public / permission waypoints
- Waypoints can be created from coordinates in the format 1/2/3 (x/y/z) with the world the player is in being used
- The permission of an existing permission waypoint can be viewed and changed

### Changed
- The plugin is no longer shading Kotlin, but using the library loader added in late 1.16.5 Spigot

## 3.1.1

### Added
- Support for Minecraft 1.18

## 3.1.0

### Added
- Option `general.features.globalWaypoints` which can be set to `false` to disable global waypoints

### Changed
- Public / Permission folders are hidden when empty
  - They are still visible if one has the permission `waypoints.modify.public` or `waypoints.modify.permission` respectively
- The default item for the global waypoints toggle in the visible state is `ENDER_CHEST`

### Fixed
- Language fallback not being applied when selected language has missing keys
- Compass resets to possibly wrong location
  - The compass would always reset to the location the player had as a compass target when he first used a waypoint

## 3.0.1

### Fixed
- Legacy importer failing if waypoints exist in worlds that no longer exist
- Legacy importer failing if a player or the global store file is malformed

## 3.0.0

### Changed
- Complete rewrite of the plugin in Kotlin
- Using SQLite as datastore instead of NBT
