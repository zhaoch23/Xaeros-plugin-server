package com.zhaoch23.xaerosminimapserver.commands.waypoint;

import com.zhaoch23.xaerosminimapserver.commands.SubCommand;
import com.zhaoch23.xaerosminimapserver.XaerosMinimapServer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SetCommand implements SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        
        if (args.length < 6) {
            player.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            return true;
        }

        // Parse coordinates
        double x = parseCoordinate(args[1], player.getLocation().getX());
        double y = parseCoordinate(args[2], player.getLocation().getY());
        double z = parseCoordinate(args[3], player.getLocation().getZ());
        
        String name = args[4];
        String color = args[5];
        String initials = args.length > 6 ? args[6] : null;
        boolean transparent = args.length > 7 && Boolean.parseBoolean(args[7]);
        boolean update = args.length <= 8 || Boolean.parseBoolean(args[8]);

        Location location = new Location(player.getWorld(), x, y, z);
        
        if (initials != null) {
            XaerosMinimapServer.getWaypointManager().addWaypoint(
                name,
                initials,
                location,
                color,
                transparent,
                update
            );
        } else {
            XaerosMinimapServer.getWaypointManager().addWaypoint(
                name,
                location,
                color,
                transparent,
                update
            );
        }

        player.sendMessage(ChatColor.GREEN + "Waypoint '" + name + "' has been set at " + 
            String.format("%.1f, %.1f, %.1f", x, y, z) + "!");
        return true;
    }

    private double parseCoordinate(String coord, double relative) {
        if (coord.startsWith("~")) {
            double offset = coord.length() > 1 ? Double.parseDouble(coord.substring(1)) : 0;
            return relative + offset;
        }
        return Double.parseDouble(coord);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }

        List<String> completions = new ArrayList<>();

        switch (args.length) {
            case 2:
            case 3:
            case 4:
                completions.add("~");
                completions.add("~0");
                completions.add("~1");
                completions.add("~-1");
                break;
            case 5:
                completions.add("<name>");
                break;
            case 6:
                completions.addAll(Arrays.asList("red", "green", "blue", "yellow", "purple", "orange", "white", "black"));
                break;
            case 7:
                completions.add("<initials>");
                break;
            case 8:
                completions.addAll(Arrays.asList("true", "false"));
                break;
            case 9:
                completions.addAll(Arrays.asList("true", "false"));
                break;
        }

        return completions;
    }

    @Override
    public String getName() {
        return "set";
    }

    @Override
    public String getUsage() {
        return "/xwp set <x> <y> <z> <name> <color> [initials] [transparent] [update]";
    }
}
