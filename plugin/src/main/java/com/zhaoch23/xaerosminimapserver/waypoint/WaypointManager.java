package com.zhaoch23.xaerosminimapserver.waypoint;

import com.zhaoch23.xaerosminimapserver.XaerosMinimapServer;
import com.zhaoch23.xaerosminimapserver.network.message.WaypointUpdatePacket;
import com.zhaoch23.xaerosminimapserver.waypoint.option.OptionManager;
import com.zhaoch23.xaerosminimapserver.waypoint.option.WaypointOption;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class WaypointManager {

    public static final String PERMISSION_NODE_PREFIX = "xaerosminimapwaypoint.";
    public final Map<String, Map<String, Waypoint>> waypoints = new ConcurrentHashMap<>();
    public final OptionManager optionManager;

    private final Map<UUID, Map<String, Waypoint>> playerWaypoints = new ConcurrentHashMap<>();
    private final XaerosMinimapServer plugin;
    private final LuckPerms luckPerms;

    public WaypointManager(XaerosMinimapServer plugin) {
        this.plugin = plugin;
        optionManager = new OptionManager(plugin);
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
            String hoverText,
            String worldName,
            int x,
            int y,
            int z,
            String color,
            boolean transparent,
            Set<String> permissions,
            String description,
            List<WaypointOption> options,
            boolean refresh
    ) {
        WaypointColor waypointColor = WaypointColor.fromName(color);
        Waypoint waypoint = new Waypoint(
                x,
                y,
                z,
                name,
                initials,
                hoverText,
                waypointColor,
                transparent,
                permissions,
                description,
                options
        );
        waypoint.permissions.add(getPermissionNode(worldName, id));
        waypoint.permissions.add(XaerosMinimapServer.getSetting().getAdminPermission());
        addWaypoint(worldName, id, waypoint, refresh);
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

    private void sendWaypointSetsToPlayer(Player player, WaypointUpdatePacket packet) {
        playerWaypoints.put(player.getUniqueId(), packet.getWaypoints());
        XaerosMinimapServer.getNetworkHandler().sendToPlayer(player, packet);
    }

    public void sendWaypointSetsToPlayers(List<String> worldNames, List<Player> players) {
        Map<String, Waypoint> waypointMap = new HashMap<>();
        for (String worldName : worldNames) {
            waypointMap.putAll(getWaypoints(worldName));
        }
        WaypointUpdatePacket packet = new WaypointUpdatePacket(waypointMap, String.join(",", worldNames));
        for (Player player : players) {
            sendWaypointSetsToPlayer(player, packet);
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
        Map<String, Waypoint> waypointList = getWaypoints(world.getName());
        if (waypointList.isEmpty()) return; // No waypoints to send

        for (Player player : world.getPlayers()) {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user == null) continue;
            Map<String, Waypoint> visibleWaypoints = waypointList.entrySet()
                    .stream()
                    .filter(entry -> canSeeWaypoint(user, world, entry.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            WaypointUpdatePacket packet = new WaypointUpdatePacket(
                    visibleWaypoints,
                    world.getName()
            );
            sendWaypointSetsToPlayer(player, packet);
        }
    }

    public void sendWaypointsToPlayer(Player player) {
        World world = player.getWorld();
        if (world == null) return;
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) return;
        Map<String, Waypoint> waypointList = getWaypoints(world.getName())
                .entrySet()
                .stream()
                .filter(entry -> canSeeWaypoint(user, world, entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        WaypointUpdatePacket packet = new WaypointUpdatePacket(
                waypointList,
                world.getName()
        );
        sendWaypointSetsToPlayer(player, packet);
    }

    public void sendWaypointsToPlayer(Player player, Map<String, Waypoint> waypointMap) {
        WaypointUpdatePacket packet = new WaypointUpdatePacket(waypointMap, player.getWorld().getName());
        sendWaypointSetsToPlayer(player, packet);
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

    public boolean canSeeWaypoint(Player player, Waypoint waypoint) {
        User user = luckPerms.getUserManager().loadUser(player.getUniqueId()).join();
        CachedPermissionData permissionData = user.getCachedData().getPermissionData();
        for (String permission : waypoint.permissions) {
            if (permissionData.checkPermission(permission).asBoolean()) {
                return true;
            }
        }
        return false;
    }

    public void refreshWaypoints(Player player) {
        Map<String, Waypoint> pw = playerWaypoints.get(player.getUniqueId());
        if (pw == null) return;
        Map<String, Waypoint> newWaypoints = pw.entrySet().stream()
                .filter(entry -> canSeeWaypoint(player, entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        if (newWaypoints.isEmpty()) return;
        sendWaypointsToPlayer(player, newWaypoints);
    }

    public void onWaypointOptionSelected(Player player, String waypointId, int optionIndex) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            Map<String, Waypoint> waypoints = playerWaypoints.get(player.getUniqueId());
            if (waypoints == null) return;
            Waypoint waypoint = waypoints.get(waypointId);
            if (waypoint == null) return;
            waypoint.options.get(optionIndex).onSelect(player, waypoint);
        });
    }

    public void emptyPlayerWaypoints(Player player) {
        playerWaypoints.remove(player.getUniqueId());
    }

    public void loadWaypoints(String worldName, FileConfiguration config) {
        ConfigurationSection waypoints = config.getConfigurationSection("waypoints");
        for (String id : waypoints.getKeys(false)) {
            try {
                ConfigurationSection waypointData = waypoints.getConfigurationSection(id);
                if (waypointData == null) {
                    plugin.getLogger().severe("Waypoint " + id + " not found");
                    continue;
                }

                boolean transparent = waypointData.getBoolean("transparent", false);

                String name = waypointData.getString("name", id);
                String initials = waypointData.getString("initials",
                        name.length() >= 2 ? name.substring(0, 2).toUpperCase() : name);
                Set<String> permissions = new HashSet<>(waypointData.getStringList("permissions"));

                List<WaypointOption> options = new ArrayList<>();
                for (String optionId : waypointData.getStringList("options")) {
                    WaypointOption option = optionManager.getOption(optionId);
                    if (option != null) {
                        options.add(option);
                    }
                }

                addWaypoint(
                        id, name, initials,
                        waypointData.getString("hover-text", ""),
                        worldName,
                        waypointData.getInt("x"), waypointData.getInt("y"), waypointData.getInt("z"),
                        waypointData.getString("color", "RED"), transparent,
                        permissions,
                        waypointData.getString("description", ""),
                        options,
                        false);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load " + id + " due to " + e);
            }

        }
        plugin.getLogger().info(prettyPrint());
    }

    public void loadConfig() {
        optionManager.loadOptions();

        clearAll(false);

        File file = new File(plugin.getDataFolder(), "waypoints/");
        if (!file.exists()) {
            file.mkdirs();
            plugin.saveResource("waypoints/world.yml", false);
        }

        File[] files = file.listFiles();
        if (files == null) {
            return;
        }

        for (File f : files) {
            if (f.isFile()) {
                String worldName = f.getName();
                if (worldName.endsWith(".yml")) {
                    try {
                        worldName = worldName.substring(0, worldName.length() - 4);
                        FileConfiguration config = YamlConfiguration.loadConfiguration(f);
                        loadWaypoints(worldName, config);
                    } catch (Exception e) {
                        plugin.getLogger().severe("Failed to load " + worldName + " due to " + e);
                    }
                }
            }
        }

    }

    public void saveWaypoints(String worldName) {
        try {
            File configFile = new File(plugin.getDataFolder(), "waypoints/" + worldName + ".yml");
            Map<String, Waypoint> worldWaypoints = getWaypoints(worldName);
            if (worldWaypoints.isEmpty()) {
                if (configFile.exists()) { // Remove empty world files
                    configFile.delete();
                }
                return;
            }
            FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            config.set("waypoints", null); // Clear existing waypoints section
            ConfigurationSection configSection = config.createSection("waypoints");
            for (Map.Entry<String, Waypoint> w : worldWaypoints.entrySet()) {
                Waypoint waypointData = w.getValue();
                ConfigurationSection waypointSection = configSection.createSection(w.getKey());
                waypointSection.set("x", waypointData.x);
                waypointSection.set("y", waypointData.y);
                waypointSection.set("z", waypointData.z);
                waypointSection.set("name", waypointData.name);
                waypointSection.set("color", waypointData.color.getName());
                waypointSection.set("initials", waypointData.initials);
                waypointSection.set("transparent", waypointData.transparent);
                waypointSection.set("permissions", new ArrayList<>(waypointData.permissions));
                waypointSection.set("description", waypointData.description);
                waypointSection.set("hover-text", waypointData.hoverText);
                waypointSection.set("options", waypointData.options.stream().map(option -> option.id).collect(Collectors.toList()));
            }

            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Failed to create waypoints file for " + worldName + " due to " + e);
        }
    }

    public void toConfig() {
        for (Map.Entry<String, Map<String, Waypoint>> entry : waypoints.entrySet()) {
            saveWaypoints(entry.getKey());
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
                }
            }
        }

        return sb.toString();
    }
}
