package com.zhaoch23.xaerosminimapserver;

import com.zhaoch23.xaerosminimapserver.network.message.ClientboundRulePacket;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class Setting {

    private final ClientboundRulePacket clientboundRulePacket = new ClientboundRulePacket(
            true,
            true,
            true
    );

    public ClientboundRulePacket getClientboundRulePacket() {
        return clientboundRulePacket;
    }

    public void loadConfig(FileConfiguration config) {
        ConfigurationSection rules = config.getConfigurationSection("rules");
        clientboundRulePacket.allowCaveModeOnServer = rules.getBoolean("allowCaveModeOnServer");
        clientboundRulePacket.allowNetherCaveModeOnServer = rules.getBoolean("allowNetherCaveModeOnServer");
        clientboundRulePacket.allowRadarOnServer = rules.getBoolean("allowRadarOnServer");
    }

    public void sendConfigToPlayers() {
        XaerosMinimapServer
                .getNetworkHandler()
                .sendToAllPlayers(clientboundRulePacket);
    }
}
