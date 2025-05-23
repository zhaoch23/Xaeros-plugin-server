package com.zhaoch23.xaerospatch.mixins.message;

import io.netty.buffer.ByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(
        value = {xaero.common.message.basic.ClientboundRulesPacket.class},
        remap = false
)
public abstract class ClientboundRulesPacketMixin {

    @Shadow
    public boolean allowCaveModeOnServer;
    @Shadow
    public boolean allowNetherCaveModeOnServer;
    @Shadow
    public boolean allowRadarOnServer;

    /**
     * @author zhaoch23
     * @reason Rewrite to use simpler methods to communicate with the server
     */
    @Overwrite
    public void fromBytes(ByteBuf buf) {
        allowCaveModeOnServer = buf.readBoolean();
        allowNetherCaveModeOnServer = buf.readBoolean();
        allowRadarOnServer = buf.readBoolean();
    }

    /**
     * @author zhaoch23
     * @reason Rewrite to use simpler methods to communicate with the server
     */
    @Overwrite
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(allowCaveModeOnServer);
        buf.writeBoolean(allowNetherCaveModeOnServer);
        buf.writeBoolean(allowRadarOnServer);
    }
}
