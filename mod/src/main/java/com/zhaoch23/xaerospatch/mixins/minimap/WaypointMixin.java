package com.zhaoch23.xaerospatch.mixins.minimap;

import com.zhaoch23.xaerospatch.common.IWaypoint;
import com.zhaoch23.xaerospatch.common.WaypointOption;
import com.zhaoch23.xaerospatch.common.WaypointServerConfig;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

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
    String xaerospatch$id;
    @Unique
    String xaerospatch$description;
    @Unique
    String xaerospatch$hoverText;
    @Unique
    List<WaypointOption> xaerospatch$options;
    @Unique
    private boolean xaerospatch$backgroundTransparent = false;

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

    public String xaerospatch$getId() {
        return xaerospatch$id;
    }

    public void xaerospatch$setId(String id) {
        this.xaerospatch$id = id;
    }

    // TODO: Add this to add waypoint GUI
    public boolean xaerospatch$isBackgroundTransparent() {
        return xaerospatch$backgroundTransparent;
    }

    public void xaerospatch$setBackgroundTransparent(boolean transparent) {
        xaerospatch$backgroundTransparent = transparent;
    }

    public String xaerospatch$getDescription() {
        return xaerospatch$description;
    }

    public void xaerospatch$setDescription(String description) {
        this.xaerospatch$description = description;
    }

    public String xaerospatch$getHoverText() {
        return xaerospatch$hoverText;
    }

    public void xaerospatch$setHoverText(String hoverText) {
        this.xaerospatch$hoverText = hoverText;
    }

    public List<WaypointOption> xaerospatch$getOptions() {
        return xaerospatch$options;
    }

    public void xaerospatch$setOptions(List<WaypointOption> options) {
        this.xaerospatch$options = options;
    }
}
