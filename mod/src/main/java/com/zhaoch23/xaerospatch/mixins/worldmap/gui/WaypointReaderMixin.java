package com.zhaoch23.xaerospatch.mixins.worldmap.gui;

import com.zhaoch23.xaerospatch.common.DropDownSlot;
import com.zhaoch23.xaerospatch.common.IWaypoint;
import com.zhaoch23.xaerospatch.common.WaypointOption;
import com.zhaoch23.xaerospatch.message.OptionSelectedPacket;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import xaero.map.WorldMap;
import xaero.map.element.MapElementReader;
import xaero.map.gui.CursorBox;
import xaero.map.gui.GuiMap;
import xaero.map.gui.IRightClickableElement;
import xaero.map.gui.dropdown.rightclick.RightClickOption;
import xaero.map.mods.SupportMinimap;
import xaero.map.mods.SupportMods;
import xaero.map.mods.gui.Waypoint;
import xaero.map.mods.gui.WaypointRenderContext;
import xaero.map.mods.gui.WaypointRenderer;

import java.util.ArrayList;
import java.util.List;

@Pseudo
@Mixin(
        value = {xaero.map.mods.gui.WaypointReader.class},
        remap = false
)
public abstract class WaypointReaderMixin extends MapElementReader<Waypoint, WaypointRenderContext, WaypointRenderer> {

    @Override
    public CursorBox getTooltip(Waypoint element, WaypointRenderContext context, boolean overMenu) {
        String hoverText = ((IWaypoint) element).getHoverText();
        if (hoverText != null && !hoverText.isEmpty()) {
            return new CursorBox(hoverText);
        }
        return null;
    }

    /**
     * @author zhaoch23
     * @reason Working with server waypoints
     */
    @Overwrite
    public ArrayList<RightClickOption> getRightClickOptions(final Waypoint element, IRightClickableElement target) {
        ArrayList<RightClickOption> rightClickOptions = new ArrayList<>();

        final boolean isServerWaypoint = ((IWaypoint) element).isServerWaypoint();

        rightClickOptions.add(new DropDownSlot(element.getName(), 0, target, false) {
            public void onAction(GuiScreen screen) {
                if (!isServerWaypoint) {
                    SupportMods.xaeroMinimap.openWaypoint((GuiMap) screen, element);
                }
            }
        });

        if (WorldMap.settings.coordinates && !SupportMods.xaeroMinimap.hidingWaypointCoordinates()) {
            rightClickOptions.add(new DropDownSlot(
                    String.format("X: %d, Y: %s, Z: %d", element.getX(), element.isyIncluded() ? String.valueOf(element.getY()) : "~", element.getZ()),
                    rightClickOptions.size(),
                    target,
                    false) {
                public void onAction(GuiScreen screen) {
                    if (!isServerWaypoint) {
                        SupportMods.xaeroMinimap.openWaypoint((GuiMap) screen, element);
                    }
                }
            });
        }

        String description = ((IWaypoint) element).getDescription();
        if (description != null && !description.isEmpty()) {
            rightClickOptions.add(new DropDownSlot(description, rightClickOptions.size(), target, false) {
                public void onAction(GuiScreen screen) {
                    if (!isServerWaypoint) {
                        SupportMods.xaeroMinimap.openWaypoint((GuiMap) screen, element);
                    }
                }
            });
        }

        if (!isServerWaypoint) {
            rightClickOptions.add((new RightClickOption("gui.xaero_right_click_waypoint_edit", rightClickOptions.size(), target) {
                public void onAction(GuiScreen screen) {
                    SupportMods.xaeroMinimap.openWaypoint((GuiMap) screen, element);
                }
            }).setNameFormatArgs("E"));
            rightClickOptions.add((new RightClickOption("gui.xaero_right_click_waypoint_teleport", rightClickOptions.size(), target) {
                public void onAction(GuiScreen screen) {
                    SupportMods.xaeroMinimap.teleportToWaypoint(screen, element);
                }

                public boolean isActive() {
                    return SupportMods.xaeroMinimap.canTeleport(SupportMods.xaeroMinimap.getWaypointWorld());
                }
            }).setNameFormatArgs("T"));
            rightClickOptions.add(new RightClickOption("gui.xaero_right_click_waypoint_share", rightClickOptions.size(), target) {
                public void onAction(GuiScreen screen) {
                    SupportMods.xaeroMinimap.shareWaypoint(element, (GuiMap) screen, SupportMods.xaeroMinimap.getWaypointWorld());
                }
            });
            rightClickOptions.add((new RightClickOption("", rightClickOptions.size(), target) {
                public String getName() {
                    return element.isTemporary() ? "gui.xaero_right_click_waypoint_restore" : (element.isDisabled() ? "gui.xaero_right_click_waypoint_enable" : "gui.xaero_right_click_waypoint_disable");
                }

                public void onAction(GuiScreen screen) {
                    if (element.isTemporary()) {
                        SupportMods.xaeroMinimap.toggleTemporaryWaypoint(element);
                    } else {
                        SupportMods.xaeroMinimap.disableWaypoint(element);
                    }
                }
            }).setNameFormatArgs("H"));
            rightClickOptions.add((new RightClickOption("", rightClickOptions.size(), target) {
                public String getName() {
                    return element.isTemporary() ? "gui.xaero_right_click_waypoint_delete_confirm" : "gui.xaero_right_click_waypoint_delete";
                }

                public void onAction(GuiScreen screen) {
                    if (element.isTemporary()) {
                        SupportMods.xaeroMinimap.deleteWaypoint(element);
                    } else {
                        SupportMods.xaeroMinimap.toggleTemporaryWaypoint(element);
                    }
                }
            }).setNameFormatArgs("DEL"));
        } else {
            IWaypoint serverWaypoint = (IWaypoint) element;
            List<WaypointOption> options = serverWaypoint.getOptions();
            if (options == null || options.isEmpty()) {
                return rightClickOptions;
            }
            for (int idx = 0; idx < options.size(); idx++) {
                WaypointOption option = options.get(idx);
                int finalIdx = idx;
                String text = "§2" + option.initials + "§r " + option.text;
                rightClickOptions.add(new DropDownSlot(text, rightClickOptions.size(), target, true) {
                    public void onAction(GuiScreen screen) {
                        SupportMinimap.getMain().getMessageHandler().sendToServer(
                                new OptionSelectedPacket(serverWaypoint.getId(), finalIdx)
                        );
                    }
                });
            }
        }
        return rightClickOptions;
    }

}
