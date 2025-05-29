package com.zhaoch23.xaerosminimapserver.commands;

import com.zhaoch23.xaerosminimapserver.XaerosMinimapServer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommandUtils {

    public static final String[] BOOLS = {"true", "false"};

    public static List<String> completePlayerName(String arg) {
        List<String> completions = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().toLowerCase().startsWith(arg.toLowerCase())) {
                completions.add(player.getName());
            }
        }
        return completions;
    }

    public static List<String> completeWorldName(String arg) {
        Set<String> completions = new HashSet<>();
        for (String world : XaerosMinimapServer.getWaypointManager().getWorlds()) {
            if (world.toLowerCase().startsWith(arg.toLowerCase())) {
                completions.add(world);
            }
        }
        for (World world : Bukkit.getWorlds()) {
            if (world.getName().toLowerCase().startsWith(arg.toLowerCase())) {
                completions.add(world.getName());
            }
        }
        return new ArrayList<>(completions);
    }

    public static List<String> completeBoolean(String arg) {
        List<String> completions = new ArrayList<>();
        for (String bool : BOOLS) {
            if (bool.toLowerCase().startsWith(arg.toLowerCase())) {
                completions.add(bool);
            }
        }
        return completions;
    }


}
