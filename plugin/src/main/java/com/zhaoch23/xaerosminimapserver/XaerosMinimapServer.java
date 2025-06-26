package com.zhaoch23.xaerosminimapserver;

import com.zhaoch23.xaerosminimapserver.commands.waypoint.WaypointCommand;
import com.zhaoch23.xaerosminimapserver.network.NetworkHandler;
import com.zhaoch23.xaerosminimapserver.waypoint.WaypointManager;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class XaerosMinimapServer extends JavaPlugin implements Listener {

    public static XaerosMinimapServer plugin;
    private final Setting setting = new Setting();
    private WaypointManager waypointManager;
    private NetworkHandler networkHandler;

    public static WaypointManager getWaypointManager() {
        return plugin.waypointManager;
    }

    public static NetworkHandler getNetworkHandler() {
        return plugin.networkHandler;
    }

    public static Setting getSetting() {
        return plugin.setting;
    }

    @Override
    public void onEnable() {
        plugin = this;
        waypointManager = new WaypointManager(this);
        networkHandler = new NetworkHandler(this);

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("xwaypoint").setExecutor(new WaypointCommand());

        reload();
    }

    public void reload() {
        saveDefaultConfig();
        reloadConfig();
        setting.loadConfig(getConfig());
        setting.sendConfigToPlayers();

        waypointManager.loadConfig();
        waypointManager.sendWaypointsToPlayers();
    }

    public String formatWithPlaceholders(Player player, String message) {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return PlaceholderAPI.setPlaceholders(player, message);
        }
        // Fallback if PlaceholderAPI is not available
        return message.replace("%player%", player.getName())
                .replace("%world%", player.getWorld().getName());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        getLogger().info("Sending rules to " + event.getPlayer().getName());
        setting.sendConfigToPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World fromWorld = event.getFrom(); // World they came from
        World toWorld = player.getWorld(); // Current world
        Bukkit.getLogger().info(player.getName() + " changed world from " + fromWorld.getName() + " to " + toWorld.getName());
        // Send waypoints for the new world
        waypointManager.sendWaypointsToPlayer(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        waypointManager.emptyPlayerWaypoints(event.getPlayer());
    }

    public void save() {
        waypointManager.toConfig();
        saveConfig();
    }

    @Override
    public void onDisable() {
//        save();
        networkHandler.unload();

    }
}
