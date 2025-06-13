package com.zhaoch23.xaerospatch.message;

import com.zhaoch23.xaerospatch.common.WaypointOption;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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

    public static List<WaypointOption> readOptions(ByteBuf buf) {
        int optionCount = buf.readInt();
        List<WaypointOption> options = new ArrayList<>();
        for (int i = 0; i < optionCount; i++) {
            String initials = readString(buf);
            String text = readString(buf);
            options.add(new WaypointOption(initials, text));
        }
        return options;
    }

}
