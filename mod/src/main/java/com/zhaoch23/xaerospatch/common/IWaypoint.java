package com.zhaoch23.xaerospatch.common;

public interface IWaypoint {

    boolean isServerWaypoint();

    boolean isBackgroundTransparent();

    void setBackgroundTransparent(boolean transparent);

    WaypointServerConfig getServerConfig();

    String getDescription();

    void setDescription(String description);
}
