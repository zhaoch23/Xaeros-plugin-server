package com.zhaoch23.xaerospatch.common;

import io.netty.buffer.ByteBuf;

public class WaypointServerConfig {
    public boolean serverWaypoint;
    public boolean canShare;
    public boolean canDisable;

    public WaypointServerConfig(boolean serverWaypoint, boolean canShare, boolean canDisable) {
        this.serverWaypoint = serverWaypoint;
        this.canShare = canShare;
        this.canDisable = canDisable;
    }

    public static WaypointServerConfig createLocal() {
        return new WaypointServerConfig(false, true, true);
    }

    public static WaypointServerConfig createRemote() {
        return new WaypointServerConfig(true, false, false);
    }

    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(serverWaypoint);
        buf.writeBoolean(canShare);
        buf.writeBoolean(canDisable);
    }

    public void fromBytes(ByteBuf buf) {
        serverWaypoint = buf.readBoolean();
        canShare = buf.readBoolean();
        canDisable = buf.readBoolean();
    }

    public void copyTo(WaypointServerConfig other) {
        other.serverWaypoint = this.serverWaypoint;
        other.canShare = this.canShare;
        other.canDisable = this.canDisable;
    }

}
