package com.zhaoch23.xaerosminimapserver.commands.waypoint;

import com.zhaoch23.xaerosminimapserver.XaerosMinimapServer;
import com.zhaoch23.xaerosminimapserver.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ReloadCommand implements SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        XaerosMinimapServer.plugin.reload();
        sender.sendMessage(ChatColor.GREEN + "XaerosMinimapServer has been reloaded!");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getUsage() {
        return "/xwp reload";
    }
}
