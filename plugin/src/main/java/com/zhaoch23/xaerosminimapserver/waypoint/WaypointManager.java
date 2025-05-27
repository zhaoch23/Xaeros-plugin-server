package com.zhaoch23.xaerosminimapserver.waypoint;

import com.zhaoch23.xaerosminimapserver.XaerosMinimapServer;
import com.zhaoch23.xaerosminimapserver.network.NetworkHandler;
import com.zhaoch23.xaerosminimapserver.network.message.WaypointUpdatePacket;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class WaypointManager {

    public static final String PERMISSION_NODE_PREFIX = "xaerosminimapwaypoint.";
    private static String adminPermission = "xaerosminimapwaypoint.admin";
    public final Map<World, Map<String, Waypoint>> waypoints = new ConcurrentHashMap<>();
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

    public static String getPermissionNode(World world, String id) {
        return PERMISSION_NODE_PREFIX + world.getName() + "." + id;
    }

    public Map<String, Waypoint> getWaypoints(World world) {
        if (world == null) return null;
        return waypoints.computeIfAbsent(world, k -> new ConcurrentHashMap<>());
    }

    public Map<String, Waypoint> getWaypoints(String worldName) {
        World world = XaerosMinimapServer.plugin.getServer().getWorld(worldName);
        if (world == null) return null;
        return getWaypoints(world);
    }

    public void addWaypoint(World world, String id, Waypoint waypoint, boolean refresh) {
        assert world != null && id != null;
        Map<String, Waypoint> waypointMap = waypoints.computeIfAbsent(world, k -> new ConcurrentHashMap<>());
        waypointMap.put(id, waypoint);

        if (refresh) {
            sendWaypointsToPlayers(world);
        }
    }

    public void addWaypoint(
            String id,
            String name,
            String initials,
            Location location,
            String color,
            boolean transparent,
            Set<String> permissions,
            boolean refresh
    ) {
        World world = location.getWorld();
        if (world == null) return;
        WaypointColor waypointColor = WaypointColor.fromName(color);
        Waypoint waypoint = new Waypoint(
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                name,
                initials,
                waypointColor,
                transparent,
                permissions
        );
        waypoint.permissions.add(getPermissionNode(world, id));
        waypoint.permissions.add(adminPermission);
        addWaypoint(world, id, waypoint, refresh);
    }

    public void addWaypoint(String id, String name, Location location, String color, boolean transparent, Set<String> permissions, boolean refresh) {
        String initials = "";
        if (name.length() < 2) {
            initials = name.toUpperCase();
        } else {
            initials = name.substring(0, 2).toUpperCase();
        }
        addWaypoint(id, name, initials, location, color, transparent, permissions, refresh);
    }

    public boolean hasWaypoint(World world, String id) {
        Map<String, Waypoint> waypointList = waypoints.get(world);
        if (waypointList == null) return false;
        return waypointList.containsKey(id);
    }

    public void removeWaypoint(World world, String id, boolean refresh) {
        Map<String, Waypoint> waypointList = waypoints.get(world);
        if (waypointList != null) {
            waypointList.remove(id);
        }
        if (refresh) {
            sendWaypointsToPlayers(world);
        }
    }

    public void clearAll(boolean refresh) {
        waypoints.clear();
        if (refresh) {
            sendWaypointsToPlayers();
        }
    }

    public void sendWaypointsToPlayers() {
        for (World world : plugin.getServer().getWorlds()) {
            sendWaypointsToPlayers(world);
        }
    }

    public void sendWaypointsToPlayers(World world) {
        NetworkHandler handler = XaerosMinimapServer.getNetworkHandler();
        Map<String, Waypoint> waypointList = waypoints.computeIfAbsent(world, k -> new ConcurrentHashMap<>());
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
        List<Waypoint> waypointList = waypoints.computeIfAbsent(world, k -> new ConcurrentHashMap<>())
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
        if (!waypoints.computeIfAbsent(world, k -> new ConcurrentHashMap<>()).containsKey(id)) {
            plugin.getLogger().severe("Waypoint " + id + " does not exist in world " + world.getName());
            return;
        }
        CompletableFuture<User> user = luckPerms.getUserManager().loadUser(player.getUniqueId());
        final Node node = Node.builder(getPermissionNode(world, id))
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
        if (!waypoints.computeIfAbsent(world, k -> new ConcurrentHashMap<>()).containsKey(id)) {
            plugin.getLogger().severe("Waypoint " + id + " does not exist in world " + world.getName());
            return;
        }

        CompletableFuture<User> user = luckPerms.getUserManager().loadUser(player.getUniqueId());
        final Node node = Node.builder(getPermissionNode(world, id))
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
        if (user == null || world == null || id == null) return false;
        CachedPermissionData permissionData = user.getCachedData().getPermissionData();
        for (String permission : waypoints.get(world).get(id).permissions) {
            if (permissionData.checkPermission(permission).asBoolean()) {
                return true;
            }
        }
        return false;
    }

    public boolean canSeeWaypoint(Player player, World world, String id) {
        User user = luckPerms.getUserManager().loadUser(player.getUniqueId()).join();
        return canSeeWaypoint(user, world, id);
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
            World world = plugin.getServer().getWorld(worldName);
            if (world == null) {
                plugin.getLogger().severe("World " + worldName + " not found");
                continue;
            }
            for (String id : waypoint.getKeys(false)) {
                try {
                    ConfigurationSection waypointData = waypoint.getConfigurationSection(id);
                    if (waypointData == null) {
                        plugin.getLogger().severe("Waypoint " + id + " not found");
                        continue;
                    }
                    String initials = waypointData.getString("initials");
                    boolean transparent = waypointData.getBoolean("transparent", false);

                    Location location = new Location(
                            world,
                            waypointData.getDouble("x"),
                            waypointData.getDouble("y"),
                            waypointData.getDouble("z")
                    );
                    String name = waypointData.getString("name", id);
                    Set<String> permissions = new HashSet<>(waypointData.getStringList("permissions"));

                    if (initials == null)
                        addWaypoint(id, name, location, waypointData.getString("color"), transparent, permissions, false);
                    else
                        addWaypoint(id, name, initials, location, waypointData.getString("color"), transparent, permissions, false);
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
        for (Map.Entry<World, Map<String, Waypoint>> entry : waypoints.entrySet()) {
            World world = entry.getKey();
            Map<String, Waypoint> worldWaypoints = entry.getValue();
            ConfigurationSection worldSection = configSection.createSection(world.getName());
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

        for (Map.Entry<World, Map<String, Waypoint>> entry : waypoints.entrySet()) {
            World world = entry.getKey();
            Map<String, Waypoint> worldWaypoints = entry.getValue();

            sb.append("\nWorld: ").append(world.getName()).append("\n");
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
