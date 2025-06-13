package com.zhaoch23.xaerospatch.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import xaero.common.message.MinimapMessage;
import xaero.common.message.server.ServerMessageConsumer;

public class OptionSelectedPacket extends MinimapMessage<OptionSelectedPacket> {

    private String waypointId;
    private int optionIndex;

    public OptionSelectedPacket() {
    }

    public OptionSelectedPacket(String waypointId, int optionIndex) {
        this.waypointId = waypointId;
        this.optionIndex = optionIndex;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkUtils.writeString(buf, waypointId);
        buf.writeInt(optionIndex);
    }

    public static class ServerHandler implements ServerMessageConsumer<OptionSelectedPacket> {
        @Override
        public void handle(MinecraftServer minecraftServer, EntityPlayerMP entityPlayerMP, OptionSelectedPacket optionSelectedPacket) {
        }
    }
}
