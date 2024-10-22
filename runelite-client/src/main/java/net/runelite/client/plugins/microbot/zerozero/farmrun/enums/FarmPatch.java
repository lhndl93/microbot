package net.runelite.client.plugins.microbot.zerozero.farmrun.enums;

import lombok.Getter;
import net.runelite.api.ItemID;
import net.runelite.api.NpcID;
import net.runelite.api.coords.WorldPoint;

public enum FarmPatch {
    FARMING_VARROCK(ItemID.VARROCK_TELEPORT, new WorldPoint(3226, 3458, 0), NpcID.TREZNOR_11957, false),  // Normal tree
    FARMING_FALADOR(ItemID.FALADOR_TELEPORT, new WorldPoint(3003, 3376, 0), NpcID.HESKEL, false),           // Normal tree
    FARMING_LUMBRIDGE(ItemID.LUMBRIDGE_TELEPORT, new WorldPoint(3193, 3228, 0), NpcID.FAYETH, false),      // Normal tree
    FARMING_TAVERLEY(ItemID.FALADOR_TELEPORT, new WorldPoint(2933, 3436, 0), NpcID.ALICE, false),         // Normal tree
    FARMING_CATHERBY(ItemID.CAMELOT_TELEPORT, new WorldPoint(2809, 3452, 0), NpcID.ELLEN, true),          // Fruit tree
    FARMING_GNOME(ItemID.CAMELOT_TELEPORT, new WorldPoint(2436, 3416, 0), NpcID.BOLONGO, true);           // Fruit tree

    @Getter
    private final int teleportItemId;
    @Getter
    private final WorldPoint patchLocation;
    @Getter
    private final int npcId;
    @Getter
    private final boolean isFruitTree;

    FarmPatch(int teleportItemId, WorldPoint patchLocation, int npcId, boolean isFruitTree) {
        this.teleportItemId = teleportItemId;
        this.patchLocation = patchLocation;
        this.npcId = npcId;
        this.isFruitTree = isFruitTree;
    }

}

