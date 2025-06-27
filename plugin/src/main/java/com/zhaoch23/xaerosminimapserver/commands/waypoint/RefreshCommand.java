package com.zhaoch23.xaerosminimapserver.commands.waypoint;

import com.zhaoch23.xaerosminimapserver.XaerosMinimapServer;
import com.zhaoch23.xaerosminimapserver.commands.CommandUtils;
import com.zhaoch23.xaerosminimapserver.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class RefreshCommand implements SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (args.length == 1) {
            XaerosMinimapServer.getWaypointManager().sendWaypointsToPlayers();
            sender.sendMessage(ChatColor.GREEN + "Waypoints have been refreshed for all players!");
        } else if (args.length == 3) {
            if (args[1].equals("player")) {
                Player player = XaerosMinimapServer.plugin.getServer().getPlayer(args[2]);
                if (player == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found!");
                    return true;
                }
                XaerosMinimapServer.getWaypointManager().refreshWaypoints(player);
            } else if (args[1].equals("world")) {
                World world = XaerosMinimapServer.plugin.getServer().getWorld(args[2]);
                if (world == null) {
                    sender.sendMessage(ChatColor.RED + "World not found!");
                    return true;
                }
                XaerosMinimapServer.getWaypointManager().sendWaypointsToPlayers(world);
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Invalid arguments. Usage: " + getUsage());
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        switch (args.length) {
            case 2:
                completions.add("player");
                completions.add("world");
                return completions;
            case 3:
                if (args[1].equals("player")) {
                    completions.addAll(CommandUtils.completePlayerName(args[2]));
                } else if (args[1].equals("world")) {
                    completions.addAll(CommandUtils.completeWorldName(args[2]));
                }
                return completions;
        }
        return completions;
    }

    @Override
    public String getName() {
        return "refresh";
    }

    @Override
    public String getUsage() {
        return "/xwp refresh [player|world <name>]";
    }
}
