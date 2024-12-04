package net.runelite.client.plugins.microbot.zerozero.aiopvmhelper;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.zerozero.aiopvmhelper.models.DAMAGE_PRAYERS;
import net.runelite.client.plugins.microbot.zerozero.aiopvmhelper.models.PRAY_MODE;
import net.runelite.client.plugins.microbot.zerozero.aiopvmhelper.utils.Utils;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;

@PluginDescriptor(
        name = PluginDescriptor.zerozero + "AIO PVM",
        description = "AIO PVM Helper",
        tags = {"zerozero", "assist", "bossing", "pvm", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class AIOPvmPlugin extends Plugin {
    @Inject
    public AIOPvmConfig config;
    @Provides
    AIOPvmConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(AIOPvmConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private AIOPvmOverlay exampleOverlay;

    @Inject
    private ClientThread clientThread;

    @Inject
    AIOPvmScript aioPvmScript;


        @Subscribe
        public void onGameTick(GameTick gameTick) {
            aioPvmScript.handlePrayers();
        }


    @Override
    protected void startUp() throws Exception {
        if (overlayManager != null) {
            overlayManager.add(exampleOverlay);
        }
        aioPvmScript.run(config);
    }

    protected void shutDown() {
        aioPvmScript.shutdown();
        overlayManager.remove(exampleOverlay);
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (!event.getGroup().equals("aiopvmhelper")) {
            return;
        }

        String logMessage = "";

        switch (event.getKey()) {
            case "prayermode":
                handlePrayerModeChange(config.PRAYER_MODE());
                break;
            case "damageprayer":
                handleDamagePrayerChange(config.DAMAGE_PRAYER());
                break;
            case "Scurrius":
                if (config.isScurriusOn()) {
                    logMessage = "Scurrius assistance enabled.";
                } else {
                    logMessage = "Scurrius assistance disabled.";
                }
                Utils.logOnceToChat("AIOPVM", logMessage, "info", true, config);
                Microbot.status = logMessage;
                break;
            case "DerangedArcheo":
                if (config.isArcheoOn()) {
                    logMessage = "Deranged Archaeologist assistance enabled.";
                } else {
                    logMessage = "Deranged Archaeologist assistance disabled.";
                }
                Utils.logOnceToChat("AIOPVM", logMessage, "info", true, config);
                Microbot.status = logMessage;
                break;
            case "Obor":
                if (config.isOborOn()) {
                    logMessage = "Obor assistance enabled.";
                } else {
                    logMessage = "Obor assistance disabled.";
                }
                Utils.logOnceToChat("AIOPVM", logMessage, "info", true, config);
                Microbot.status = logMessage;
                break;
            case "debugLogging":
                if (config.debugLogging()) {
                    logMessage = "Debug logging enabled.";
                } else {
                    logMessage = "Debug logging disabled.";
                }
                Utils.logOnceToChat("AIOPVM", logMessage, "info", true, config);
                break;
            default:
                logMessage = "Unknown configuration changed: " + event.getKey();
                Utils.logOnceToChat("AIOPVM", logMessage, "error", true, config);
        }
    }

    private void handlePrayerModeChange(PRAY_MODE mode) {
        String logMessage;
        switch (mode) {
            case NONE:
                aioPvmScript.turnOffPrayers();
                logMessage = "Prayers disabled.";
                break;
            case VISUAL:
                logMessage = "Visual prayer mode enabled.";
                break;
            case AUTO:
                logMessage = "Auto prayer mode enabled.";
                break;
            case FLICK:
                logMessage = "Flick prayer mode enabled.";
                break;
            default:
                logMessage = "Unknown prayer mode.";
        }

        Utils.logOnceToChat("AIOPVM", logMessage, "info", true, config);
        Microbot.status = logMessage;
    }

    private void handleDamagePrayerChange(DAMAGE_PRAYERS prayer) {
        aioPvmScript.updateDamagePrayer(prayer);
        String logMessage = "Damage prayer updated to: " + prayer.toString();
        Utils.logOnceToChat("AIOPVM", logMessage, "info", true, config);
        Microbot.status = logMessage;
    }

}
