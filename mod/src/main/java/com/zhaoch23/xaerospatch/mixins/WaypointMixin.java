package com.zhaoch23.xaerospatch.mixins;

import com.zhaoch23.xaerospatch.common.IWaypoint;
import com.zhaoch23.xaerospatch.common.WaypointServerConfig;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = xaero.common.minimap.waypoints.Waypoint.class, remap = false)
@Implements(
        @Interface(
                iface = IWaypoint.class,
                prefix = "xaerospatch$"
        )
)
public abstract class WaypointMixin {

    @Unique
    private final WaypointServerConfig xaerospatch$serverConfig = WaypointServerConfig.createLocal();

    @Unique
    public WaypointServerConfig xaerospatch$getServerConfig() {
        return xaerospatch$serverConfig;
    }

    @Inject(
            method = "isServerWaypoint",
            at = @At("HEAD"),
            cancellable = true
    )
    public void isServerWaypoint(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(this.xaerospatch$serverConfig.serverWaypoint);
    }


}
