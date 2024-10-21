package net.runelite.client.plugins.microbot.farming;

import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.farming.enums.FarmingState;
import net.runelite.client.plugins.microbot.util.bank.enums.BankLocation;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.util.concurrent.TimeUnit;

public class FarmingScript extends Script {

    public static double version = 1.0;
    public static FarmingState state = FarmingState.RESET;

    // Tree patch locations
    WorldPoint TREE_RUN_GNOME_STRONGHOLD = new WorldPoint(2437, 3418, 0);
    WorldPoint TREE_RUN_VARROCK = new WorldPoint(3226, 3458, 0);
    WorldPoint TREE_RUN_LUMBRIDGE = new WorldPoint(3193, 3228, 0);
    WorldPoint TREE_RUN_FALADOR = new WorldPoint(3003, 3376, 0);
    WorldPoint TREE_RUN_TAVERLEY = new WorldPoint(2933, 3436, 0);

    // Fruit tree locations
    WorldPoint FRUIT_TREE_RUN_CATHERBY = new WorldPoint(2809, 3452, 0);
    WorldPoint FRUIT_TREE_RUN_GNOME = new WorldPoint(2436, 3416, 0);

    // Define the Grand Exchange area
    WorldArea grandExchange = new WorldArea(3142, 3470, 50, 50, 0); // Grand Exchange coordinates

    int sleepBetweenTeleports = 4000;

    // Check if the player is in the Grand Exchange
    private boolean isInGrandExchange() {
        Player localPlayer = Microbot.getClient().getLocalPlayer();
        if (localPlayer == null) {
            return false;
        }
        return grandExchange.contains(localPlayer.getWorldLocation());
    }

    public boolean run(FarmingConfig config) {
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;

                switch (state) {
                    case RESET:
                        // Ensure the script starts in the Grand Exchange
                        if (!isInGrandExchange()) {
                            Microbot.getNotifier().notify("Start the script in the Grand Exchange.");
                            shutdown();
                        } else {
                            state = FarmingState.BANKING;
                        }
                        break;

                    case BANKING:
                        // Open the bank and withdraw necessary items
                        if (Rs2Bank.openBank()) {
                            Rs2Bank.depositAll();
                            withdrawFarmingItems(config);
                            Rs2Bank.closeBank();
                            state = FarmingState.FARMING_GNOME_STRONGHOLD;  // Start with first tree patch
                        }
                        break;

                    case FARMING_GNOME_STRONGHOLD:
                        if (isInGrandExchange()) {
                            if (Rs2Walker.walkTo(getPatchLocationForState(FarmingState.FARMING_GNOME_STRONGHOLD))) {
                                plantTree(config, 19147, NpcID.PRISSY_SCILLA, FarmingState.FARMING_VARROCK);
                            }
                        }
                        break;

                    case FARMING_VARROCK:
                        if (Rs2Walker.walkTo(getPatchLocationForState(FarmingState.FARMING_VARROCK))) {
                            plantTree(config, 8390, NpcID.TREZNOR_11957, FarmingState.FARMING_FALADOR);
                        }
                        break;

                    case FARMING_FALADOR:
                        if (Rs2Walker.walkTo(getPatchLocationForState(FarmingState.FARMING_FALADOR))) {
                            plantTree(config, 8389, NpcID.HESKEL, FarmingState.FARMING_LUMBRIDGE);
                        }
                        break;

                    case FARMING_LUMBRIDGE:
                        if (Rs2Walker.walkTo(getPatchLocationForState(FarmingState.FARMING_LUMBRIDGE))) {
                            plantTree(config, 8391, NpcID.FAYETH, FarmingState.FARMING_TAVERLEY);
                        }
                        break;

                    case FARMING_TAVERLEY:
                        if (Rs2Walker.walkTo(getPatchLocationForState(FarmingState.FARMING_TAVERLEY))) {
                            plantTree(config, 8392, NpcID.ALICE, FarmingState.FARMING_CATHERBY);
                        }
                        break;

                    case FARMING_CATHERBY:
                        if (Rs2Walker.walkTo(getPatchLocationForState(FarmingState.FARMING_CATHERBY))) {
                            plantTree(config, 19532, NpcID.ELLEN, FarmingState.FARMING_GNOME);
                        }
                        break;

                    case FARMING_GNOME:
                        if (Rs2Walker.walkTo(getPatchLocationForState(FarmingState.FARMING_GNOME))) {
                            plantTree(config, 19533, NpcID.BOLONGO, FarmingState.FINISHED);
                        }
                        break;

                    case FINISHED:
                        if (Rs2Walker.walkTo(BankLocation.GRAND_EXCHANGE.getWorldPoint())) {
                            shutdown();
                        }
                        break;

                    default:
                        break;
                }

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    private void withdrawFarmingItems(FarmingConfig config) {
        FarmingMaterial material = config.farmingMaterial();

        Rs2Bank.withdrawOne(ItemID.SPADE);
        Rs2Bank.withdrawOne(ItemID.RAKE);
        Rs2Bank.withdrawX(ItemID.STAMINA_POTION4, 1);
        Rs2Bank.withdrawX(getTeleportItemForState(FarmingState.FARMING_VARROCK), 2);
        Rs2Bank.withdrawX(getTeleportItemForState(FarmingState.FARMING_FALADOR), 1);
        Rs2Bank.withdrawX(getTeleportItemForState(FarmingState.FARMING_LUMBRIDGE), 1);
        Rs2Bank.withdrawX(material.getItemName(), 4);  // Withdraw saplings
        Rs2Bank.withdrawX(material.getProtectionItem(), material.getProtectionItemAmount());  // Withdraw protection items
        Rs2Bank.withdrawX(ItemID.COINS_995, 10000);  // Ensure enough coins for payments
    }

    // Helper method to get the correct teleport item based on the current state
    private int getTeleportItemForState(FarmingState state) {
        switch (state) {
            case FARMING_VARROCK:
                return ItemID.VARROCK_TELEPORT;
            case FARMING_FALADOR:
                return ItemID.FALADOR_TELEPORT;
            case FARMING_LUMBRIDGE:
                return ItemID.LUMBRIDGE_TELEPORT;
            case FARMING_TAVERLEY:
                return ItemID.TAVERLEY_TELEPORT; // You may need to define this item ID
            case FARMING_CATHERBY:
                return ItemID.CATHERBY_TELEPORT; // You may need to define this item ID
            case FARMING_GNOME:
                return ItemID.GNOME_STRONGHOLD_TELEPORT; // You may need to define this item ID
            default:
                return -1;  // Return an invalid item ID if there's no teleport
        }
    }

    // Helper method to get the correct patch location based on the current state
    private WorldPoint getPatchLocationForState(FarmingState state) {
        switch (state) {
            case FARMING_VARROCK:
                return TREE_RUN_VARROCK;
            case FARMING_FALADOR:
                return TREE_RUN_FALADOR;
            case FARMING_LUMBRIDGE:
                return TREE_RUN_LUMBRIDGE;
            case FARMING_TAVERLEY:
                return TREE_RUN_TAVERLEY;
            case FARMING_CATHERBY:
                return FRUIT_TREE_RUN_CATHERBY;
            case FARMING_GNOME:
                return FRUIT_TREE_RUN_GNOME;
            default:
                return null;  // Return null if there's no valid patch location
        }
    }

    private boolean plantTree(FarmingConfig config, int objectId, int npcId, FarmingState nextState) {
        try {
            FarmingMaterial material = config.farmingMaterial(); // Get the selected material

            // Get the tree object composition
            final ObjectComposition tree = Rs2GameObject.findObjectComposition(objectId);

            if (tree != null && tree.getImpostor().getName().equalsIgnoreCase(material.getName())) {

                // Check if tree health can be checked
                if (Rs2GameObject.hasAction(tree, "check-health")) {
                    Rs2GameObject.interact(objectId, "check-health");
                    int currentFarmingExp = Microbot.getClient().getSkillExperience(Skill.FARMING);
                    sleepUntilOnClientThread(() -> currentFarmingExp != Microbot.getClient().getSkillExperience(Skill.FARMING));

                    // If the tree can be chopped down, interact with NPC to pay for chopping
                } else if (Rs2GameObject.hasAction(tree, "chop down")) {
                    Rs2Npc.interact(npcId, "pay");
                    sleepUntil(() -> Rs2Widget.hasWidget("pay 200 coins"));
                    Rs2Keyboard.typeString("1");

                    // Otherwise, pay for protection if the tree has no other actions
                } else {
                    if (tree != null
                            && !Rs2GameObject.hasAction(tree, "chop down")
                            && !Rs2GameObject.hasAction(tree, "check-health")
                            && tree.getImpostor().getName().equalsIgnoreCase(material.getName())) {
                        Rs2Npc.interact(npcId, "pay");
                        sleepUntil(() -> Rs2Inventory.hasItemAmount(material.getProtectionItem(), material.getProtectionItemAmount(), true));
                    }
                }
            } else {
                // Rake and plant the tree if no tree exists
                if (!rakeAndPlantTree(objectId, material.getItemName(), tree)) return true;
            }

            // If the inventory contains the required items for the material, proceed to the next state
            if (Rs2Inventory.hasItemAmount(material.getItemName(), 1, false, true) &&
                    Rs2Inventory.hasItemAmount(material.getProtectionItem(), material.getProtectionItemAmount(), false, true)) {
                state = nextState;
            }
            return false;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return false;
    }

    private boolean rakeAndPlantTree(int patchId, String treeToPlant, ObjectComposition tree) {
        if (Microbot.isAnimating()) return false;

        GameObject farmingPatch = Rs2GameObject.findObjectByImposter(patchId, "rake");
        if (farmingPatch != null) {
            Rs2GameObject.interact(farmingPatch, "rake");
            sleep(2000);
            sleepUntil(() -> !Microbot.isAnimating());
        } else {
            Rs2Inventory.use(treeToPlant);
            boolean success = Rs2GameObject.interact(patchId);
            if (success) {
                sleepUntil(() -> tree != null);
            }
        }
        Rs2Inventory.dropAll("weeds");
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
        state = FarmingState.RESET;
    }
}
