package com.zhaoch23.xaerosminimapserver.commands.waypoint;

import com.zhaoch23.xaerosminimapserver.commands.BaseCommand;

public class WaypointCommand extends BaseCommand {

    public WaypointCommand() {
        super("waypoint");
        registerSubCommand(new SetCommand());
        registerSubCommand(new RemoveCommand());
        registerSubCommand(new ReloadCommand());
        registerSubCommand(new SaveCommand());
        registerSubCommand(new ListCommand());
        registerSubCommand(new RefreshCommand());
        registerSubCommand(new ShowCommand());
    }

    @Override
    protected String getPermissionNode() {
        return "xaerosminimapserver.waypoint";
    }
}
