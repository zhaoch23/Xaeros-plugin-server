package com.zhaoch23.xaerospatch.message;

import com.zhaoch23.xaerospatch.XaerosPatch;
import com.zhaoch23.xaerospatch.common.IWaypoint;
import com.zhaoch23.xaerospatch.common.WaypointServerConfig;
import com.zhaoch23.xaerospatch.common.WaypointUtils;
import io.netty.buffer.ByteBuf;
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
            Waypoint waypoint = new Waypoint(x, y, z, name, initials, color);

            // TODO: Make this configurable
            WaypointServerConfig serverConfig = ((IWaypoint) waypoint).getServerConfig();
            serverConfig.serverWaypoint = true;
            serverConfig.canShare = true;
            serverConfig.canDisable = false;

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

                // Add the Waypoints to default set
                // TODO: Make this configurable
                WaypointSet waypointSet = world.getWaypointSet("gui.xaero_default");
                if (waypointSet == null) {
                    XaerosPatch.getLogger().error("WaypointSet is null, cannot update waypoints");
                    return;
                }

                // Remove all remote waypoints
                WaypointUtils.clearWaypoints(waypointSet, true);

                waypointSet.addAll(packet.waypoints);

                XaerosPatch.getLogger()
                        .info("Added " + packet.waypoints.size() + " waypoints to the set " + waypointSet.getName());
            } catch (Exception e) {
                XaerosPatch.getLogger().error("Error updating waypoints: " + e.getMessage(), e);
            }

        }
    }
}