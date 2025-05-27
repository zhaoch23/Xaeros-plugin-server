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
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 7) {
            player.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            return true;
        }

        // Parse coordinates
        double x = parseCoordinate(args[1], player.getLocation().getX());
        double y = parseCoordinate(args[2], player.getLocation().getY());
        double z = parseCoordinate(args[3], player.getLocation().getZ());

        String id = args[4];
        String name = args[5];
        String color = args[6];
        String initials = args.length > 7 ? args[7] : null;
        boolean transparent = args.length > 8 && Boolean.parseBoolean(args[8]);
        boolean refresh = args.length <= 9 || Boolean.parseBoolean(args[9]);

        Location location = new Location(player.getWorld(), x, y, z);

        if (initials != null) {
            XaerosMinimapServer.getWaypointManager().addWaypoint(
                    id,
                    name,
                    initials,
                    location,
                    color,
                    transparent,
                    new HashSet<>(),
                    refresh
            );
        } else {
            XaerosMinimapServer.getWaypointManager().addWaypoint(
                    id,
                    name,
                    location,
                    color,
                    transparent,
                    new HashSet<>(),
                    refresh
            );
        }

        player.sendMessage(ChatColor.GREEN + "Waypoint '" + id + "' has been set at " +
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
                return completions;
            case 5:
                completions.add("<id>");
                return completions;
            case 6:
                completions.add("<name>");
                return completions;
            case 7:
                for (String color : validColors) {
                    if (color.startsWith(args[5])) {
                        completions.add(color);
                    }
                }
                return completions;
            case 8:
                completions.add("<initials>");
                return completions;
            case 9:
            case 10:
                return CommandUtils.completeBoolean(args[9]);
        }

        return completions;
    }

    @Override
    public String getName() {
        return "set";
    }

    @Override
    public String getUsage() {
        return "/xwp set <x> <y> <z> <id> <name> <color> [initials] [transparent] [refresh]";
    }
}
