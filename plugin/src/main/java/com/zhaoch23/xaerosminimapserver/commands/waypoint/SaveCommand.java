package com.zhaoch23.xaerosminimapserver.commands.waypoint;

import com.zhaoch23.xaerosminimapserver.commands.SubCommand;
import com.zhaoch23.xaerosminimapserver.XaerosMinimapServer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class SaveCommand implements SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        XaerosMinimapServer.plugin.save();
        sender.sendMessage(ChatColor.GREEN + "Waypoints have been saved to the configuration file!");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public String getName() {
        return "save";
    }

    @Override
    public String getUsage() {
        return "/waypoint save";
    }
}
