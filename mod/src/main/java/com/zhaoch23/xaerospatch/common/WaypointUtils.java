package com.zhaoch23.xaerospatch.common;

import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.waypoint.set.WaypointSet;

import java.util.ArrayList;
import java.util.List;

public class WaypointUtils {

    public static void clearWaypoints(WaypointSet set, boolean serverSide) {
        List<Waypoint> toRemove = new ArrayList<>();

        for (Waypoint waypoint : set.getWaypoints()) {
            if (waypoint.isServerWaypoint() == serverSide) {
                toRemove.add(waypoint);
            }
        }
        for (Waypoint waypoint : toRemove) {
            set.remove(waypoint);
        }
    }
}
