package com.zhaoch23.xaerospatch.message;

import com.zhaoch23.xaerospatch.RemoteWaypoint;
import com.zhaoch23.xaerospatch.XaerosPatch;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import xaero.common.message.MinimapMessage;
import xaero.common.message.client.ClientMessageConsumer;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.WaypointColor;
import xaero.hud.minimap.waypoint.set.WaypointSet;
import xaero.hud.minimap.world.MinimapWorld;

import java.util.ArrayList;
import java.util.List;

public class WaypointUpdatePacket extends MinimapMessage<WaypointUpdatePacket> {

    private final List<Waypoint> waypoints = new ArrayList<>();
    private String worldName;

    public WaypointUpdatePacket() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        worldName = NetworkUtils.readString(buf);
        int count = buf.readInt();
        for (int i = 0; i < count; i++) {
            int x = buf.readInt();
            int y = buf.readInt();
            int z = buf.readInt();
            String name = NetworkUtils.readString(buf);
            String initials = NetworkUtils.readString(buf);
            WaypointColor color = WaypointColor.values()[buf.readInt()];
            RemoteWaypoint waypoint = new RemoteWaypoint(x, y, z, name, initials, color);
            this.waypoints.add(waypoint);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    public static class ClientHandler implements ClientMessageConsumer<WaypointUpdatePacket> {
        @Override
        public void handle(WaypointUpdatePacket packet) {
            XaerosPatch.getLogger()
                    .info("Received waypoint packet with " +
                            packet.waypoints.size() +
                            " waypoints in dimension " +
                            packet.worldName
                    );

            // We're already on the main thread, execute directly
            try {
                MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
                if (session == null) {
                    XaerosPatch.getLogger().error("MinimapSession is null, cannot update waypoints");
                    return;
                }

                MinimapWorld world = session.getWorldManager().getCurrentWorld();
                if (world == null) {
                    XaerosPatch.getLogger().error("MinimapWorld is null, cannot update waypoints");
                    return;
                }

                WaypointSet waypointSet = world.getWaypointSet(world.getCurrentWaypointSetId());
                if (waypointSet == null) {
                    XaerosPatch.getLogger().error("WaypointSet is null, cannot update waypoints");
                    return;
                }

                // Remove all remote waypoints
                List<RemoteWaypoint> waypointsToRemove = new ArrayList<>();
                for (Waypoint waypoint : waypointSet.getWaypoints()) {
                    if (waypoint instanceof RemoteWaypoint) {
                        waypointsToRemove.add((RemoteWaypoint) waypoint);
                    }
                }
                for (RemoteWaypoint waypoint : waypointsToRemove) {
                    waypointSet.remove(waypoint);
                }

                waypointSet.addAll(packet.waypoints);

                XaerosPatch.getLogger()
                        .info("Added " + packet.waypoints.size() + " waypoints to the set " + waypointSet.getName());
            } catch (Exception e) {
                XaerosPatch.getLogger().error("Error updating waypoints: " + e.getMessage(), e);
            }

        }
    }
}