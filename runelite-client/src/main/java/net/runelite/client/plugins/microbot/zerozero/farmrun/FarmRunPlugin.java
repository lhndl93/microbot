package net.runelite.client.plugins.microbot.zerozero.farmrun;

import com.google.inject.Provides;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;

import javax.inject.Inject;

@PluginDescriptor(
        name = "Farm Run",
        description = "Automates tree and fruit tree farming runs",
        tags = {"farming", "tree", "run"}
)
public class FarmRunPlugin extends Plugin {

    @Inject
    private FarmRunScript script;

    @Inject
    private ConfigManager configManager;

    private FarmRunConfig config;

    @Override
    protected void startUp() throws Exception {
        config = configManager.getConfig(FarmRunConfig.class);  // Get the configuration instance
        if (config.startStop()) {
            script.run(config);  // If started via the config, run the script
        }
    }

    @Override
    protected void shutDown() throws Exception {
        script.shutdown();
    }

    // This method is used to provide the config instance for the plugin
    @Provides
    FarmRunConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(FarmRunConfig.class);
    }

    // Listen to config changes (Start/Stop button)
    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (!event.getGroup().equals("farmrun")) {
            return;
        }

        if (event.getKey().equals("startStop")) {
            if (config.startStop()) {
                script.run(config);  // Start the script when toggled on
            } else {
                script.shutdown();  // Stop the script when toggled off
            }
        }
    }
}
