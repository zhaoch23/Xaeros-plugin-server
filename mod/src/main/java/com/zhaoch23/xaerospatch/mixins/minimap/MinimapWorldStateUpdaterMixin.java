package com.zhaoch23.xaerospatch.mixins.minimap;

import com.zhaoch23.xaerospatch.XaerosPatch;
import com.zhaoch23.xaerospatch.message.WaypointRequestPacket;
import net.minecraft.client.multiplayer.WorldClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import xaero.common.HudMod;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.world.container.MinimapWorldContainer;
import xaero.hud.minimap.world.container.MinimapWorldRootContainer;
import xaero.hud.minimap.world.state.MinimapWorldState;
import xaero.hud.path.XaeroPath;

@Mixin(
        value = {xaero.hud.minimap.world.state.MinimapWorldStateUpdater.class},
        remap = false
)
public abstract class MinimapWorldStateUpdaterMixin {

    @Shadow
    @Final
    private HudMod modMain;

    @Shadow
    @Final
    private MinimapSession session;

    @Shadow
    public abstract XaeroPath getPotentialContainerPath();

    @Shadow
    public abstract String getPotentialWorldNode(int dimId, boolean useWorldmap);

    /**
     * @author zhaoch23
     * @reason Sync remote on world change
     */
    @Overwrite
    public void update() {
        MinimapWorldState state = this.session.getWorldState();
        XaeroPath oldAutoWorldPath = state.getAutoWorldPath();
        XaeroPath potentialAutoContainerPath = this.getPotentialContainerPath();
        state.setAutoContainerPathIgnoreCaseCache(potentialAutoContainerPath);
        boolean worldmap = this.modMain.getSupportMods().worldmap();
        WorldClient world = this.session.getMc().world;
        String displayName = world.getWorldInfo().getWorldName();
        String potentialAutoWorldNode = this.getPotentialWorldNode(world.provider.getDimension(), worldmap);
        if (potentialAutoWorldNode != null) {
            XaeroPath autoWorldPath = potentialAutoContainerPath.resolve(potentialAutoWorldNode);
            state.setAutoWorldPath(autoWorldPath);
            if (oldAutoWorldPath == null || !potentialAutoContainerPath.equals(oldAutoWorldPath.getParent())) {
                // Entering new World
                MinimapWorldRootContainer autoRootContainer = this.session.getWorldManager().getAutoRootContainer();
                autoRootContainer.renameOldContainer(potentialAutoContainerPath);
                autoRootContainer.updateDimensionType(this.session.getMc().world);
                if (oldAutoWorldPath != null) {
                    MinimapWorldContainer oldContainer = this.session.getWorldManager().getWorldContainer(oldAutoWorldPath.getParent());
                    oldContainer.getServerWaypointManager().clear();
                }

                // Send a request to the server for waypoints
                this.session.getModMain().getMessageHandler().sendToServer(
                        new WaypointRequestPacket(
                                displayName
                        )
                );

                XaerosPatch.getLogger().debug("Waypoint sync request sent to server for world: " + displayName);
            }
        }
    }

}
