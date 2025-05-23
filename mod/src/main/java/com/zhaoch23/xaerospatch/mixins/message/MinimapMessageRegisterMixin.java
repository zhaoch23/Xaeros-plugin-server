package com.zhaoch23.xaerospatch.mixins.message;

import com.zhaoch23.xaerospatch.XaerosPatch;
import com.zhaoch23.xaerospatch.message.WaypointRequestPacket;
import com.zhaoch23.xaerospatch.message.WaypointUpdatePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.common.message.MinimapMessageHandler;


@Mixin(
        value = {xaero.common.message.MinimapMessageRegister.class},
        remap = false
)
public abstract class MinimapMessageRegisterMixin {

    @Inject(
            method = "register",
            at = @At("RETURN"),
            remap = false
    )
    public void register(MinimapMessageHandler messageHandler, CallbackInfo ci) {
        XaerosPatch.getLogger().debug("Registering new packets with MinimapMessageHandler");
        messageHandler.register(5, WaypointUpdatePacket.class, null, new WaypointUpdatePacket.ClientHandler());
        messageHandler.register(6, WaypointRequestPacket.class, new WaypointRequestPacket.ServerHandler(), null);
    }

}
