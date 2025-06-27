package com.zhaoch23.xaerosminimapserver.waypoint.option;

import com.zhaoch23.xaerosminimapserver.waypoint.Waypoint;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandOption extends WaypointOption {

    public DispatchMode dispatchMode;
    public List<String> onSelect;

    public CommandOption(String initials, String text, String dispatchMode, List<String> onSelect) {
        this.initials = initials;
        this.text = text;
        this.dispatchMode = DispatchMode.fromName(dispatchMode);
        this.onSelect = onSelect;
    }

    @Override
    public void onSelect(Player player, Waypoint waypoint) {
        for (String command : onSelect) {
            String formattedCommand = waypoint.formatString(command, player);
            switch (dispatchMode) {
                case PLAYER:
                    player.performCommand(formattedCommand);
                    break;
                case SERVER:
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formattedCommand);
                    break;
                case OP:
                    boolean op = player.isOp();
                    try {
                        player.setOp(true);
                        player.performCommand(formattedCommand);
                    } finally {
                        player.setOp(op);
                    }
                    break;
            }
        }
    }

    public enum DispatchMode {
        PLAYER("player"),
        SERVER("server"),
        OP("op");

        private final String name;

        DispatchMode(String name) {
            this.name = name;
        }

        public static DispatchMode fromName(String name) {
            for (DispatchMode mode : DispatchMode.values()) {
                if (mode.getName().equalsIgnoreCase(name)) {
                    return mode;
                }
            }
            throw new IllegalArgumentException("Invalid dispatch mode: " + name);
        }

        public String getName() {
            return name;
        }
    }
}
