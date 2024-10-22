package net.runelite.client.plugins.microbot.zerozero.farmrun;

import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.zerozero.farmrun.enums.FarmingMaterial;
import net.runelite.client.plugins.microbot.zerozero.farmrun.enums.FarmRunState;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.bank.enums.BankLocation;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.microbot.zerozero.farmrun.enums.ProtectionMode;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class FarmRunScript extends Script {

    public static double version = 1.0;
    public static FarmRunState state = FarmRunState.RESET;
    private FarmRunState lastState = null; // To track state changes and log only once per state
    private static final int MAX_RETRY_COUNT = 3;  // Max number of retries for missing items
    private Map<Integer, Integer> missingItemRetries = new HashMap<>(); // Tracks retries for missing items


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

    // Helper method to log state changes (only once per state)
    private void logStateChange(FarmRunState newState) {
        if (newState != lastState) {
            Microbot.log("State changed to: " + newState);
            lastState = newState;  // Update the last logged state
        }
    }

    public boolean run(FarmRunConfig config) {
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;

                logStateChange(state);  // Log state transitions

                switch (state) {
                    case RESET:
                        // Ensure the script starts in the Grand Exchange
                        if (!isInGrandExchange()) {
                            Microbot.log("Not in the Grand Exchange. Please start the script in the Grand Exchange.");
                            shutdown();
                        } else {
                            state = FarmRunState.BANKING;
                        }
                        break;

                    case BANKING:
                        // Open the bank and withdraw necessary items
                        Microbot.log("Opening bank to withdraw farming items...");
                        if (Rs2Bank.openBank()) {
                            Rs2Bank.depositAll();
                            boolean success = withdrawFarmingItems(config);
                            Rs2Bank.closeBank();
                            if (success) {
                                state = FarmRunState.FARMING_VARROCK;  // Start with first tree patch
                            } else {
                                Microbot.log("Failed to withdraw required items. Shutting down the script.");
                                shutdown();
                            }
                        }
                        break;

                    case FARMING_GNOME_STRONGHOLD:
                        Microbot.log("Walking to Gnome Stronghold patch...");
                        if (Rs2Walker.walkTo(getPatchLocationForState(FarmRunState.FARMING_GNOME_STRONGHOLD))) {
                            plantTree(config, 19147, NpcID.PRISSY_SCILLA, FarmRunState.FARMING_VARROCK);
                        }
                        break;

                    case FARMING_VARROCK:
                        Microbot.log("Walking to Varrock patch...");
                        if (Rs2Walker.walkTo(getPatchLocationForState(FarmRunState.FARMING_VARROCK))) {
                            plantTree(config, 8390, NpcID.TREZNOR_11957, FarmRunState.FARMING_FALADOR);
                        }
                        break;

                    case FARMING_FALADOR:
                        Microbot.log("Walking to Falador patch...");
                        if (Rs2Walker.walkTo(getPatchLocationForState(FarmRunState.FARMING_FALADOR))) {
                            plantTree(config, 8389, NpcID.HESKEL, FarmRunState.FARMING_LUMBRIDGE);
                        }
                        break;

                    case FARMING_LUMBRIDGE:
                        Microbot.log("Walking to Lumbridge patch...");
                        if (Rs2Walker.walkTo(getPatchLocationForState(FarmRunState.FARMING_LUMBRIDGE))) {
                            plantTree(config, 8391, NpcID.FAYETH, FarmRunState.FARMING_TAVERLEY);
                        }
                        break;

                    case FARMING_TAVERLEY:
                        Microbot.log("Walking to Taverley patch...");
                        if (Rs2Walker.walkTo(getPatchLocationForState(FarmRunState.FARMING_TAVERLEY))) {
                            plantTree(config, 8392, NpcID.ALICE, FarmRunState.FARMING_CATHERBY);
                        }
                        break;

                    case FARMING_CATHERBY:
                        Microbot.log("Walking to Catherby patch...");
                        if (Rs2Walker.walkTo(getPatchLocationForState(FarmRunState.FARMING_CATHERBY))) {
                            plantTree(config, 19532, NpcID.ELLEN, FarmRunState.FARMING_GNOME);
                        }
                        break;

                    case FARMING_GNOME:
                        Microbot.log("Walking to Gnome patch...");
                        if (Rs2Walker.walkTo(getPatchLocationForState(FarmRunState.FARMING_GNOME))) {
                            plantTree(config, 19533, NpcID.BOLONGO, FarmRunState.FINISHED);
                        }
                        break;

                    case FINISHED:
                        Microbot.log("Farm run finished. Returning to the Grand Exchange.");
                        if (Rs2Walker.walkTo(BankLocation.GRAND_EXCHANGE.getWorldPoint())) {
                            shutdown();
                        }
                        break;

                    default:
                        break;
                }

            } catch (Exception ex) {
                Microbot.log("Error: " + ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    private boolean withdrawFarmingItems(FarmRunConfig config) {
        FarmingMaterial material = config.farmingMaterial();
        Microbot.log("Withdrawing farming items: " + material.getItemName());

        if (!withdrawItemWithRetry(ItemID.SPADE)) return false;
        if (!withdrawItemWithRetry(ItemID.RAKE)) return false;
        if (!withdrawItemWithRetry(ItemID.STAMINA_POTION4, 1)) return false;
        if (!withdrawItemWithRetry(getTeleportItemForState(FarmRunState.FARMING_VARROCK), 2)) return false;
        if (!withdrawItemWithRetry(getTeleportItemForState(FarmRunState.FARMING_FALADOR), 1)) return false;
        if (!withdrawItemWithRetry(getTeleportItemForState(FarmRunState.FARMING_LUMBRIDGE), 1)) return false;
        if (!withdrawItemWithRetry(material.getItemId(), 4)) return false;  // Withdraw saplings by item ID

        // Only withdraw protection items if "Protect Patch" is selected
        if (config.protectionMode() == ProtectionMode.PROTECT_PATCH) {
            Microbot.log("Withdrawing protection items.");
            if (!withdrawItemWithRetry(material.getProtectionItemId(), material.getProtectionItemAmount())) return false;  // Withdraw protection items by item ID
        } else {
            Microbot.log("Skipping protection item withdrawal (Pay to Protect selected).");
        }

        if (!withdrawItemWithRetry(ItemID.COINS_995, 10000)) return false;  // Ensure enough coins for payments

        return true;  // All items were successfully withdrawn
    }


    // Helper method to withdraw an item with retry logic
    private boolean withdrawItemWithRetry(int itemId, int amount) {
        int retries = missingItemRetries.getOrDefault(itemId, 0);

        if (!Rs2Bank.hasItem(itemId)) {
            retries++;
            Microbot.log("Item ID " + itemId + " is missing from the bank. Retry attempt: " + retries);
            missingItemRetries.put(itemId, retries);

            if (retries >= MAX_RETRY_COUNT) {
                Microbot.log("Failed to find Item ID " + itemId + " after " + MAX_RETRY_COUNT + " attempts. Stopping the script.");
                return false;  // Exceeded max retries
            }
            return false;  // Retry withdrawing this item on the next cycle
        }

        Rs2Bank.withdrawX(itemId, amount);
        missingItemRetries.remove(itemId);  // Reset retries if item is found
        return true;  // Successfully withdrew the item
    }

    private boolean withdrawItemWithRetry(int itemId) {
        return withdrawItemWithRetry(itemId, 1);  // Default to withdraw one if no amount is specified
    }

    private int getTeleportItemForState(FarmRunState state) {
        switch (state) {
            case FARMING_VARROCK:
                return ItemID.VARROCK_TELEPORT;
            case FARMING_FALADOR:
                return ItemID.FALADOR_TELEPORT;
            case FARMING_LUMBRIDGE:
                return ItemID.LUMBRIDGE_TELEPORT;
            case FARMING_TAVERLEY:
                return ItemID.TAVERLEY_TELEPORT;  // Define this correctly
            case FARMING_CATHERBY:
                return ItemID.CATHERBY_TELEPORT;  // Define this correctly
            case FARMING_GNOME:
                return ItemID.CATHERBY_TELEPORT;  // NEED TO UPDATE
            default:
                return -1;  // Return an invalid item ID if there's no teleport
        }
    }

    private WorldPoint getPatchLocationForState(FarmRunState state) {
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

    private boolean plantTree(FarmRunConfig config, int objectId, int npcId, FarmRunState nextState) {
        try {
            FarmingMaterial material = config.farmingMaterial();  // Get the selected material
            final ObjectComposition tree = Rs2GameObject.findObjectComposition(objectId);

            if (tree != null && tree.getImpostor().getName().equalsIgnoreCase(material.getName())) {

                if (Rs2GameObject.hasAction(tree, "check-health")) {
                    Rs2GameObject.interact(objectId, "check-health");
                    int currentFarmingExp = Microbot.getClient().getSkillExperience(Skill.FARMING);
                    sleepUntilOnClientThread(() -> currentFarmingExp != Microbot.getClient().getSkillExperience(Skill.FARMING));
                } else if (Rs2GameObject.hasAction(tree, "chop down")) {
                    Rs2Npc.interact(npcId, "pay");
                    sleepUntil(() -> Rs2Widget.hasWidget("pay 200 coins"));
                    Rs2Keyboard.typeString("1");
                } else {
                    if (tree != null && !Rs2GameObject.hasAction(tree, "chop down")
                            && !Rs2GameObject.hasAction(tree, "check-health")
                            && tree.getImpostor().getName().equalsIgnoreCase(material.getName())) {
                        Rs2Npc.interact(npcId, "pay");
                        sleepUntil(() -> Rs2Inventory.hasItemAmount(material.getProtectionItem(), material.getProtectionItemAmount(), true));
                    }
                }
            } else {
                if (!rakeAndPlantTree(objectId, material.getItemName(), tree)) return true;
            }

            if (Rs2Inventory.hasItemAmount(material.getItemName(), 1, false, true) &&
                    Rs2Inventory.hasItemAmount(material.getProtectionItem(), material.getProtectionItemAmount(), false, true)) {
                state = nextState;
            }
            return false;
        } catch (Exception ex) {
            Microbot.log("Error in plantTree: " + ex.getMessage());
        }
        return false;
    }

    private boolean rakeAndPlantTree(int patchId, String treeToPlant, ObjectComposition tree) {
        if (Rs2Player.isAnimating()) return false;

        GameObject farmingPatch = Rs2GameObject.findObjectByImposter(patchId, "rake");
        if (farmingPatch != null) {
            Rs2GameObject.interact(farmingPatch, "rake");
            sleep(2000);
            sleepUntil(() -> !Rs2Player.isAnimating());
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
        if (mainScheduledFuture != null) {
            mainScheduledFuture.cancel(true);  // Stop the scheduled tasks
        }
        super.shutdown();
        state = FarmRunState.RESET;  // Reset state when stopped
    }
}
