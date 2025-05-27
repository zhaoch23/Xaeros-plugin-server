package com.zhaoch23.xaerosminimapserver.commands.waypoint;

import com.zhaoch23.xaerosminimapserver.XaerosMinimapServer;
import com.zhaoch23.xaerosminimapserver.commands.CommandUtils;
import com.zhaoch23.xaerosminimapserver.commands.SubCommand;
import com.zhaoch23.xaerosminimapserver.waypoint.WaypointManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ShowCommand implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (args.length < 5) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            return true;
        }

        String playerName = args[1];
        String worldName = args[2];
        String id = args[3];
        boolean show = Boolean.parseBoolean(args[4]);

        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            sender.sendMessage(ChatColor.RED + "World not found!");
            return true;
        }

        WaypointManager waypointManager = XaerosMinimapServer.getWaypointManager();

        if (!waypointManager.getWaypoints(world).containsKey(id)) {
            sender.sendMessage(ChatColor.RED + "Waypoint not found!");
            return true;
        }

        if (show) {
            waypointManager.grantWaypoint(player, world, id);
        } else {
            waypointManager.revokeWaypoint(player, world, id);
        }
        sender.sendMessage(ChatColor.GREEN + "Waypoint '" + id + "' has been " + (show ? "shown" : "hidden") + " for player " + playerName + ".");

        waypointManager.sendWaypointsToPlayer(player);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        switch (args.length) {
            case 2:
                return CommandUtils.completePlayerName(args[1]);
            case 3:
                return CommandUtils.completeWorldName(args[2]);
            case 4:
                return XaerosMinimapServer.getWaypointManager().getWaypoints(args[2]).keySet().stream()
                        .filter(id -> id.startsWith(args[3]))
                        .collect(Collectors.toList());
            case 5:
                return CommandUtils.completeBoolean(args[4]);
        }
        return completions;
    }

    @Override
    public String getName() {
        return "show";
    }

    @Override
    public String getUsage() {
        return "/xwp show <player> <world> <id> [true|false]";
    }
}
