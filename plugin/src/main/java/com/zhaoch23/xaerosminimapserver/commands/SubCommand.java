package com.zhaoch23.xaerosminimapserver.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface SubCommand {

    boolean onCommand(CommandSender sender, String[] args);

    List<String> onTabComplete(CommandSender sender, String[] args);

    String getName();

    String getUsage();
}
