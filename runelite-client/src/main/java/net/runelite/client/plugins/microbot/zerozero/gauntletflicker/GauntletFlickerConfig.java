package net.runelite.client.plugins.microbot.zerozero.gauntletflicker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("gauntletflicker")
public interface GauntletFlickerConfig extends Config {

    @ConfigItem(
            keyName = "startScript",
            name = "Start Script",
            description = "Toggle to start/stop the Gauntlet Flicker script"
    )
    default boolean startScript() {
        return false;
    }
}
