package com.zhaoch23.xaerosminimapserver.commands;

import com.zhaoch23.xaerosminimapserver.XaerosMinimapServer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

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
        List<String> completions = new ArrayList<>();
        for (World world : XaerosMinimapServer.plugin.getServer().getWorlds()) {
            if (world.getName().toLowerCase().startsWith(arg.toLowerCase())) {
                completions.add(world.getName());
            }
        }
        return completions;
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
