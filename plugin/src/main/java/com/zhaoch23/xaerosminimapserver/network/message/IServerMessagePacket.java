package com.zhaoch23.xaerosminimapserver.network.message;

public interface IServerMessagePacket {

    byte[] toBytes();

    byte getDiscriminator();
}
