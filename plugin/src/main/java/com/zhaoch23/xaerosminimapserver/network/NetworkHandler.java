package com.zhaoch23.xaerosminimapserver.network;

import com.zhaoch23.xaerosminimapserver.XaerosMinimapServer;
import com.zhaoch23.xaerosminimapserver.network.message.IClientMessageHandler;
import com.zhaoch23.xaerosminimapserver.network.message.IServerMessagePacket;
import com.zhaoch23.xaerosminimapserver.network.message.OptionSelectedHandler;
import com.zhaoch23.xaerosminimapserver.network.message.WaypointRequestHandler;
import com.zhaoch23.xaerosminimapserver.waypoint.option.WaypointOption;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkHandler implements PluginMessageListener {

    public static final String CHANNEL = "xaerominimap:main";
    private final XaerosMinimapServer plugin;
    private final Map<Byte, IClientMessageHandler> handlers = new HashMap<>();

    public NetworkHandler(XaerosMinimapServer plugin) {
        this.plugin = plugin;
        plugin.getServer().getMessenger()
                .registerOutgoingPluginChannel(plugin, NetworkHandler.CHANNEL);

        plugin.getServer().getMessenger()
                .registerIncomingPluginChannel(plugin, NetworkHandler.CHANNEL, this);

        // Register all the handlers
        registerHandler(new WaypointRequestHandler());
        registerHandler(new OptionSelectedHandler());
    }

    public static String readString(ByteBuf buf) {
        int len = buf.readInt();
        byte[] bytes = new byte[len];
        buf.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static void writeString(ByteBuf buf, String s) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }

    public static void writeOptions(ByteBuf buf, List<WaypointOption> options) {
        buf.writeInt(options.size());
        for (WaypointOption option : options) {
            writeString(buf, option.initials);
            writeString(buf, option.text);
        }
    }

    public void registerHandler(IClientMessageHandler msg) {
        handlers.put(msg.getDiscriminator(), msg);
    }

    public void unload() {
        plugin.getServer().getMessenger()
                .unregisterOutgoingPluginChannel(plugin, CHANNEL);
        plugin.getServer().getMessenger()
                .unregisterIncomingPluginChannel(plugin, CHANNEL, this);
    }

    public void sendToPlayer(Player player, IServerMessagePacket message) {
        try {
            byte[] payload = message.toBytes();
            byte[] data = new byte[1 + payload.length];
            data[0] = message.getDiscriminator();
            System.arraycopy(payload, 0, data, 1, payload.length);

            // send it
            player.sendPluginMessage(plugin, CHANNEL, data);
            plugin.getLogger().info("Send waypoints to " + player.getName());
        } catch (Exception e) {
            plugin.getLogger().warning(
                    "Failed to sync waypoints with " + player.getName()
            );
        }
    }

    public void sendToAllPlayers(IServerMessagePacket message) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            sendToPlayer(player, message);
        }
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        Bukkit.getLogger().info("Received packet from " + player.getName() + " on channel " + channel);

        if (!CHANNEL.equals(channel)) return;

        // wrap the raw bytes in a ByteBuf
        ByteBuf buf = Unpooled.wrappedBuffer(message);
        try {
            // first byte = discriminator
            byte disc = buf.readByte();

            IClientMessageHandler handler = handlers.get(disc);
            if (handler != null) {
                handler.handle(player, buf);
            } else {
                plugin.getLogger().warning(
                        "No IClientMessage registered for discriminator " + disc
                );
            }
        } finally {
            // release if needed (Unpooled.wrappedBuffer is unpooled, so this is safe/no-op)
            buf.release();
        }
    }

}
