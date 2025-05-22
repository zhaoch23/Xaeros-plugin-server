package com.zhaoch23.xaerosminimapserver.waypoint;

import com.zhaoch23.xaerosminimapserver.XaerosMinimapServer;
import com.zhaoch23.xaerosminimapserver.network.NetworkHandler;
import com.zhaoch23.xaerosminimapserver.network.message.WaypointUpdatePacket;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WaypointManager {

    public final Map<World, Set<Waypoint>> waypoints = new HashMap<>();
    private final XaerosMinimapServer plugin;

    public WaypointManager(XaerosMinimapServer plugin) {
        this.plugin = plugin;
    }

    public Set<Waypoint> getWaypoints(World world) {
        if (world == null) return null;
        return waypoints.computeIfAbsent(world, k -> new HashSet<>());
    }

    public Set<Waypoint> getWaypoints(String worldName) {
        World world = XaerosMinimapServer.plugin.getServer().getWorld(worldName);
        if (world == null) return null;
        return getWaypoints(world);
    }

    public void addWaypoint(World world, Waypoint waypoint, boolean update) {
        assert world != null && waypoint != null;
        Set<Waypoint> waypointList = waypoints.computeIfAbsent(world, k -> new HashSet<>());

        if (waypointList.contains(waypoint)) {
            waypointList.remove(waypoint);
        }
        
        waypointList.add(waypoint);

        if (update) {
            sendWaypointsToPlayers(world);
        }
    }

    public void addWaypoint(String name, String initials, Location location, String color, boolean update) {
        World world = location.getWorld();
        if (world == null) return;
        WaypointColor waypointColor = WaypointColor.fromName(color);
        Waypoint waypoint = new Waypoint(
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                name,
                initials,
                waypointColor
        );
        addWaypoint(world, waypoint, update);
    }

    public void addWaypoint(String name, Location location, String color, boolean update) {
        String initials = "";
        if (name.length() < 2) {
            initials = name.toUpperCase();
        } else {
            initials = name.substring(0, 2).toUpperCase();
        }
        addWaypoint(name, initials, location, color, update);
    }

    public boolean hasWaypoint(World world, String waypoint) {
        Set<Waypoint> waypointList = waypoints.get(world);
        if (waypointList == null) return false;
        return waypointList.stream().anyMatch(w -> w.name.equals(waypoint));
    }
    
    public void removeWaypoint(World world, Waypoint waypoint, boolean update) {
        Set<Waypoint> waypointList = waypoints.get(world);
        if (waypointList != null) {
            waypointList.remove(waypoint);
        }
        if (update) {
            sendWaypointsToPlayers(world);
        }
    }

    public void removeWaypoint(World world, String name, boolean update) {
        Set<Waypoint> waypointList = waypoints.get(world);
        if (waypointList != null) {
            waypointList.removeIf(waypoint -> waypoint.name.equals(name));
        }
        if (update) {
            sendWaypointsToPlayers(world);
        }
    }

    public void clearAll(boolean update) {
        waypoints.clear();
        if (update) {
            sendWaypointsToPlayers();
        }
    }

    public void sendWaypointsToPlayers() {
        NetworkHandler handler = XaerosMinimapServer.getNetworkHandler();
        for (World world : waypoints.keySet()) {
            Set<Waypoint> waypointList = waypoints.get(world);
            if (waypointList == null) continue;
            WaypointUpdatePacket packet = new WaypointUpdatePacket(
                waypointList,
                world.getName()
            );
            for (Player p : world.getPlayers()) {
                handler.sendToPlayer(p, packet);
            }
        }
    }

    public void sendWaypointsToPlayers(World world) {
        NetworkHandler handler = XaerosMinimapServer.getNetworkHandler();
        Set<Waypoint> waypointList = waypoints.get(world);
        if (waypointList == null) return;
        WaypointUpdatePacket packet = new WaypointUpdatePacket(
                waypointList,
                world.getName()
        );
        for (Player p : world.getPlayers()) {
            handler.sendToPlayer(p, packet);
        }
    }

    public void sendWaypointsToPlayer(Player player) {
        NetworkHandler handler = XaerosMinimapServer.getNetworkHandler();
        World world = player.getWorld();
        if (world == null) return;
        Set<Waypoint> waypointList = waypoints.get(world);
        if (waypointList == null) return;
        WaypointUpdatePacket packet = new WaypointUpdatePacket(
                waypointList,
                world.getName()
        );
        handler.sendToPlayer(player, packet);
    }

    public void loadConfig(FileConfiguration config) {
        clearAll(false);

        ConfigurationSection waypoints = config.getConfigurationSection("waypoints");
        if (waypoints == null) {
            plugin.getLogger().severe("Waypoints section not found in config");
            return;
        }
        for (String worldName : waypoints.getKeys(false)) {
            ConfigurationSection waypoint = waypoints.getConfigurationSection(worldName);
            World world = plugin.getServer().getWorld(worldName);
            if (world == null) {
                plugin.getLogger().severe("World " + world + " not found");
                continue;
            }
            for (String name : waypoint.getKeys(false)) {
                try {
                    ConfigurationSection waypointData = waypoint.getConfigurationSection(name);
                    if (waypointData == null) {
                        plugin.getLogger().severe("Waypoint " + name + " not found");
                        continue;
                    }
                    String initials = waypointData.getString("initials");

                    Location location = new Location(
                            world,
                            waypointData.getDouble("x"),
                            waypointData.getDouble("y"),
                            waypointData.getDouble("z")
                    );

                    if (initials == null)
                        addWaypoint(name, location, waypointData.getString("color"), false);
                    else
                        addWaypoint(name, initials, location, waypointData.getString("color"), false);
                } catch (Exception e) {
                    plugin.getLogger().severe("Failed to load " + name + " due to " + e);
                }

            }
        }
        plugin.getLogger().info(prettyPrint());
    }

    public void toConfig(FileConfiguration config) {
        config.set("waypoints", null); // Clear existing waypoints section
        ConfigurationSection configSection = config.createSection("waypoints");
        for (Map.Entry<World, Set<Waypoint>> entry : waypoints.entrySet()) {
            World world = entry.getKey();
            Set<Waypoint> worldWaypoints = entry.getValue();
            ConfigurationSection worldSection = configSection.createSection(world.getName());
            for (Waypoint waypoint : worldWaypoints) {
                ConfigurationSection waypointSection = worldSection.createSection(waypoint.name);
                waypointSection.set("x", waypoint.x);
                waypointSection.set("y", waypoint.y);
                waypointSection.set("z", waypoint.z);
                waypointSection.set("color", waypoint.color.getName());
                waypointSection.set("initials", waypoint.initials);
            }
        }
    }

    /**
     * Prints all waypoints in a readable format
     * @return A formatted string containing all waypoints
     */
    public String prettyPrint() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Waypoints ===\n");
        
        for (Map.Entry<World, Set<Waypoint>> entry : waypoints.entrySet()) {
            World world = entry.getKey();
            Set<Waypoint> worldWaypoints = entry.getValue();
            
            sb.append("\nWorld: ").append(world.getName()).append("\n");
            sb.append("-------------------\n");
            
            if (worldWaypoints.isEmpty()) {
                sb.append("  No waypoints\n");
            } else {
                for (Waypoint waypoint : worldWaypoints) {
                    sb.append(String.format("  %s (%s)\n", waypoint.name, waypoint.initials));
                    sb.append(String.format("    Location: %d, %d, %d\n", 
                        waypoint.x, waypoint.y, waypoint.z));
                    sb.append(String.format("    Color: %s\n", waypoint.color));
                }
            }
        }
        
        return sb.toString();
    }

}
