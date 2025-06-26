package com.zhaoch23.xaerosminimapserver;

import com.zhaoch23.xaerosminimapserver.network.message.ClientboundRulePacket;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class Setting {

    private final ClientboundRulePacket clientboundRulePacket = new ClientboundRulePacket(
            true,
            true,
            true
    );

    private String adminPermission = "xaerosminimapwaypoint.admin";

    public ClientboundRulePacket getClientboundRulePacket() {
        return clientboundRulePacket;
    }

    public void loadConfig(FileConfiguration config) {
        ConfigurationSection rules = config.getConfigurationSection("rules");
        clientboundRulePacket.allowCaveModeOnServer = rules.getBoolean("allowCaveModeOnServer");
        clientboundRulePacket.allowNetherCaveModeOnServer = rules.getBoolean("allowNetherCaveModeOnServer");
        clientboundRulePacket.allowRadarOnServer = rules.getBoolean("allowRadarOnServer");

        adminPermission = config.getString("admin.admin-permission", adminPermission);
    }

    public String getAdminPermission() {
        return adminPermission;
    }

    public void sendConfigToPlayers() {
        XaerosMinimapServer
                .getNetworkHandler()
                .sendToAllPlayers(clientboundRulePacket);
    }

    public void sendConfigToPlayer(Player player) {
        XaerosMinimapServer
                .getNetworkHandler()
                .sendToPlayer(player, clientboundRulePacket);
    }
}
