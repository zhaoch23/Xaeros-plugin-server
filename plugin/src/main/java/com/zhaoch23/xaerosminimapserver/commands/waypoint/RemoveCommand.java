package com.zhaoch23.xaerosminimapserver.commands.waypoint;

import com.zhaoch23.xaerosminimapserver.XaerosMinimapServer;
import com.zhaoch23.xaerosminimapserver.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class RemoveCommand implements SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            return true;
        }

        String name = args[1];
        if (!XaerosMinimapServer.getWaypointManager().hasWaypoint(player.getWorld(), name)) {
            player.sendMessage(ChatColor.RED + "Waypoint '" + name + "' does not exist!");
            return true;
        }
        XaerosMinimapServer.getWaypointManager().removeWaypoint(player.getWorld(), name, true);
        player.sendMessage(ChatColor.GREEN + "Waypoint '" + name + "' has been removed!");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 2) {
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
        return "/xwp remove <Id>";
    }
}
