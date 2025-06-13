package com.zhaoch23.xaerosminimapserver.network.message;

import com.zhaoch23.xaerosminimapserver.XaerosMinimapServer;
import com.zhaoch23.xaerosminimapserver.network.NetworkHandler;
import io.netty.buffer.ByteBuf;
import org.bukkit.entity.Player;

public class OptionSelectedHandler implements IClientMessageHandler {

    @Override
    public void handle(Player player, ByteBuf buf) {
        String waypointId = NetworkHandler.readString(buf);
        int optionIndex = buf.readInt();
        XaerosMinimapServer.getWaypointManager().onWaypointOptionSelected(player, waypointId, optionIndex);
    }

    @Override
    public byte getDiscriminator() {
        return 7;
    }
}
