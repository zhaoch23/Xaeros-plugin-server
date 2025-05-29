package com.zhaoch23.xaerosminimapserver.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.ArrayList;
import java.util.List;

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
        if (args.length == 0 || args[0].equals("help")) {
            StringBuilder usage = new StringBuilder();
            usage.append("&cUsage: &f").append(this.getName()).append(" <subcommand> [args]\n");
            for (SubCommand subCommand : this.subCommands) {
                usage.append("&c").append(subCommand.getName()).append(": &f").append(subCommand.getUsage()).append("\n");
            }
            sender.sendMessage(usage.toString());
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
                if (subCommand.getName().startsWith(args[0])) {
                    tabCompletes.add(subCommand.getName());
                }
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
