package com.zhaoch23.xaerosminimapserver.network.message;

public class ClientboundRulePacket implements IServerMessagePacket {

    public boolean allowCaveModeOnServer;
    public boolean allowNetherCaveModeOnServer;
    public boolean allowRadarOnServer;

    public ClientboundRulePacket(boolean allowCaveModeOnServer, boolean allowNetherCaveModeOnServer, boolean allowRadarOnServer) {
        this.allowCaveModeOnServer = allowCaveModeOnServer;
        this.allowNetherCaveModeOnServer = allowNetherCaveModeOnServer;
        this.allowRadarOnServer = allowRadarOnServer;
    }

    @Override
    public byte[] toBytes() {
        byte[] bytes = new byte[3];
        bytes[0] = (byte) (allowCaveModeOnServer ? 1 : 0);
        bytes[1] = (byte) (allowNetherCaveModeOnServer ? 1 : 0);
        bytes[2] = (byte) (allowRadarOnServer ? 1 : 0);
        return bytes;
    }

    @Override
    public byte getDiscriminator() {
        return 4;
    }
}
