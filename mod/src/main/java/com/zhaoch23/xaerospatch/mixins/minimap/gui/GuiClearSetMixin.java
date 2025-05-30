package com.zhaoch23.xaerospatch.mixins.minimap.gui;

import com.zhaoch23.xaerospatch.common.WaypointUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xaero.hud.minimap.waypoint.set.WaypointSet;

@Mixin(
        value = {xaero.common.gui.GuiClearSet.class},
        remap = false
)
public abstract class GuiClearSetMixin {

    @Redirect(
            method = "func_146284_a(Lnet/minecraft/client/gui/GuiButton;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lxaero/hud/minimap/waypoint/set/WaypointSet;clear()V"
            )
    )
    private void redirect_clear(WaypointSet set) {
        // Only clear the local waypoints
        WaypointUtils.clearWaypoints(set, false);
    }
}
