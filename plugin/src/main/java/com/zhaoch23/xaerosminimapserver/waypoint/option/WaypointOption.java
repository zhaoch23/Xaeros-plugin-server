package com.zhaoch23.xaerosminimapserver.waypoint.option;

import com.zhaoch23.xaerosminimapserver.waypoint.Waypoint;
import org.bukkit.entity.Player;

public abstract class WaypointOption {

    public String initials;
    public String text;

    public abstract void onSelect(Player player, Waypoint waypoint);

}
