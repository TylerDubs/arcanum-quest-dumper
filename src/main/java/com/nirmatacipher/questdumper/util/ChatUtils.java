package com.nirmatacipher.questdumper.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class ChatUtils {
    private ChatUtils() {
    }

    public static void info(String message) {
        send(message);
    }

    public static void success(String message) {
        send("SUCCESS: " + message);
    }

    public static void warn(String message) {
        send("WARNING: " + message);
    }

    public static void error(String message) {
        send("ERROR: " + message);
    }

    public static void send(String message) {
        Minecraft mc = Minecraft.getInstance();
        String full = "[QuestDumper] " + message;
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal(full), false);
        } else {
            System.out.println(full);
        }
    }
}
