package com.zhaoch23.xaerospatch.mixins.worldmap.render;

import com.zhaoch23.xaerospatch.common.IWaypoint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import xaero.map.WorldMap;
import xaero.map.element.render.ElementReader;
import xaero.map.element.render.ElementRenderInfo;
import xaero.map.element.render.ElementRenderProvider;
import xaero.map.element.render.ElementRenderer;
import xaero.map.icon.XaeroIcon;
import xaero.map.mods.gui.Waypoint;
import xaero.map.mods.gui.WaypointRenderContext;
import xaero.map.mods.gui.WaypointRenderer;
import xaero.map.mods.gui.WaypointSymbolCreator;


@Pseudo
@Mixin(
        value = {xaero.map.mods.gui.WaypointRenderer.class},
        remap = false
)
public abstract class WaypointRendererMixin extends ElementRenderer<Waypoint, WaypointRenderContext, WaypointRenderer> {

    protected WaypointRendererMixin(WaypointRenderContext context, ElementRenderProvider<Waypoint, WaypointRenderContext> provider, ElementReader<Waypoint, WaypointRenderContext, WaypointRenderer> reader) {
        super(context, provider, reader);
    }

    @Shadow
    public abstract WaypointSymbolCreator getSymbolCreator();

    /**
     * @author zhaoch23
     * @reason Change the rendering method
     */
    @Overwrite
    public void renderElementShadow(Waypoint w, boolean hovered, float optionalScale, double partialX, double partialY, ElementRenderInfo renderInfo) {

    }

    /**
     * @author zhaoch23
     * @reason Change the rendering method
     */
    @Overwrite
    public boolean renderElement(
            Waypoint w,
            boolean hovered,
            double optionalDepth,
            float optionalScale,
            double partialX,
            double partialY,
            ElementRenderInfo renderInfo) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

        boolean renderBackground = (hovered || WorldMap.settings.waypointBackgrounds) &&
                !((IWaypoint) w).isBackgroundTransparent();

        int color = w.getColor();
        int r = color >> 16 & 255;
        int g = color >> 8 & 255;
        int b = color & 255;
        float visibilityAlpha = w.isDisabled() ? 0.3F : 1.0F;
        GlStateManager.pushMatrix();

        double scaleFactor = optionalScale * getContext().worldmapWaypointsScale;
        GlStateManager.scale(scaleFactor, scaleFactor, 1.0F);

        String symbol = w.getSymbol();
        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
        if (renderBackground) { // Render a little flag
            int flagU = 35;
            int flagV = 34;
            int flagW = 30;
            int flagH = 43;
            if (symbol.length() > 1) {
                flagU += 35;
                flagW += 13;
            }
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) -flagW / 2.0F, 1 - flagH, 0.0F);
            GlStateManager.blendFunc(
                    GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
            );
            textureManager.bindTexture(WorldMap.guiTextures);
            GlStateManager.color(r * visibilityAlpha, g * visibilityAlpha, b * visibilityAlpha, visibilityAlpha);
            Gui.drawModalRectWithCustomSizedTexture(0, 0, (float) flagU, (float) flagV, flagW, flagH, 256.0F, 256.0F);
            GlStateManager.popMatrix();
        }


        if (w.getType() == 1) {
            XaeroIcon symbolIcon = getSymbolCreator().getDeathSymbolTexture(renderInfo.scaledResolution);
            GlStateManager.pushMatrix();
            GlStateManager.translate(-1.0F - (float) 27 / 2.0F, (float) (62 + (renderBackground ? -43 + 3 - 1 : -12)), 0.0F);
            GlStateManager.scale(1.0F, -1.0F, 1.0F);
            GlStateManager.bindTexture(symbolIcon.getTextureAtlas().getTextureId());
            GlStateManager.color(visibilityAlpha, visibilityAlpha, visibilityAlpha, visibilityAlpha);
            Gui.drawModalRectWithCustomSizedTexture(0, 0, (float) (symbolIcon.getOffsetX() + 1), (float) (symbolIcon.getOffsetY() + 1), 62, 62, (float) symbolIcon.getTextureAtlas().getWidth(), (float) symbolIcon.getTextureAtlas().getWidth());
            GlStateManager.popMatrix();
        } else {
            GlStateManager.pushMatrix();
            GlStateManager.scale(
                    3.0F,
                    3.0F,
                    1.0F
            );
            double scaledX = partialX / 3.0F;
            double scaledY = partialY / 3.0F;
            float renderWidth = fontRenderer.getStringWidth(symbol);
            fontRenderer.drawString(
                    w.getSymbol(),
                    (float) scaledX + 0.5F - renderWidth / 2,
                    (float) scaledY - (renderBackground ? 12 : 3),
                    -1,
                    false
            );
            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
        return false;
    }


}