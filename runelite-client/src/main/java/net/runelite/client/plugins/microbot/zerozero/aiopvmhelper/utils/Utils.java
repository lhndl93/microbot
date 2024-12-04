package net.runelite.client.plugins.microbot.zerozero.aiopvmhelper.utils;

import net.runelite.api.*;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum;
import net.runelite.client.plugins.microbot.zerozero.aiopvmhelper.AIOPvmConfig;
import net.runelite.client.util.ColorUtil;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Utils {
    private static String lastLoggedMessage = "";

    public static boolean isNearNPC(int id) {
        NPC currentNPC = Rs2Npc.getNpc(id);
        if (currentNPC == null || currentNPC.isDead()) return false;
        int distance = Microbot.getClient().getLocalPlayer().getLocalLocation().distanceTo(currentNPC.getLocalLocation());
        System.out.println(distance);
        return false;
    }

    public static void logOnceToChat(String title, String message, String type, boolean isDebug, AIOPvmConfig config) {
        if (!isDebug || config.debugLogging()) {
            if (!message.equals(lastLoggedMessage)) {
                Map<String, Color> typeToColor = Map.of(
                        "error", Color.RED,
                        "success", Color.GREEN,
                        "info", Color.BLUE
                );
                String coloredTitle = title;
                if (type != null) {
                    Color color = typeToColor.get(type.toLowerCase());
                    if (color != null) {
                        coloredTitle = ColorUtil.wrapWithColorTag(title, color);
                    }
                }
                String formattedMessage = (coloredTitle != null ? coloredTitle : "") + ": " + message;
                Microbot.log(formattedMessage);
                lastLoggedMessage = message;
            }
        }
    }
}