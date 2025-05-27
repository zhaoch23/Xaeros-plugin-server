package com.zhaoch23.xaerosminimapserver.commands.waypoint;

import com.zhaoch23.xaerosminimapserver.XaerosMinimapServer;
import com.zhaoch23.xaerosminimapserver.commands.SubCommand;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ListCommand implements SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        String waypointsList = XaerosMinimapServer.getWaypointManager().prettyPrint();

        // Split the output into lines and send each line to the player
        for (String line : waypointsList.split("\n")) {
            sender.sendMessage(line);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>(); // No tab completion needed for list command
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getUsage() {
        return "/xwp list";
    }
}
