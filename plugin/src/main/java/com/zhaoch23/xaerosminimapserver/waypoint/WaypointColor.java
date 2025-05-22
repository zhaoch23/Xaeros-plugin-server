package com.zhaoch23.xaerosminimapserver.waypoint;

public enum WaypointColor {
    BLACK(0, "BLACK"),
    DARK_BLUE(1, "DARK_BLUE"),
    DARK_GREEN(2, "DARK_GREEN"),
    DARK_AQUA(3, "DARK_AQUA"),
    DARK_RED(4, "DARK_RED"),
    DARK_PURPLE(5, "DARK_PURPLE"),
    GOLD(6, "GOLD"),
    GRAY(7, "GRAY"),
    DARK_GRAY(8, "DARK_GRAY"),
    BLUE(9, "BLUE"),
    GREEN(10, "GREEN"),
    AQUA(11, "AQUA"),
    RED(12, "RED"),
    PURPLE(13, "PURPLE"),
    YELLOW(14, "YELLOW"),
    WHITE(15, "WHITE");


    private final int color;
    private final String name;

    WaypointColor(int color, String name) {
        this.color = color;
        this.name = name;
    }

    public static WaypointColor fromName(String name) {
        name = name.toUpperCase();
        for (WaypointColor waypointColor : values()) {
            if (waypointColor.getName().equals(name)) {
                return waypointColor;
            }
        }
        return WHITE;
    }

    public static WaypointColor fromColor(int color) {
        for (WaypointColor waypointColor : values()) {
            if (waypointColor.getColor() == color) {
                return waypointColor;
            }
        }
        return WHITE; // Default to white if not found
    }

    public int getColor() {
        return color;
    }

    public String getName() {
        return name;
    }
}
