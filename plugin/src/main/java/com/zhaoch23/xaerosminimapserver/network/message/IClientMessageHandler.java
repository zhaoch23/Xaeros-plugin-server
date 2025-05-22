package com.zhaoch23.xaerosminimapserver.network.message;

import io.netty.buffer.ByteBuf;
import org.bukkit.entity.Player;

public interface IClientMessageHandler {

    void handle(Player player, ByteBuf packet);

    byte getDiscriminator();

}
