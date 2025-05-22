package com.zhaoch23.xaerospatch;

import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.waypoint.WaypointColor;
import xaero.hud.minimap.waypoint.WaypointPurpose;

public class RemoteWaypoint extends Waypoint {

    public RemoteWaypoint(int x, int y, int z, String name, String initials, WaypointColor color) {
        super(x, y, z, name, initials, color);
    }

    public RemoteWaypoint(int x, int y, int z, String name, String initials, WaypointColor color, WaypointPurpose purpose) {
        super(x, y, z, name, initials, color, purpose);
    }

    public RemoteWaypoint(int x, int y, int z, String name, String initials, WaypointColor color, WaypointPurpose purpose, boolean temp) {
        super(x, y, z, name, initials, color, purpose, temp);
    }

    public RemoteWaypoint(int x, int y, int z, String name, String initials, WaypointColor color, WaypointPurpose purpose, boolean temp, boolean yIncluded) {
        super(x, y, z, name, initials, color, purpose, temp, yIncluded);
    }


}
