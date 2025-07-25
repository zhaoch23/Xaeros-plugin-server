package com.zhaoch23.xaerosminimapserver.waypoint.option;

import com.zhaoch23.xaerosminimapserver.waypoint.Waypoint;
import org.bukkit.entity.Player;

public abstract class WaypointOption {

    final public String id;
    final public String initials;
    final public String text;

    public WaypointOption(String id, String initials, String text) {
        this.id = id;
        this.initials = initials;
        this.text = text;
    }

    public abstract void onSelect(Player player, Waypoint waypoint);

}
