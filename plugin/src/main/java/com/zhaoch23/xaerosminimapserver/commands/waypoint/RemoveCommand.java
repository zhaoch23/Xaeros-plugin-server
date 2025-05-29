package com.zhaoch23.xaerosminimapserver.commands.waypoint;

import com.zhaoch23.xaerosminimapserver.XaerosMinimapServer;
import com.zhaoch23.xaerosminimapserver.commands.CommandUtils;
import com.zhaoch23.xaerosminimapserver.commands.SubCommand;
import com.zhaoch23.xaerosminimapserver.waypoint.WaypointManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class RemoveCommand implements SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            return true;
        }
        WaypointManager waypointManager = XaerosMinimapServer.getWaypointManager();
        String worldName = args[1];
        if (worldName.equals("~")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
                return true;
            }
            Player player = (Player) sender;
            worldName = player.getWorld().getName();
        }

        if (!waypointManager.hasWaypoint(worldName, args[2])) {
            sender.sendMessage(ChatColor.RED + "Waypoint '" + args[2] + "' does not exist!");
            return true;
        }

        String name = args[2];
        waypointManager.removeWaypoint(worldName, name, true);
        sender.sendMessage(ChatColor.GREEN + "Waypoint '" + name + "' has been removed!");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 2) {
            completions.addAll(CommandUtils.completeWorldName(args[0]));
            if (args[1].isEmpty()) {
                completions.add("~");
            }
            return completions;
        }
        if (args.length == 3) {
            // Add existing waypoint names to tab completion
            Player player = (Player) sender;
            XaerosMinimapServer.getWaypointManager().getWaypoints(player.getWorld())
                    .forEach((id, waypoint) -> completions.add(id));
        }

        return completions;
    }

    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getUsage() {
        return "/xwp remove <world> <Id>";
    }
}
