package net.runelite.client.plugins.microbot.zerozero.farmrun.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ItemID;

@Getter
@RequiredArgsConstructor
public enum FarmingMaterial {
    OAK_TREE("Oak", "oak sapling", ItemID.OAK_SAPLING, "tomatoes(5)", ItemID.TOMATOES5, 1, 15),
    WILLOW_TREE("Willow", "willow sapling", ItemID.WILLOW_SAPLING, "apples(5)", ItemID.APPLES5, 1, 30),
    MAPLE_TREE("Maple", "maple sapling", ItemID.MAPLE_SAPLING, "oranges(5)", ItemID.ORANGES5, 1, 45),
    YEW_TREE("Yew", "yew sapling", ItemID.YEW_SAPLING, "cactus spine", ItemID.CACTUS_SPINE, 10, 60),
    MAGIC_TREE("Magic", "magic sapling", ItemID.MAGIC_SAPLING, "coconut", ItemID.COCONUT, 25, 75),
    APPLE_TREE("Apple", "apple sapling", ItemID.APPLE_SAPLING, "sweetcorn", ItemID.SWEETCORN, 9, 27),
    BANANA_TREE("Banana", "banana sapling", ItemID.BANANA_SAPLING, "apples(5)", ItemID.APPLES5, 4, 33),
    ORANGE_TREE("Orange", "orange sapling", ItemID.ORANGE_SAPLING, "strawberries(5)", ItemID.STRAWBERRIES5, 3, 39),
    CURRY_TREE("Curry", "curry sapling", ItemID.CURRY_SAPLING, "bananas(5)", ItemID.BANANAS5, 5, 42),
    PINEAPPLE_TREE("Pineapple", "pineapple sapling", ItemID.PINEAPPLE_SAPLING, "watermelon", ItemID.WATERMELON, 10, 51),
    PAPAYA_TREE("Papaya", "papaya sapling", ItemID.PAPAYA_SAPLING, "pineapple", ItemID.PINEAPPLE, 10, 57),
    PALM_TREE("Palm", "palm sapling", ItemID.PALM_SAPLING, "papaya fruit", ItemID.PAPAYA_FRUIT, 15, 68),
    DRAGONFRUIT_TREE("Dragonfruit", "dragonfruit sapling", ItemID.DRAGONFRUIT_SAPLING, "coconut", ItemID.COCONUT, 15, 81);

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

