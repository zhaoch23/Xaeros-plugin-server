package com.zhaoch23.xaerosminimapserver.waypoint;

import com.zhaoch23.xaerosminimapserver.XaerosMinimapServer;
import com.zhaoch23.xaerosminimapserver.network.NetworkHandler;
import com.zhaoch23.xaerosminimapserver.network.message.WaypointUpdatePacket;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class WaypointManager {

    public static final String PERMISSION_NODE_PREFIX = "xaerosminimapwaypoint.";
    private static String adminPermission = "xaerosminimapwaypoint.admin";
    public final Map<String, Map<String, Waypoint>> waypoints = new ConcurrentHashMap<>();
    private final XaerosMinimapServer plugin;
    private final LuckPerms luckPerms;

    public WaypointManager(XaerosMinimapServer plugin) {
        this.plugin = plugin;
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = provider.getProvider();
        } else {
            throw new RuntimeException("LuckPerms not found");
        }
    }

    public static String getPermissionNode(String worldName, String id) {
        return PERMISSION_NODE_PREFIX + worldName + "." + id;
    }

    public Set<String> getWorlds() {
        return waypoints.keySet();
    }

    public Map<String, Waypoint> getWaypoints(World world) {
        if (world == null) return null;
        return getWaypoints(world.getName());
    }

    public Map<String, Waypoint> getWaypoints(String worldName) {
        return waypoints.computeIfAbsent(worldName, k -> new ConcurrentHashMap<>());
    }

    public void addWaypoint(String worldName, String id, Waypoint waypoint, boolean refresh) {
        Map<String, Waypoint> waypointMap = waypoints.computeIfAbsent(worldName, k -> new ConcurrentHashMap<>());
        waypointMap.put(id, waypoint);

        if (refresh) {
            sendWaypointsToPlayers(worldName);
        }
    }

    public void addWaypoint(
            String id,
            String name,
            String initials,
            String worldName,
            int x,
            int y,
            int z,
            String color,
            boolean transparent,
            Set<String> permissions,
            String description,
            boolean refresh
    ) {
        WaypointColor waypointColor = WaypointColor.fromName(color);
        Waypoint waypoint = new Waypoint(
                x,
                y,
                z,
                name,
                initials,
                waypointColor,
                transparent,
                permissions,
                description
        );
        waypoint.permissions.add(getPermissionNode(worldName, id));
        waypoint.permissions.add(adminPermission);
        addWaypoint(worldName, id, waypoint, refresh);
    }

    public void addWaypoint(
            String id,
            String name,
            String worldName,
            int x,
            int y,
            int z,
            String color,
            boolean transparent,
            Set<String> permissions,
            String description,
            boolean refresh
    ) {
        String initials = "";
        if (name.length() < 2) {
            initials = name.toUpperCase();
        } else {
            initials = name.substring(0, 2).toUpperCase();
        }
        addWaypoint(id, name, initials, worldName, x, y, z, color, transparent, permissions, description, refresh);
    }

    public boolean hasWaypoint(World world, String id) {
        return hasWaypoint(world.getName(), id);
    }

    public boolean hasWaypoint(String worldName, String id) {
        Map<String, Waypoint> waypointList = waypoints.get(worldName);
        if (waypointList == null) return false;
        return waypointList.containsKey(id);
    }

    public void removeWaypoint(World world, String id, boolean refresh) {
        removeWaypoint(world.getName(), id, refresh);
    }

    public void removeWaypoint(String worldName, String id, boolean refresh) {
        Map<String, Waypoint> waypointList = waypoints.get(worldName);
        if (waypointList != null) {
            waypointList.remove(id);
        }
        if (refresh) {
            sendWaypointsToPlayers(worldName);
        }
    }

    public void clearAll(boolean refresh) {
        waypoints.clear();
        if (refresh) {
            sendWaypointsToPlayers();
        }
    }

    public void sendWaypointSetsToPlayers(List<String> worldNames, List<Player> players) {
        List<Waypoint> waypoints = new ArrayList<>();
        for (String worldName : worldNames) {
            waypoints.addAll(getWaypoints(worldName).values());
        }
        StringBuilder worldNamesString = new StringBuilder();
        for (String worldName : worldNames) {
            worldNamesString.append(worldName).append(",");
        }
        WaypointUpdatePacket packet = new WaypointUpdatePacket(waypoints, worldNamesString.toString());
        for (Player player : players) {
            XaerosMinimapServer.getNetworkHandler().sendToPlayer(player, packet);
        }
    }

    public void sendWaypointsToPlayers() {
        for (String worldName : waypoints.keySet()) {
            sendWaypointsToPlayers(worldName);
        }
    }

    public void sendWaypointsToPlayers(String worldName) {
        List<Player> players = Bukkit.getOnlinePlayers().stream().filter(player -> player.getWorld().getName().equals(worldName)).collect(Collectors.toList());
        for (Player player : players) {
            sendWaypointsToPlayer(player);
        }
    }

    public void sendWaypointsToPlayers(World world) {
        NetworkHandler handler = XaerosMinimapServer.getNetworkHandler();
        Map<String, Waypoint> waypointList = getWaypoints(world.getName());
        if (waypointList.isEmpty()) return; // No waypoints to send

        for (Player player : world.getPlayers()) {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user == null) continue;
            List<Waypoint> visibleWaypoints = waypointList.entrySet()
                    .stream()
                    .filter(entry -> canSeeWaypoint(user, world, entry.getKey()))
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());
            WaypointUpdatePacket packet = new WaypointUpdatePacket(
                    visibleWaypoints,
                    world.getName()
            );
            handler.sendToPlayer(player, packet);
        }
    }

    public void sendWaypointsToPlayer(Player player) {
        NetworkHandler handler = XaerosMinimapServer.getNetworkHandler();
        World world = player.getWorld();
        if (world == null) return;
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) return;
        List<Waypoint> waypointList = getWaypoints(world.getName())
                .entrySet()
                .stream()
                .filter(entry -> canSeeWaypoint(user, world, entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
        WaypointUpdatePacket packet = new WaypointUpdatePacket(
                waypointList,
                world.getName()
        );
        handler.sendToPlayer(player, packet);
    }

    public void grantWaypoint(Player player, World world, String id) {
        assert player != null && world != null && id != null;
        grantWaypoint(player, world.getName(), id);
    }

    public void grantWaypoint(Player player, String worldName, String id) {
        assert player != null && worldName != null && id != null;
        if (!waypoints.computeIfAbsent(worldName, k -> new ConcurrentHashMap<>()).containsKey(id)) {
            plugin.getLogger().severe("Waypoint " + id + " does not exist in world " + worldName);
            return;
        }
        CompletableFuture<User> user = luckPerms.getUserManager().loadUser(player.getUniqueId());
        final Node node = Node.builder(getPermissionNode(worldName, id))
                .value(true)
                .build();
        user.thenAcceptAsync(
                u -> {
                    u.data().add(node);
                    luckPerms.getUserManager().saveUser(u);
                    plugin.getLogger().info(
                            "Granted waypoint permission " + node.getKey() + " to " + player.getName()
                    );
                }
        );
    }

    public void revokeWaypoint(Player player, World world, String id) {
        assert player != null && world != null && id != null;
        revokeWaypoint(player, world.getName(), id);
    }

    public void revokeWaypoint(Player player, String worldName, String id) {
        assert player != null && worldName != null && id != null;
        if (!waypoints.computeIfAbsent(worldName, k -> new ConcurrentHashMap<>()).containsKey(id)) {
            plugin.getLogger().severe("Waypoint " + id + " does not exist in world " + worldName);
            return;
        }

        CompletableFuture<User> user = luckPerms.getUserManager().loadUser(player.getUniqueId());
        final Node node = Node.builder(getPermissionNode(worldName, id))
                .value(true)
                .build();
        user.thenAcceptAsync(
                u -> {
                    u.data().remove(node); // Remove the node from the user's data
                    luckPerms.getUserManager().saveUser(u);
                    plugin.getLogger().info(
                            "Revoked waypoint permission " + node.getKey() + " from " + player.getName()
                    );
                }
        );
    }

    public boolean canSeeWaypoint(User user, World world, String id) {
        return canSeeWaypoint(user, world.getName(), id);
    }

    public boolean canSeeWaypoint(User user, String worldName, String id) {
        if (user == null || worldName == null || id == null) return false;
        CachedPermissionData permissionData = user.getCachedData().getPermissionData();
        for (String permission : waypoints.get(worldName).get(id).permissions) {
            if (permissionData.checkPermission(permission).asBoolean()) {
                return true;
            }
        }
        return false;
    }

    public boolean canSeeWaypoint(Player player, World world, String id) {
        return canSeeWaypoint(player, world.getName(), id);
    }

    public boolean canSeeWaypoint(Player player, String worldName, String id) {
        User user = luckPerms.getUserManager().loadUser(player.getUniqueId()).join();
        return canSeeWaypoint(user, worldName, id);
    }

    public void loadConfig(FileConfiguration config) {
        clearAll(false);

        adminPermission = config.getString("admin.admin-permission", adminPermission);

        ConfigurationSection waypoints = config.getConfigurationSection("waypoints");
        if (waypoints == null) {
            plugin.getLogger().severe("Waypoints section not found in config");
            return;
        }
        for (String worldName : waypoints.getKeys(false)) {
            ConfigurationSection waypoint = waypoints.getConfigurationSection(worldName);
            for (String id : waypoint.getKeys(false)) {
                try {
                    ConfigurationSection waypointData = waypoint.getConfigurationSection(id);
                    if (waypointData == null) {
                        plugin.getLogger().severe("Waypoint " + id + " not found");
                        continue;
                    }
                    String initials = waypointData.getString("initials");
                    boolean transparent = waypointData.getBoolean("transparent", false);

                    int x = waypointData.getInt("x");
                    int y = waypointData.getInt("y");
                    int z = waypointData.getInt("z");
                    String name = waypointData.getString("name", id);
                    String description = waypointData.getString("description", "");
                    Set<String> permissions = new HashSet<>(waypointData.getStringList("permissions"));

                    if (initials == null)
                        addWaypoint(id, name, worldName, x, y, z, waypointData.getString("color"), transparent, permissions, description, false);
                    else
                        addWaypoint(id, name, initials, worldName, x, y, z, waypointData.getString("color"), transparent, permissions, description, false);
                } catch (Exception e) {
                    plugin.getLogger().severe("Failed to load " + id + " due to " + e);
                }
            }
        }
        plugin.getLogger().info(prettyPrint());
    }

    public void toConfig(FileConfiguration config) {
        config.set("admin.admin-permission", adminPermission);

        config.set("waypoints", null); // Clear existing waypoints section

        ConfigurationSection configSection = config.createSection("waypoints");
        for (Map.Entry<String, Map<String, Waypoint>> entry : waypoints.entrySet()) {
            String worldName = entry.getKey();
            Map<String, Waypoint> worldWaypoints = entry.getValue();
            ConfigurationSection worldSection = configSection.createSection(worldName);
            for (Map.Entry<String, Waypoint> waypoint : worldWaypoints.entrySet()) {
                Waypoint waypointData = waypoint.getValue();
                ConfigurationSection waypointSection = worldSection.createSection(waypoint.getKey());
                waypointSection.set("x", waypointData.x);
                waypointSection.set("y", waypointData.y);
                waypointSection.set("z", waypointData.z);
                waypointSection.set("name", waypointData.name);
                waypointSection.set("color", waypointData.color.getName());
                waypointSection.set("initials", waypointData.initials);
                waypointSection.set("transparent", waypointData.transparent);
                waypointSection.set("permissions", waypointData.permissions);
                waypointSection.set("description", waypointData.description);
            }
        }
    }

    /**
     * Prints all waypoints in a readable format
     *
     * @return A formatted string containing all waypoints
     */
    public String prettyPrint() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Waypoints ===\n");

        for (Map.Entry<String, Map<String, Waypoint>> entry : waypoints.entrySet()) {
            String worldName = entry.getKey();
            Map<String, Waypoint> worldWaypoints = entry.getValue();

            sb.append("\nWorld: ").append(worldName).append("\n");
            sb.append("-------------------\n");

            if (worldWaypoints.isEmpty()) {
                sb.append("  No waypoints\n");
            } else {
                for (Map.Entry<String, Waypoint> waypoint : worldWaypoints.entrySet()) {
                    Waypoint waypointData = waypoint.getValue();
                    sb.append(String.format("  %s (%s) (%s)\n", waypoint.getKey(), waypointData.name, waypointData.initials));
                    sb.append(String.format("    Location: %d, %d, %d\n",
                            waypointData.x, waypointData.y, waypointData.z));
                    sb.append(String.format("    Color: %s\n", waypointData.color));
                    sb.append(String.format("    Transparent: %s\n", waypointData.transparent));
                    sb.append(String.format("    Permissions: %s\n", waypointData.permissions));
                }
            }
        }

        return sb.toString();
    }
}
