# A Bukkit Server for Xaero's Minimap on 1.12.2 (In Dev)

## Installation

### Client-side (Mod)
1. Download and install Xaero's Minimap version 25.2 or higher
2. Place the mod file in your Minecraft mods folder


### Server-side (Plugin)
1. Build the latest release of this plugin
2. Place the plugin JAR file in your Bukkit/Spigot server's `plugins` folder
3. Restart your server or reload the plugins

Note: Make sure both the client and server are running Minecraft version 1.12.2

## Commands

The plugin provides several commands for managing waypoints. All commands require the permission `xaerosminimapserver.waypoint`.

### Waypoint Commands

- `/xwp set <x> <y> <z> <name> <color> [initials] [update]`
  - Sets a new waypoint at the specified coordinates
  - Colors: red, green, blue, yellow, purple, orange, white, black
  - Coordinates can be relative using `~` (e.g., `~` for current position, `~5` for 5 blocks forward)
  - Optional initials for the waypoint
  - Optional update parameter (true/false) to send to players

- `/xwp remove <name>`
  - Removes a waypoint with the specified name

- `/xwp list`
  - Lists all available waypoints

- `/xwp update`
  - Sends updated waypoints to players

- `/xwp save`
  - Saves all waypoints to the configuration file

- `/xwp reload`
  - Reloads waypoints from the configuration file
