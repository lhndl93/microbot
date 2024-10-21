package net.runelite.client.plugins.microbot.zerozero.farmrun;

import com.google.inject.Provides;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;

@PluginDescriptor(
        name = "Farm Run",
        description = "Automates tree and fruit tree farming runs",
        tags = {"farming", "tree", "run"}
)
public class FarmRunPlugin extends Plugin {

    @Inject
    private FarmRunOverlay overlay;

    @Inject
    private FarmRunScript script;

    @Override
    protected void startUp() throws Exception {
        overlay.initialize();
        script.run(getConfig(FarmRunConfig.class));
    }

    @Override
    protected void shutDown() throws Exception {
        overlay.shutdown();
        script.shutdown();
    }

    @Provides
    FarmRunConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(FarmRunConfig.class);
    }
}
