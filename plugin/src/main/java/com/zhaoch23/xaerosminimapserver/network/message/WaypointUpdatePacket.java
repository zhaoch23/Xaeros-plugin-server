package com.zhaoch23.xaerosminimapserver.network.message;

import com.zhaoch23.xaerosminimapserver.network.NetworkHandler;
import com.zhaoch23.xaerosminimapserver.waypoint.Waypoint;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Map;

public class WaypointUpdatePacket implements IServerMessagePacket {

    private final Map<String, Waypoint> waypoints;

    private final String worldName;


    public WaypointUpdatePacket(Map<String, Waypoint> waypoints, String worldName) {
        this.waypoints = waypoints;
        this.worldName = worldName;
    }

    public Map<String, Waypoint> getWaypoints() {
        return waypoints;
    }

    @Override
    public byte getDiscriminator() {
        return 5;
    }

    public void toBytes(ByteBuf buf) {
        NetworkHandler.writeString(buf, worldName);
        buf.writeInt(waypoints.size());
        for (Map.Entry<String, Waypoint> entry : waypoints.entrySet()) {
            Waypoint waypoint = entry.getValue();
            NetworkHandler.writeString(buf, entry.getKey());
            buf.writeInt(waypoint.x);
            buf.writeInt(waypoint.y);
            buf.writeInt(waypoint.z);
            buf.writeBoolean(waypoint.transparent);
            buf.writeInt(waypoint.color.ordinal());
            NetworkHandler.writeString(buf, waypoint.name);
            NetworkHandler.writeString(buf, waypoint.initials);
            NetworkHandler.writeString(buf, waypoint.hoverText);
            NetworkHandler.writeString(buf, waypoint.description);
            NetworkHandler.writeOptions(buf, waypoint.options);
        }
    }

    @Override
    public byte[] toBytes() {
        ByteBuf buf = Unpooled.buffer();
        toBytes(buf);
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        return bytes;
    }

}
