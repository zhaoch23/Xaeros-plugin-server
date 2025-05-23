package com.zhaoch23.xaerospatch.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.waypoint.set.WaypointSet;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Mixin(
        value = {xaero.hud.minimap.waypoint.io.WaypointIO.class},
        remap = false
)
public abstract class WaypointIoMixin {

    @Redirect(
            method = "saveWaypoints",
            at = @At(
                    value = "INVOKE",
                    target = "Lxaero/hud/minimap/waypoint/set/WaypointSet;getWaypoints()Ljava/lang/Iterable;"
            )
    )
    private Iterable<Waypoint> filterServerWaypoints(WaypointSet set) { // Don't save server waypoints
        return StreamSupport.stream(set.getWaypoints().spliterator(), false)
                .filter(w -> !w.isServerWaypoint())
                .collect(Collectors.toList());
    }
}
