package com.zhaoch23.xaerospatch.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import xaero.common.message.MinimapMessage;
import xaero.common.message.server.ServerMessageConsumer;

public class WaypointRequestPacket extends MinimapMessage<WaypointRequestPacket> {

    String worldName;

    public WaypointRequestPacket() {
    }

    public WaypointRequestPacket(String worldName) {
        this.worldName = worldName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {
        // Write the world name to the buffer
        NetworkUtils.writeString(buf, worldName);
    }

    public static class ServerHandler implements ServerMessageConsumer<WaypointRequestPacket> {

        @Override
        public void handle(MinecraftServer minecraftServer, EntityPlayerMP entityPlayerMP, WaypointRequestPacket waypointRequestPacket) {
            // I don't know why but this is required lol
        }
    }

}
