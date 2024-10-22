package net.runelite.client.plugins.microbot.zerozero.farmrun;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.plugins.microbot.zerozero.farmrun.enums.FarmingMaterial;
import net.runelite.client.plugins.microbot.zerozero.farmrun.enums.ProtectionMode;

@ConfigGroup("farmrun")
public interface FarmRunConfig extends Config {

    @ConfigItem(
            keyName = "teleportEnabled",
            name = "Enable Teleports",
            description = "Enable or disable teleportation to tree patches.",
            position = 0
    )
    default boolean TELEPORT() {
        return true;
    }

    @ConfigItem(
            keyName = "farmingMaterial",
            name = "Farming Material",
            description = "The type of tree or fruit tree to plant.",
            position = 1
    )
    default FarmingMaterial farmingMaterial() {
        return FarmingMaterial.MAGIC_TREE;  // Default to Magic Tree
    }

    @ConfigItem(
            keyName = "protectionMode",
            name = "Protection Mode",
            description = "Choose between protecting the patch with items or paying the NPC.",
            position = 2
    )
    default ProtectionMode protectionMode() {
        return ProtectionMode.PAY_TO_PROTECT;  // Default to Pay to Protect
    }

    @ConfigItem(
            keyName = "startStop",
            name = "Start/Stop Farm Run",
            description = "Toggle to start or stop the farm run.",
            position = 2
    )
    default boolean startStop() {
        return false;  // Default to not started
    }
}

