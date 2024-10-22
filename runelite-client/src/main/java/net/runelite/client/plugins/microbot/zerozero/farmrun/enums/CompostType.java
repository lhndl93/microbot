package net.runelite.client.plugins.microbot.zerozero.farmrun.enums;

import lombok.Getter;
import net.runelite.api.ItemID;

@Getter
public enum CompostType {
    COMPOST(ItemID.COMPOST),
    SUPER_COMPOST(ItemID.SUPERCOMPOST),
    ULTRA_COMPOST(ItemID.ULTRACOMPOST),
    BOTTOMLESS_COMPOST_BUCKET(ItemID.BOTTOMLESS_COMPOST_BUCKET_22997);

    private final int itemId;

    CompostType(int itemId) {
        this.itemId = itemId;
    }

}
