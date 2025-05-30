package com.zhaoch23.xaerospatch.mixins.worldmap;

import com.zhaoch23.xaerospatch.common.IWaypoint;
import com.zhaoch23.xaerospatch.common.WaypointServerConfig;
import org.spongepowered.asm.mixin.*;

@Pseudo
@Mixin(value = xaero.map.mods.gui.Waypoint.class)
@Implements(
        @Interface(
                iface = IWaypoint.class,
                prefix = "xaerospatch$"
        )
)
public class WaypointMixin {
    @Unique
    private final WaypointServerConfig xaerospatch$serverConfig = WaypointServerConfig.createLocal();
    @Unique
    private boolean xaerospatch$backgroundTransparent = false;

    @Unique
    public WaypointServerConfig xaerospatch$getServerConfig() {
        return xaerospatch$serverConfig;
    }

    @Unique
    public boolean xaerospatch$isServerWaypoint() {
        return xaerospatch$serverConfig.serverWaypoint;
    }

    // TODO: Add this to add waypoint GUI
    public boolean xaerospatch$isBackgroundTransparent() {
        return xaerospatch$backgroundTransparent;
    }

    public void xaerospatch$setBackgroundTransparent(boolean transparent) {
        xaerospatch$backgroundTransparent = transparent;
    }


}
