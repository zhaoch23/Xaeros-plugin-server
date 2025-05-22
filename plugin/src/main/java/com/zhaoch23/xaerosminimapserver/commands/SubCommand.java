package com.zhaoch23.xaerosminimapserver.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

public interface SubCommand {
    
    boolean onCommand(CommandSender sender, String[] args);

    List<String> onTabComplete(CommandSender sender, String[] args);
    
    String getName();

    String getUsage();
}
