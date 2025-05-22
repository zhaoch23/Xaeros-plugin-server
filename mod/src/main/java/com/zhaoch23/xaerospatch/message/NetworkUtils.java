package com.zhaoch23.xaerospatch.message;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

public class NetworkUtils {

    // Helpers to write/read length‐prefixed UTF-8 strings
    public static void writeString(ByteBuf buf, String s) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }

    public static String readString(ByteBuf buf) {
        int len = buf.readInt();
        byte[] bytes = new byte[len];
        buf.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

}
