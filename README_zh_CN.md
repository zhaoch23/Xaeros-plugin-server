# Xaero's 小地图 1.12.2 Bukkit 服务器插件（开发中）

## 安装

### 客户端（模组）
1. 下载并安装 Xaero's Minimap 25.2 或更高版本
2. 将模组文件放入你的 Minecraft mods 文件夹

### 服务端（插件）
1. 构建此插件的最新版本
2. 将插件 JAR 文件放入你的 Bukkit/Spigot 服务器的 `plugins` 文件夹
3. 重启服务器或重载插件

注意：确保客户端和服务器都运行 Minecraft 1.12.2 版本

## 功能特性

- **服务器到客户端路径点同步**
  - 自动将路径点从服务器发送到连接的客户端
  - 确保所有玩家都能访问相同的路径点
  - 路径点修改时实时更新

- **路径点保护**
  - 防止客户端修改服务器端路径点
  - 在所有玩家中保持路径点完整性

- **透明路径点背景**
  - 支持透明背景路径点

- **自定义路径点右键选项**
  - 定义玩家右键点击路径点时出现的自定义交互选项
  - 选择选项时执行命令、发送消息或执行自定义操作
  - 支持不同的执行模式：玩家命令、服务器命令或管理员命令
  - 可配置的选项文本和缩写，便于识别
  - 集成 PlaceholderAPI 以在命令和消息中使用动态内容

## 自定义路径点选项

该插件支持路径点的自定义右键选项，允许你创建可以执行命令或执行操作的可交互路径点。

### 选项配置

选项在 `options/` 文件夹的 YAML 文件中定义。每个选项具有以下属性：

- **initials**: 选项的简短标识符（例如，"TP" 表示传送）
- **text**: 向玩家显示的文本
- **dispatch-mode**: 命令的执行方式（`player`、`server` 或 `op`）
- **onSelect**: 选择选项时要执行的命令列表

### 执行模式

- **player**: 命令以玩家身份执行（需要玩家权限）
- **server**: 命令以服务器控制台身份执行
- **op**: 命令以临时管理员权限执行

### 可用占位符

命令支持以下占位符：
- `{{player}}`: 玩家名称
- `{{x}}`、`{{y}}`、`{{z}}`: 路径点坐标
- `{{world}}`: 世界名称
- `{{name}}`: 路径点名称
- `{{id}}`: 路径点 ID

### 选项配置示例

```yaml
# options/teleport.yml
teleport:
  initials: "TP"
  text: "传送到路径点"
  dispatch-mode: "player"
  onSelect:
    - "tp {{player}} {{x}} {{y}} {{z}}"

# options/message.yml
message:
  initials: "MSG"
  text: "发送消息"
  dispatch-mode: "server"
  onSelect:
    - "say {{player}} 到达了 {{name}}！"

# options/op_command.yml
admin_action:
  initials: "ADM"
  text: "管理员操作"
  dispatch-mode: "op"
  onSelect:
    - "gamemode creative {{player}}"
    - "give {{player}} diamond_sword"
```

### 在路径点中使用选项

要在路径点中添加选项，请在路径点配置中包含它们：

```yaml
# waypoints/custom_world.yml
waypoints:
  id_test:
    name: "测试"
    x: 0
    y: 100
    z: 0
    color: RED
    initials: "TE"
    transparent: true
    hover-text: "测试悬停文本"
    description: |
      这是一个测试路径点。
      它有描述。
      它有悬停文本。
      它有颜色。
      它是透明的。
      它有权限。
      它有选项。
    options:
      - "teleport"
      - "message"
    permissions:
      - "mypermission_group.node" # 由 LuckPerms 管理
```

## 命令

该插件提供了多个用于管理路径点的命令。所有命令都需要 `xaerosminimapserver.waypoint` 权限。

### 路径点命令

- `/xwp set <world> <x> <y> <z> <id> <name> <color> [initials] [transparent] [refresh]`
  - 在指定坐标设置新的路径点
  - **world**: 世界名称或 `~` 表示当前世界（玩家使用时）
  - **x, y, z**: 坐标（可以使用 `~` 表示当前位置，`~5` 表示向前5格等相对坐标）
  - **id**: 路径点的唯一标识符
  - **name**: 路径点的显示名称
  - **color**: red, green, blue, yellow, purple, orange, white, black
  - **initials**: 可选的缩写（默认为名称的前2个字符）
  - **transparent**: 可选的透明背景布尔值（默认：false）
  - **refresh**: 可选的立即发送给玩家的布尔值（默认：true）

- `/xwp remove <world> <id>`
  - 从指定世界移除指定 ID 的路径点
  - **world**: 世界名称或 `~` 表示当前世界（玩家使用时）
  - **id**: 要移除的路径点的唯一标识符

- `/xwp list`
  - 列出所有世界中的所有可用路径点

- `/xwp refresh [player|world <name>]`
  - 为玩家刷新路径点
  - 无参数：为所有玩家刷新路径点
  - **player <name>**: 为特定玩家刷新路径点
  - **world <name>**: 为特定世界中的所有玩家刷新路径点

- `/xwp save`
  - 将所有路径点保存到配置文件

- `/xwp reload`
  - 重载插件配置和路径点

- `/xwp show <player> <world> <id> <true|false>`
  - 通过授予/移除 LuckPerms 中的权限节点来显示或隐藏特定玩家的路径点
  - **player**: 目标玩家名称
  - **world**: 路径点存在的世界名称
  - **id**: 路径点标识符
  - **true|false**: 是否显示（true）或隐藏（false）路径点
  - 注意：如果玩家仍然拥有此路径点的其他权限节点，他们仍然可以看到它
  - 注意：此命令不会自动刷新路径点 - 之后使用 `/xwp refresh <player>` 来应用更改

- `/xwp send <player> <world>`
  - 将特定世界的路径点发送给玩家
  - **player**: 目标玩家名称
  - **world**: 要发送路径点的逻辑世界名称

## 路径点示例
```yaml
# waypoints/custom_world.yml
waypoints:
  id_test:
    name: "测试"
    x: 0
    y: 100
    z: 0
    color: RED
    initials: "TE"
    transparent: true
    hover-text: "测试悬停文本"
    description: |
      这是一个测试路径点。
      它有描述。
      它有悬停文本。
      它有颜色。
      它是透明的。
      它有权限。
      它有选项。
    options:
      - "teleport"
      - "message"
``` 