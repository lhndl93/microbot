package net.runelite.client.plugins.microbot.zerozero.ironmantools.util;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.util.ColorUtil;

import java.awt.*;
import java.util.Map;

public class AIOHelper {
    private static String lastChatMessage = "";

    /**
     * Logs a message to the chat only if it is different from the last logged message.
     *
     * @param message The message to log to the chat.
     */
    public static void logOnceToChat(String message, String type) {
        if (!message.equals(lastChatMessage)) {
            String formattedMessage = message;

            Map<String, Color> typeToColor = Map.of(
                    "error", Color.RED,
                    "success", Color.GREEN,
                    "info", Color.BLUE
            );

            Color color = type != null ? typeToColor.get(type.toLowerCase()) : null;

            if (color != null) {
                formattedMessage = ColorUtil.wrapWithColorTag(message, color);
            }

            Microbot.log(formattedMessage);
            lastChatMessage = message;
        }
    }
}