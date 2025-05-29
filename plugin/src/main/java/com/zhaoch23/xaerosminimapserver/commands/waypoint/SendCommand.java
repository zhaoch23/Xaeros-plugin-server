package com.zhaoch23.xaerosminimapserver.commands.waypoint;

import com.zhaoch23.xaerosminimapserver.XaerosMinimapServer;
import com.zhaoch23.xaerosminimapserver.commands.CommandUtils;
import com.zhaoch23.xaerosminimapserver.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SendCommand implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            return true;
        }

        String playerName = args[1];
        String worldName = args[2];

        Player player = XaerosMinimapServer.plugin.getServer().getPlayer(playerName);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }
        XaerosMinimapServer.getWaypointManager()
                .sendWaypointSetsToPlayers(
                        Collections.singletonList(worldName),
                        Collections.singletonList(player)
                );

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {

        switch (args.length) {
            case 2:
                return CommandUtils.completePlayerName(args[1]);
            case 3:
                return CommandUtils.completeWorldName(args[2]);
        }
        return new ArrayList<>();
    }

    @Override
    public String getName() {
        return "send";
    }

    @Override
    public String getUsage() {
        return "/xwp send <player> <world>";
    }

}
