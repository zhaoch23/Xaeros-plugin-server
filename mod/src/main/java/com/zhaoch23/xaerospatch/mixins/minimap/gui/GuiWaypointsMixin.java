package com.zhaoch23.xaerospatch.mixins.minimap.gui;

import com.zhaoch23.xaerospatch.common.IWaypoint;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import xaero.common.minimap.waypoints.Waypoint;

import java.util.ArrayList;
import java.util.List;

import static xaero.common.gui.GuiWaypoints.allWaypointsAre;

@Mixin(value = xaero.common.gui.GuiWaypoints.class, remap = false)
public abstract class GuiWaypointsMixin extends GuiScreen {

    @Shadow
    protected abstract ArrayList<Waypoint> getSelectedWaypointsList(boolean includeServer);

    @Shadow
    protected abstract boolean canTeleport();

    @Shadow
    public abstract boolean shouldDeleteSet();

    @Shadow
    protected abstract boolean isSomethingSelected();

    public boolean isAllLocal() {
        List<Waypoint> waypoints = getSelectedWaypointsList(true);
        for (Waypoint waypoint : waypoints) {
            if (((IWaypoint) waypoint).isServerWaypoint())
                return false;
        }
        return true;
    }

    public boolean isAllCanShare() {
        List<Waypoint> waypoints = getSelectedWaypointsList(true);
        for (Waypoint waypoint : waypoints) {
            if (!((IWaypoint) waypoint).getServerConfig().canShare)
                return false;
        }
        return true;
    }

    public boolean isAllCanDisable() {
        List<Waypoint> waypoints = getSelectedWaypointsList(true);
        for (Waypoint waypoint : waypoints) {
            if (!((IWaypoint) waypoint).getServerConfig().canDisable)
                return false;
        }
        return true;
    }

    /**
     * @author zhaoch23
     * @reason Rewrite to use server configs
     */
    @Overwrite
    private void updateButtons() {
        buttonList.get(0).enabled = isAllLocal(); // Delete
        buttonList.get(2).enabled = isAllLocal(); // Add/edit
        buttonList.get(3).enabled = canTeleport(); // Teleport
        buttonList.get(7).enabled = isAllCanShare(); // Share

        // Update the clear set button text
        buttonList.get(5).displayString =
                I18n.format(shouldDeleteSet() ? "gui.xaero_delete_set" : "gui.xaero_clear");

        ArrayList<Waypoint> selectedWaypointsList = this.getSelectedWaypointsList(true);
        if (isSomethingSelected() && allWaypointsAre(selectedWaypointsList, 0)) { // All Temporary
            buttonList.get(4).displayString = I18n.format("gui.xaero_delete");
            buttonList.get(0).displayString = I18n.format("gui.xaero_restore");
        } else {
            buttonList.get(0).displayString = I18n.format("gui.xaero_delete");
            String[] enabledisable = I18n.format("gui.xaero_disable_enable").split("/");
            GuiButton disableButton = buttonList.get(4);
            if (!allWaypointsAre(selectedWaypointsList, 1)) {// any enabled
                disableButton.displayString = I18n.format(enabledisable[0]);
                disableButton.enabled = isAllCanDisable();
            } else {
                disableButton.displayString = I18n.format(enabledisable[1]);
            }
        }
//        buttonList.get(4).enabled = isAllLocal(); // Enable/Disable
//        buttonList.get(5).enabled = isAllLocal(); // Clear set
//        buttonList.get(6).enabled = isAllLocal(); // Setting

    }
}
