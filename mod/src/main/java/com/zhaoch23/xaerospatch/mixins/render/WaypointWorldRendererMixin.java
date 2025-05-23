package com.zhaoch23.xaerospatch.mixins.render;

import com.zhaoch23.xaerospatch.common.IWaypoint;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.common.icon.XaeroIcon;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.compass.render.CompassRenderer;
import xaero.hud.minimap.waypoint.render.world.WaypointWorldRenderer;

@Mixin(
        value = {xaero.hud.minimap.waypoint.render.world.WaypointWorldRenderer.class},
        remap = false
)
public abstract class WaypointWorldRendererMixin {
    /**
     * * This mixin is used to skip the background quad rendering for transparent waypoints.
     */

    private Waypoint currentWaypoint = null;

    @Inject(
            method = "renderIcon",
            at = @At("HEAD")
    )
    private void onRenderIconHead(Waypoint w, boolean highlit, FontRenderer fontRenderer, CallbackInfo ci) {
        this.currentWaypoint = w;
    }

    @Inject(
            method = "renderColorBackground",
            at = @At("HEAD"),
            cancellable = true
    )
    private void skipIfTransparent(int addedFrame, float r, float g, float b, float a, CallbackInfo ci) {
        if (currentWaypoint != null && ((IWaypoint) currentWaypoint).isBackgroundTransparent()) {
            ci.cancel();  // skip the entire background quad
        }
    }

    @Inject(
            method = "renderIcon(Lxaero/common/minimap/waypoints/Waypoint;ZLnet/minecraft/client/gui/FontRenderer;)V",
            at = @At("RETURN")
    )
    private void clearWaypoint(CallbackInfo ci) {
        this.currentWaypoint = null;
    }

}
