package com.zhaoch23.xaerospatch.mixins.worldmap;

import com.zhaoch23.xaerospatch.common.IWaypoint;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.map.mods.gui.Waypoint;

@Pseudo
@Mixin(
        value = xaero.map.mods.SupportXaeroMinimap.class,
        remap = false
)
abstract public class SupportXaeroMinimapMixin {

    /**
     * @author zhaoch23
     * @reason inject additional waypoint functionality
     */
    @Inject(
            method = "convertWaypoint",
            at = @At("RETURN")
    )
    private void injectWaypointConversion(xaero.common.minimap.waypoints.Waypoint w, boolean editable, String setName, double dimDiv, CallbackInfoReturnable<Waypoint> cir) {
        Waypoint converted = cir.getReturnValue();
        if (converted == null) return;

        IWaypoint iconverted = (IWaypoint) converted;
        IWaypoint iorigin = (IWaypoint) w;
        iconverted.setBackgroundTransparent(iorigin.isBackgroundTransparent());
        iconverted.setDescription(iorigin.getDescription());
        iorigin.getServerConfig().copyTo(iconverted.getServerConfig());
    }

}
