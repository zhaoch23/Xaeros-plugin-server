package com.zhaoch23.xaerospatch.common;

import java.util.List;

public interface IWaypoint {

    boolean isServerWaypoint();

    boolean isBackgroundTransparent();

    void setBackgroundTransparent(boolean transparent);

    WaypointServerConfig getServerConfig();

    String getId();

    void setId(String id);

    String getDescription();

    void setDescription(String description);

    String getHoverText();

    void setHoverText(String hoverText);

    List<WaypointOption> getOptions();

    void setOptions(List<WaypointOption> options);
}
