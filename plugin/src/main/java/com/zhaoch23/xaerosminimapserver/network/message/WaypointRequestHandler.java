package com.zhaoch23.xaerosminimapserver.network.message;

import com.zhaoch23.xaerosminimapserver.XaerosMinimapServer;
import com.zhaoch23.xaerosminimapserver.network.NetworkHandler;
import io.netty.buffer.ByteBuf;
import org.bukkit.entity.Player;

public class WaypointRequestHandler implements IClientMessageHandler {

    @Override
    public void handle(Player player, ByteBuf buf) {
        XaerosMinimapServer.getWaypointManager().sendWaypointsToPlayer(player);
    }

    @Override
    public byte getDiscriminator() {
        return 6;
    }
}
