package net.runelite.client.plugins.microbot.zerozero.farmrun.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ItemID;

@Getter
@RequiredArgsConstructor
public enum NormalTreeMaterial {
    OAK_TREE("Oak", "oak sapling", ItemID.OAK_SAPLING, "tomatoes(5)", ItemID.TOMATOES5, 1, 15),
    WILLOW_TREE("Willow", "willow sapling", ItemID.WILLOW_SAPLING, "apples(5)", ItemID.APPLES5, 1, 30),
    MAPLE_TREE("Maple", "maple sapling", ItemID.MAPLE_SAPLING, "oranges(5)", ItemID.ORANGES5, 1, 45),
    YEW_TREE("Yew", "yew sapling", ItemID.YEW_SAPLING, "cactus spine", ItemID.CACTUS_SPINE, 10, 60),
    MAGIC_TREE("Magic", "magic sapling", ItemID.MAGIC_SAPLING, "coconut", ItemID.COCONUT, 25, 75);

    private final String name;
    private final String itemName;  // Sapling name
    private final int itemId;       // Sapling ID
    private final String protectionItem;
    private final int protectionItemId;  // Protection item ID
    private final int protectionItemAmount;
    private final int levelRequired;

    @Override
    public String toString() {
        return name;
    }
}
