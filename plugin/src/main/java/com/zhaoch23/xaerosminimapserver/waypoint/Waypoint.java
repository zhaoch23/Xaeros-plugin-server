package com.zhaoch23.xaerosminimapserver.waypoint;

import com.zhaoch23.xaerosminimapserver.waypoint.option.WaypointOption;

import java.util.List;
import java.util.Set;

public class Waypoint {
    public final int x, y, z;

    public final String name, initials, hoverText;
    public final WaypointColor color;
    public final boolean transparent;
    public final Set<String> permissions;
    public final String description;
    public final List<WaypointOption> options;

    public Waypoint(
            int x, int y, int z,
            String name,
            String initials,
            String hoverText,
            WaypointColor color,
            boolean transparent,
            Set<String> permissions,
            String description,
            List<WaypointOption> options
    ) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.name = name;
        this.initials = initials;
        this.hoverText = hoverText;
        this.color = color;
        this.transparent = transparent;
        this.permissions = permissions;
        this.description = description;
        this.options = options;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Waypoint)) return false;
        Waypoint other = (Waypoint) obj;
        return name.equals(other.name);
    }

    public String toString() {
        return "Waypoint{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", name='" + name + '\'' +
                ", initials='" + initials + '\'' +
                ", color=" + color +
                ", transparent=" + transparent +
                ", permissions=" + permissions +
                '}';
    }
}
