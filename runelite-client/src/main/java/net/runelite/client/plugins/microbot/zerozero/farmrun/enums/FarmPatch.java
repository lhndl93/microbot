package net.runelite.client.plugins.microbot.zerozero.farmrun.enums;

import lombok.Getter;
import net.runelite.api.ItemID;
import net.runelite.api.NpcID;
import net.runelite.api.coords.WorldPoint;

public enum FarmPatch {
    FARMING_VARROCK(ItemID.VARROCK_TELEPORT, new WorldPoint(3226, 3458, 0), NpcID.TREZNOR_11957, false),  // Normal tree
    FARMING_FALADOR(ItemID.FALADOR_TELEPORT, new WorldPoint(3003, 3376, 0), NpcID.HESKEL, false),           // Normal tree
    FARMING_LUMBRIDGE(ItemID.LUMBRIDGE_TELEPORT, new WorldPoint(3193, 3228, 0), NpcID.FAYETH, false),      // Normal tree
    FARMING_TAVERLEY(ItemID.FALADOR_TELEPORT, new WorldPoint(2933, 3436, 0), NpcID.ALAIN, false),         // Normal tree
    FARMING_CATHERBY(ItemID.CAMELOT_TELEPORT, new WorldPoint(2854, 3432, 0), NpcID.ELLENA, true),
    FARMING_GNOME_VILLAGE(ItemID.VARROCK_TELEPORT, new WorldPoint(2492, 3182, 0), NpcID.GILETH, true),           // Fruit tree
    FARMING_GNOME_STRONGHOLD(ItemID.VARROCK_TELEPORT, new WorldPoint(2439, 3419, 0), NpcID.PRISSY_SCILLA, true),           // Fruit tree
    FARMING_GUILD_NORMAL(ItemID.VARROCK_TELEPORT, new WorldPoint(1233, 3732, 0), NpcID.ROSIE, false),      // Normal tree in Farming Guild
    FARMING_GUILD_FRUIT(ItemID.VARROCK_TELEPORT, new WorldPoint(1243, 3755, 0), NpcID.NIKKIE, true),         // Fruit tree in Farming Guild
    FARMING_GNOME_STRONGHOLD_FRUIT(ItemID.VARROCK_TELEPORT, new WorldPoint(2473, 3447, 0), NpcID.BOLONGO, true);  // Fruit tree in Gnome Stronghold



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
