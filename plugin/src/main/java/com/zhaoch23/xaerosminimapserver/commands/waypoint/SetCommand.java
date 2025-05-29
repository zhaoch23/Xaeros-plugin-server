package com.zhaoch23.xaerosminimapserver.commands.waypoint;

import com.zhaoch23.xaerosminimapserver.XaerosMinimapServer;
import com.zhaoch23.xaerosminimapserver.commands.CommandUtils;
import com.zhaoch23.xaerosminimapserver.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SetCommand implements SubCommand {

    private static final String[] validColors = {"red", "green", "blue", "yellow", "purple", "orange", "white", "black"};

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (args.length < 8) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            return true;
        }

        if (!(sender instanceof Player) &&
                (args[1].equals("~") || args[2].equals("~") || args[3].equals("~") || args[4].equals("~"))) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            return true;
        }

        String worldName = args[1];
        if (worldName.equals("~")) {
            Player player = (Player) sender;
            worldName = player.getWorld().getName();
        }

        double x, y, z;
        if (sender instanceof Player) {
            Player player = (Player) sender;
            // Parse coordinates
            x = parseCoordinate(args[2], player.getLocation().getX());
            y = parseCoordinate(args[3], player.getLocation().getY());
            z = parseCoordinate(args[4], player.getLocation().getZ());
        } else {
            x = parseCoordinate(args[2], 0);
            y = parseCoordinate(args[3], 0);
            z = parseCoordinate(args[4], 0);
        }

        String id = args[5];
        String name = args[6];
        String color = args[7];
        String initials = args.length > 8 ? args[8] : null;
        boolean transparent = args.length > 9 && Boolean.parseBoolean(args[9]);
        boolean refresh = args.length <= 10 || Boolean.parseBoolean(args[10]);

        if (initials != null) {
            XaerosMinimapServer.getWaypointManager().addWaypoint(
                    id,
                    name,
                    initials,
                    worldName,
                    (int) x,
                    (int) y,
                    (int) z,
                    color,
                    transparent,
                    new HashSet<>(),
                    refresh
            );
        } else {
            XaerosMinimapServer.getWaypointManager().addWaypoint(
                    id,
                    name,
                    worldName,
                    (int) x,
                    (int) y,
                    (int) z,
                    color,
                    transparent,
                    new HashSet<>(),
                    refresh
            );
        }

        sender.sendMessage(ChatColor.GREEN + "Waypoint '" + id + "' has been set at " +
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
        List<String> completions = new ArrayList<>();

        switch (args.length) {
            case 2:
                completions.addAll(CommandUtils.completeWorldName(args[1]));
                if (args[1].isEmpty()) {
                    completions.add("~");
                }
                return completions;
            case 3:
            case 4:
            case 5:
                completions.add("~");
                completions.add("~0");
                completions.add("~1");
                completions.add("~-1");
                return completions;
            case 6:
                completions.add("<id>");
                return completions;
            case 7:
                completions.add("<name>");
                return completions;
            case 8:
                for (String color : validColors) {
                    if (color.startsWith(args[7])) {
                        completions.add(color);
                    }
                }
                return completions;
            case 9:
                completions.add("<initials>");
                return completions;
            case 10:
            case 11:
                return CommandUtils.completeBoolean(args[10]);
        }

        return completions;
    }

    @Override
    public String getName() {
        return "set";
    }

    @Override
    public String getUsage() {
        return "/xwp set <world> <x> <y> <z> <id> <name> <color> [initials] [transparent] [refresh]";
    }
}
