package com.zhaoch23.xaerosminimapserver.waypoint;

public abstract class WaypointOption {

    public String text;

    abstract void onSelect(Waypoint waypoint);

}
