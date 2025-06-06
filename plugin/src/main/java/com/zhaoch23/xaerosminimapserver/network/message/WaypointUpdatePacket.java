package com.zhaoch23.xaerosminimapserver.network.message;

import com.zhaoch23.xaerosminimapserver.network.NetworkHandler;
import com.zhaoch23.xaerosminimapserver.waypoint.Waypoint;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.List;

public class WaypointUpdatePacket implements IServerMessagePacket {

    private final List<Waypoint> waypoints;

    private final String worldName;


    public WaypointUpdatePacket(List<Waypoint> waypoints, String worldName) {
        this.waypoints = waypoints;
        this.worldName = worldName;
    }


    @Override
    public byte getDiscriminator() {
        return 5;
    }

    public void toBytes(ByteBuf buf) {
        NetworkHandler.writeString(buf, worldName);
        buf.writeInt(waypoints.size());
        for (Waypoint waypoint : waypoints) {
            buf.writeInt(waypoint.x);
            buf.writeInt(waypoint.y);
            buf.writeInt(waypoint.z);
            NetworkHandler.writeString(buf, waypoint.name);
            NetworkHandler.writeString(buf, waypoint.initials);
            buf.writeInt(waypoint.color.ordinal());
            buf.writeBoolean(waypoint.transparent);
            NetworkHandler.writeString(buf, waypoint.description);
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
