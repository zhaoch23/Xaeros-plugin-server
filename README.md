# A Bukkit Server for Xaero's Minimap on 1.12.2 (In Dev)

[中文README](/README_zh_CN.md)

## Installation

### Client-side (Mod)
1. Download and install Xaero's Minimap version 25.2 or higher
2. Place the mod file in your Minecraft mods folder


### Server-side (Plugin)
1. Build the latest release of this plugin
2. Place the plugin JAR file in your Bukkit/Spigot server's `plugins` folder
3. Restart your server or reload the plugins

Note: Make sure both the client and server are running Minecraft version 1.12.2

## Features

- **Server-to-Client Waypoint Synchronization**
  - Automatically sends waypoints from the server to connected clients
  - Ensures all players have access to the same waypoints
  - Real-time updates when waypoints are modified

- **Waypoint Protection**
  - Prevents clients from modifying server-side waypoints
  - Maintains waypoint integrity across all players

- **Transparent Waypoint Background**
  - Support for transparent background waypoints

- **Custom Waypoint Right-Click Options**
  - Define custom interactive options that appear when players right-click on waypoints
  - Execute commands, send messages, or perform custom actions when options are selected
  - Support for different dispatch modes: player commands, server commands, or operator commands
  - Configurable option text and initials for easy identification
  - Integration with PlaceholderAPI for dynamic content in commands and messages

## Custom Waypoint Options

The plugin supports custom right-click options for waypoints, allowing you to create interactive waypoints that can execute commands or perform actions when clicked.

### Option Configuration

Options are defined in YAML files in the `options/` folder. Each option has the following properties:

- **initials**: Short identifier for the option (e.g., "TP" for teleport)
- **text**: Display text shown to players
- **dispatch-mode**: How the command is executed (`player`, `server`, or `op`)
- **onSelect**: List of commands to execute when the option is selected

### Dispatch Modes

- **player**: Commands are executed as the player (requires player permissions)
- **server**: Commands are executed as the server console
- **op**: Commands are executed with temporary operator privileges

### Available Placeholders

Commands support the following placeholders:
- `{{player}}`: Player name
- `{{x}}`, `{{y}}`, `{{z}}`: Waypoint coordinates
- `{{world}}`: World name
- `{{name}}`: Waypoint name
- `{{id}}`: Waypoint ID

### Example Option Configuration

```yaml
# options/teleport.yml
teleport:
  initials: "TP"
  text: "Teleport to waypoint"
  dispatch-mode: "player"
  onSelect:
    - "tp {{player}} {{x}} {{y}} {{z}}"

# options/message.yml
message:
  initials: "MSG"
  text: "Send message"
  dispatch-mode: "server"
  onSelect:
    - "say {{player}} arrived at {{name}}!"

# options/op_command.yml
admin_action:
  initials: "ADM"
  text: "Admin action"
  dispatch-mode: "op"
  onSelect:
    - "gamemode creative {{player}}"
    - "give {{player}} diamond_sword"
```

### Using Options in Waypoints

To add options to a waypoint, include them in the waypoint configuration:

```yaml
# waypoints/custom_world.yml
waypoints:
  id_test:
    name: "Test"
    x: 0
    y: 100
    z: 0
    color: RED
    initials: "TE"
    transparent: true
    hover-text: "Test hover text"
    description: |
      This is a test waypoint.
      It has a description.
      It has a hover text.
      It has a color.
      It has a transparent.
      It has a permissions.
      It has a options.
    options:
      - "teleport"
      - "message"
    permissions:
      - "mypermission_group.node" # Managed by LuckPerms
```

## Commands

The plugin provides several commands for managing waypoints. All commands require the permission `xaerosminimapserver.waypoint`.

### Waypoint Commands

- `/xwp set <world> <x> <y> <z> <id> <name> <color> [initials] [transparent] [refresh]`
  - Sets a new waypoint at the specified coordinates
  - **world**: World name or `~` for current world (when used by a player)
  - **x, y, z**: Coordinates (can be relative using `~` for current position, `~5` for 5 blocks forward, etc.)
  - **id**: Unique identifier for the waypoint
  - **name**: Display name for the waypoint
  - **color**: red, green, blue, yellow, purple, orange, white, black
  - **initials**: Optional initials (defaults to first 2 characters of name)
  - **transparent**: Optional boolean for transparent background (default: false)
  - **refresh**: Optional boolean to send to players immediately (default: true)

- `/xwp remove <world> <id>`
  - Removes a waypoint with the specified ID from the specified world
  - **world**: World name or `~` for current world (when used by a player)
  - **id**: The unique identifier of the waypoint to remove

- `/xwp list`
  - Lists all available waypoints across all worlds

- `/xwp refresh [player|world <name>]`
  - Refreshes waypoints for players
  - Without arguments: refreshes waypoints for all players
  - **player <name>**: refreshes waypoints for a specific player
  - **world <name>**: refreshes waypoints for all players in a specific world

- `/xwp save`
  - Saves all waypoints to the configuration file

- `/xwp reload`
  - Reloads the plugin configuration and waypoints

- `/xwp show <player> <world> <id> <true|false>`
  - Shows or hides a specific waypoint for a player by granting/removing a permission node in LuckPerms
  - **player**: Target player name
  - **world**: World name where the waypoint exists
  - **id**: Waypoint identifier
  - **true|false**: Whether to show (true) or hide (false) the waypoint
  - Note: If the player still has other permission nodes for this waypoint, they will still be able to see it
  - Note: This command does not automatically refresh waypoints - use `/xwp refresh <player>` afterwards to apply changes

- `/xwp send <player> <world>`
  - Sends waypoints from a specific world to a player
  - **player**: Target player name
  - **world**: Logic world name to send waypoints from 

## Example Waypoint
```yaml
# waypoints/custom_world.yml
waypoints:
  id_test:
    name: "Test"
    x: 0
    y: 100
    z: 0
    color: RED
    initials: "TE"
    transparent: true
    hover-text: "Test hover text"
    description: |
      This is a test waypoint.
      It has a description.
      It has a hover text.
      It has a color.
      It has a transparent.
      It has a permissions.
      It has a options.
    options:
      - "teleport"
      - "message"
    permissions:
      - "mypermission_group.node" # Managed by LuckPerms
```