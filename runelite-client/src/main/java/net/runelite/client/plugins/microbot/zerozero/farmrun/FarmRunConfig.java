package net.runelite.client.plugins.microbot.zerozero.farmrun;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.plugins.microbot.zerozero.farmrun.enums.CompostType;
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
            keyName = "normalTree",
            name = "Normal Tree",
            description = "The type of tree to plant.",
            position = 1
    )
    default FarmingMaterial normalTree() {
        return FarmingMaterial.MAGIC_TREE;  // Default to Magic Tree
    }

    @ConfigItem(
            keyName = "fruiteTree",
            name = "Fruit Tree",
            description = "The type of fruit tree to plant.",
            position = 2
    )
    default FarmingMaterial fruitTree() {
        return FarmingMaterial.PALM_TREE;  // Default to Magic Tree
    }

    @ConfigItem(
            keyName = "protectionMode",
            name = "Protection Mode",
            description = "Choose between protecting the patch with items or using compost.",
            position = 3
    )
    default ProtectionMode protectionMode() {
        return ProtectionMode.PROTECT_PATCH_WITH_ITEMS;  // Default to protecting with items
    }

    @ConfigItem(
            keyName = "compostType",
            name = "Compost Type",
            description = "Choose the type of compost to use.",
            position = 4
    )
    default CompostType compostType() {
        return CompostType.ULTRA_COMPOST;  // Default to Ultra Compost
    }

    @ConfigItem(
            keyName = "varrockPatch",
            name = "Enable Varrock Patch",
            description = "Enable or disable the Varrock tree patch.",
            position = 6
    )
    default boolean varrockPatch() {
        return true;  // Default to enabled
    }

    @ConfigItem(
            keyName = "faladorPatch",
            name = "Enable Falador Patch",
            description = "Enable or disable the Falador tree patch.",
            position = 7
    )
    default boolean faladorPatch() {
        return true;  // Default to enabled
    }

    @ConfigItem(
            keyName = "lumbridgePatch",
            name = "Enable Lumbridge Patch",
            description = "Enable or disable the Lumbridge tree patch.",
            position = 8
    )
    default boolean lumbridgePatch() {
        return true;  // Default to enabled
    }

    @ConfigItem(
            keyName = "taverleyPatch",
            name = "Enable Taverley Patch",
            description = "Enable or disable the Taverley tree patch.",
            position = 9
    )
    default boolean taverleyPatch() {
        return true;  // Default to enabled
    }

    @ConfigItem(
            keyName = "catherbyPatch",
            name = "Enable Catherby Patch",
            description = "Enable or disable the Catherby tree patch.",
            position = 10
    )
    default boolean catherbyPatch() {
        return true;  // Default to enabled
    }

    @ConfigItem(
            keyName = "gnomePatch",
            name = "Enable Gnome Patch",
            description = "Enable or disable the Gnome tree patch.",
            position = 11
    )
    default boolean gnomePatch() {
        return true;  // Default to enabled
    }

    @ConfigItem(
            keyName = "gnomefruitPatch",
            name = "Enable Gnome Fruit Patch",
            description = "Enable or disable the Gnome tree patch.",
            position = 12
    )
    default boolean gnomefruitPatch() {
        return true;  // Default to enabled
    }

    @ConfigItem(
            keyName = "startStop",
            name = "Start/Stop Farm Run",
            description = "Toggle to start or stop the farm run.",
            position = 13
    )
    default boolean startStop() {
        return false;  // Default to not started
    }

}



