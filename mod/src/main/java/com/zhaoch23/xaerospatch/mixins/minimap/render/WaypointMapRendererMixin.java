package com.zhaoch23.xaerospatch.mixins.minimap.render;

import com.zhaoch23.xaerospatch.common.IWaypoint;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.common.minimap.render.MinimapRendererHelper;
import xaero.common.minimap.waypoints.Waypoint;

@Mixin(
        value = {xaero.hud.minimap.waypoint.render.WaypointMapRenderer.class},
        remap = false
)
public abstract class WaypointMapRendererMixin {
    /**
     * * This is a workaround for the fact that the current waypoint is not
     */

    private static Waypoint currentWaypoint;

    @Inject(
            method = "drawIconOnGUI(Lxaero/common/minimap/render/MinimapRendererHelper;" +
                    "Lxaero/common/minimap/waypoints/Waypoint;III)V",
            at = @At("HEAD")
    )
    private void captureWaypoint(
            MinimapRendererHelper helper,
            Waypoint w,
            int drawX,
            int drawY,
            int opacity,
            CallbackInfo ci
    ) {
        currentWaypoint = w;
    }

    @Redirect(
            method = "drawIconOnGUI(Lxaero/common/minimap/render/MinimapRendererHelper;" +
                    "Lxaero/common/minimap/waypoints/Waypoint;III)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Gui;func_73734_a(IIIII)V"
            )
    )
    private void skipBackgroundIfTransparent(
            int rectX1,
            int rectY1,
            int rectX2,
            int rectY2,
            int color
    ) {
        // only draw the background if we don't have transparent==true
        if (currentWaypoint == null || !((IWaypoint) currentWaypoint).isBackgroundTransparent()) {
            Gui.drawRect(rectX1, rectY1, rectX2, rectY2, color);
        }
    }

    @Inject(
            method = "drawIconOnGUI(Lxaero/common/minimap/render/MinimapRendererHelper;" +
                    "Lxaero/common/minimap/waypoints/Waypoint;III)V",
            at = @At("RETURN")
    )
    private void clearWaypoint(CallbackInfo ci) {
        currentWaypoint = null;
    }

}
