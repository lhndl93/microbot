package net.runelite.client.plugins.microbot.zerozero.gauntletflicker;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.zerozero + "Gauntlet Flicker",
        description = "Automates prayer flicking and weapon switching during Gauntlet boss fights",
        tags = {"gauntlet", "flicker", "weapon", "switch"},
        enabledByDefault = false
)
@Slf4j
public class GauntletFlickerPlugin extends Plugin {

    @Inject
    private GauntletFlickerConfig config;

    @Provides
    GauntletFlickerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(GauntletFlickerConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private GauntletFlickerOverlay gauntletFlickerOverlay;

    @Inject
    GauntletFlickerScript gauntletFlickerScript;

    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(gauntletFlickerOverlay);
        }
        gauntletFlickerScript.run(config); // Pass config if needed
    }

    @Override
    protected void shutDown() {
        gauntletFlickerScript.shutdown();
        overlayManager.remove(gauntletFlickerOverlay);
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        gauntletFlickerScript.onGameTick();
    }
}
