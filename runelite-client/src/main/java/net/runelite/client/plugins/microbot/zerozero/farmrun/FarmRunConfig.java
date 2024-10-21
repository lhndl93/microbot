package net.runelite.client.plugins.microbot.zerozero.farmrun;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.plugins.microbot.farming.enums.FarmingMaterial;

@ConfigGroup("farmrun")
public interface FarmRunConfig extends Config {

    @ConfigItem(
            keyName = "farmingMaterial",
            name = "Select Farming Material",
            description = "Select the type of tree or fruit tree to plant"
    )
    default FarmingMaterial farmingMaterial() {
        return FarmingMaterial.MAGIC_TREE;  // Default to Magic Tree
    }

    @ConfigItem(
            keyName = "teleportEnabled",
            name = "Enable Teleports",
            description = "Enable or disable teleportation to tree patches."
    )
    default boolean TELEPORT() {
        return true;
    }
}
