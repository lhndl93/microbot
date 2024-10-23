package net.runelite.client.plugins.microbot.zerozero.farmrun;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.plugins.microbot.zerozero.farmrun.enums.*;

@ConfigGroup("farmrun")
public interface FarmRunConfig extends Config {

    // General Section
    @ConfigSection(
            name = "General Settings",
            description = "General configurations for farm run.",
            position = 0,
            closedByDefault = false
    )
    String generalSection = "generalSection";

    @ConfigItem(
            keyName = "startStop",
            name = "Start/Stop Farm Run",
            description = "Toggle to start or stop the farm run.",
            position = 0,
            section = generalSection
    )
    default boolean startStop() {
        return false;  // Default to not started
    }

    @ConfigSection(
            name = "Extra Settings",
            description = "General configurations for farm run.",
            position = 1,
            closedByDefault = true
    )
    String extraSettings = "extraSettings";

    @ConfigItem(
            keyName = "returnAfterFinish",
            name = "Return to G/E after",
            description = "Return to G/E when finished the run",
            position = 1,
            section = extraSettings
    )
    default boolean returnAfterFinish() {
        return false;
    }

    @ConfigItem(
            keyName = "autoWeed",
            name = "Auto-weed (Tithe farm reward)",
            description = "Auto Weed from Tithe farm",
            position = 2,
            section = extraSettings
    )
    default boolean autoWeed() {
        return false;
    }

    @ConfigSection(
            name = "Teleport Options",
            description = "Configure teleport options",
            position = 2,
            closedByDefault = true
    )
    String teleportSettings = "teleportSettings";

    @ConfigItem(
            keyName = "teleportMethod",
            name = "Teleport Method",
            description = "Select whether to use Magic spells or Teleport Tablets for teleporting.",
            position = 0,
            section = teleportSettings
    )
    default TeleportMethod teleportMethod() {
        return TeleportMethod.MAGIC;
    }

    // Planting Settings Section
    @ConfigSection(
            name = "Planting Settings",
            description = "Settings for the type of tree or fruit tree to plant.",
            position = 3,
            closedByDefault = true
    )
    String plantingSection = "plantingSection";

    @ConfigItem(
            keyName = "normalTree",
            name = "Normal Tree",
            description = "The type of tree to plant.",
            position = 0,
            section = plantingSection
    )
    default NormalTreeMaterial normalTree() {
        return NormalTreeMaterial.MAGIC_TREE;  // Default to Magic Tree
    }

    @ConfigItem(
            keyName = "fruitTree",
            name = "Fruit Tree",
            description = "The type of fruit tree to plant.",
            position = 1,
            section = plantingSection
    )
    default FruitTreeMaterial fruitTree() {
        return FruitTreeMaterial.PALM_TREE;  // Default to Palm Tree
    }

    // Tree Patches Section
    @ConfigSection(
            name = "Tree Patches",
            description = "Enable or disable tree patches.",
            position = 4,
            closedByDefault = true
    )
    String treePatchesSection = "treePatchesSection";

    @ConfigItem(
            keyName = "varrockPatch",
            name = "Enable Varrock Patch",
            description = "Enable or disable the Varrock tree patch.",
            position = 0,
            section = treePatchesSection
    )
    default boolean varrockPatch() {
        return true;  // Default to enabled
    }

    @ConfigItem(
            keyName = "faladorPatch",
            name = "Enable Falador Patch",
            description = "Enable or disable the Falador tree patch.",
            position = 1,
            section = treePatchesSection
    )
    default boolean faladorPatch() {
        return true;  // Default to enabled
    }

    @ConfigItem(
            keyName = "lumbridgePatch",
            name = "Enable Lumbridge Patch",
            description = "Enable or disable the Lumbridge tree patch.",
            position = 2,
            section = treePatchesSection
    )
    default boolean lumbridgePatch() {
        return true;  // Default to enabled
    }

    @ConfigItem(
            keyName = "taverleyPatch",
            name = "Enable Taverley Patch",
            description = "Enable or disable the Taverley tree patch.",
            position = 3,
            section = treePatchesSection
    )
    default boolean taverleyPatch() {
        return true;  // Default to enabled
    }

    @ConfigItem(
            keyName = "gnomeStrongholdNormalPatch",
            name = "Enable Gnome Stronghold Patch",
            description = "Enable or disable the Gnome tree patch.",
            position = 4,
            section = treePatchesSection
    )
    default boolean gnomeStrongholdNormalPatch() {
        return true;  // Default to enabled
    }

    @ConfigItem(
            keyName = "farmingGuildNormalPatch",
            name = "Enable Farming Guild patch",
            description = "Enable or disable the Farming Guild normal tree patch.",
            position = 5,
            section = treePatchesSection
    )
    default boolean farmingGuildNormalPatch() {
        return true;  // Enabled by default
    }

    // Fruit Tree Patches Section
    @ConfigSection(
            name = "Fruit Tree Patches",
            description = "Enable or disable fruit tree patches.",
            position = 5,
            closedByDefault = true
    )
    String fruitTreePatchesSection = "fruitTreePatchesSection";

    @ConfigItem(
            keyName = "catherbyPatch",
            name = "Enable Catherby Patch",
            description = "Enable or disable the Catherby fruit tree patch.",
            position = 0,
            section = fruitTreePatchesSection
    )
    default boolean catherbyPatch() {
        return true;  // Default to enabled
    }

    @ConfigItem(
            keyName = "gnomeStrongholdFruitPatch",
            name = "Enable Gnome Stronghold",
            description = "Enable or disable the Gnome Stronghold fruit tree patch.",
            position = 1,
            section = fruitTreePatchesSection
    )
    default boolean gnomeStrongholdFruitPatch() {
        return true;  // Default to enabled
    }

    @ConfigItem(
            keyName = "farmingGuildFruitPatch",
            name = "Enable Farming Guild Patch",
            description = "Enable or disable the Farming Guild fruit tree patch.",
            position = 3,
            section = fruitTreePatchesSection
    )
    default boolean farmingGuildFruitPatch() {
        return true;  // Enabled by default
    }

    @ConfigItem(
            keyName = "gnomeVillageFruit",
            name = "Enable Gnome Village",
            description = "Enable or disable the Gnome Village fruit tree patch.",
            position = 3,
            section = fruitTreePatchesSection
    )
    default boolean gnomeVillageFruit() {
        return true;  // Enabled by default
    }

    // Protection Settings Section
    @ConfigSection(
            name = "Protection Settings",
            description = "Settings for protecting the patches with compost or items.",
            position = 6,
            closedByDefault = true
    )
    String protectionSection = "protectionSection";

    @ConfigItem(
            keyName = "protectionMode",
            name = "Protection Mode",
            description = "Choose between protecting the patch with items or using compost.",
            position = 0,
            section = protectionSection
    )
    default ProtectionMode protectionMode() {
        return ProtectionMode.PROTECT_PATCH_WITH_ITEMS;  // Default to protecting with items
    }

    @ConfigItem(
            keyName = "compostType",
            name = "Compost Type",
            description = "Choose the type of compost to use.",
            position = 1,
            section = protectionSection
    )
    default CompostType compostType() {
        return CompostType.ULTRA_COMPOST;  // Default to Ultra Compost
    }


    public enum TeleportMethod {
        MAGIC,
        TELEPORT_TABLET
    }

}
