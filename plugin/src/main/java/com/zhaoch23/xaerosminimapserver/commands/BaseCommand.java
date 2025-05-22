package com.zhaoch23.xaerosminimapserver.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

public abstract class BaseCommand implements TabExecutor {
    private final String command;
    private final List<SubCommand> subCommands;

    public BaseCommand(String command) {
        this.command = command;
        this.subCommands = new ArrayList<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if (this.getPermissionNode() != null && !sender.hasPermission(this.getPermissionNode())) {
            sender.sendMessage("&cYou don't have permission to do this!");
            return true;
        }
        if (args.length == 0) {
            for (SubCommand subCommand : this.subCommands) {
                sender.sendMessage(subCommand.getUsage());
            }
            return false;
        }
        for (SubCommand subCommand : this.subCommands) {
            if (subCommand.getName().equalsIgnoreCase(args[0])) {
                return subCommand.onCommand(sender, args);
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if (this.getPermissionNode() != null && !sender.hasPermission(this.getPermissionNode())) {
            return null;
        }
        if (args.length == 1) {
            ArrayList<String> tabCompletes = new ArrayList<>();
            for (SubCommand subCommand : this.subCommands) {
                tabCompletes.add(subCommand.getName());
            }
            return tabCompletes;
        } else {
            for (SubCommand subCommand : this.subCommands) {
                if (subCommand.getName().equalsIgnoreCase(args[0])) {
                    return subCommand.onTabComplete(sender, args);
                }
            }
        }
        return null;
    }
    
    protected void registerSubCommand(SubCommand subCommand) {
        this.subCommands.add(subCommand);
    }

    protected abstract String getPermissionNode();

    protected String getName() {
        return command;
    }
}
