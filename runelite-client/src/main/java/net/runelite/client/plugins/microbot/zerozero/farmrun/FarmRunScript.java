package net.runelite.client.plugins.microbot.zerozero.farmrun;

import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.zerozero.farmrun.enums.*;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.bank.enums.BankLocation;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class FarmRunScript extends Script {

    public static double version = 1.0;
    public static FarmRunState state = FarmRunState.RESET;
    private FarmRunState lastState = null; // To track state changes and log only once per state
    private static final int MAX_RETRY_COUNT = 3;  // Max number of retries for missing items
    private Map<Integer, Integer> missingItemRetries = new HashMap<>(); // Tracks retries for missing items

    // Define flags to prevent logging spams
    private boolean travelledToVarrock = false;
    private boolean travelledToFalador = false;
    private boolean travelledToLumbridge = false;
    private boolean travelledToTaverley = false;
    private boolean travelledToCatherby = false;
    private boolean travelledToGnome = false;
    private boolean travelledToGnomeStronghold = false;
    private boolean travelledToFarmingGuildNormal = false;
    private boolean travelledToFarmingGuildFruit = false;

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
                                state = FarmRunState.FARMING_GNOME_STRONGHOLD_FRUIT;  // Start with first tree patch
                            } else {
                                Microbot.log("Failed to withdraw required items. Shutting down the script.");
                                shutdown();
                            }
                        }
                        break;

                    case FARMING_GNOME_STRONGHOLD_FRUIT:
                        if (config.gnomeFruitPatch()) {  // Check if Gnome patch is enabled
                            if (!travelledToGnome) {  // Prevent logging multiple times
                                Microbot.log("Walking to Gnome patch...");
                                travelledToGnome = true;
                            }
                            if (Rs2Walker.walkTo(FarmPatch.FARMING_GNOME_STRONGHOLD_FRUIT.getPatchLocation())) {
                                if (performFarmActions(config, FarmPatch.FARMING_GNOME_STRONGHOLD_FRUIT, 7962)) {
                                    state = FarmRunState.FINISHED;
                                    travelledToGnome = false;  // Reset flag for next use
                                }
                            }
                        } else {
                            state = FarmRunState.FARMING_GNOME_STRONGHOLD;  // Skip Gnome and finish the farm run
                        }
                        break;

                    case FARMING_GNOME_STRONGHOLD:
                        if (config.gnomeNormalPatch()) {  // Check if Gnome patch is enabled
                            if (!travelledToGnomeStronghold) {  // Prevent logging multiple times
                                Microbot.log("Walking to Gnome patch...");
                                travelledToGnomeStronghold = true;
                            }
                            if (Rs2Walker.walkTo(FarmPatch.FARMING_GNOME_STRONGHOLD.getPatchLocation())) {
                                if (performFarmActions(config, FarmPatch.FARMING_GNOME_STRONGHOLD, 19147)) {
                                    state = FarmRunState.FINISHED;
                                    travelledToGnomeStronghold = false;  // Reset flag for next use
                                }
                            }
                        } else {
                            state = FarmRunState.FARMING_VARROCK;  // Skip Gnome and finish the farm run
                        }
                        break;

                    case FARMING_VARROCK:
                        if (config.varrockPatch()) {  // Check if Varrock patch is enabled
                            if (!travelledToVarrock) {  // Prevent logging multiple times
                                Microbot.log("Walking to Varrock patch...");
                                travelledToVarrock = true;
                            }
                            if (Rs2Walker.walkTo(FarmPatch.FARMING_VARROCK.getPatchLocation())) {
                                if (performFarmActions(config, FarmPatch.FARMING_VARROCK, 8390)) {
                                    state = FarmRunState.FARMING_FALADOR;
                                    travelledToVarrock = false;  // Reset flag for next use
                                }
                            }
                        } else {
                            state = FarmRunState.FARMING_FALADOR;  // Skip Varrock and go to Falador
                        }
                        break;

                    case FARMING_FALADOR:
                        if (config.faladorPatch()) {  // Check if Falador patch is enabled
                            if (!travelledToFalador) {  // Prevent logging multiple times
                                Microbot.log("Teleporting to Falador...");
                                if (Rs2Inventory.hasItem(ItemID.FALADOR_TELEPORT)) {
                                    Rs2Inventory.interact(ItemID.FALADOR_TELEPORT, "break");
                                    sleepUntil(() -> !Rs2Player.isAnimating() && !Rs2Player.isMoving(), 10000);
                                    Rs2Walker.setTarget(null);
                                    Rs2Player.waitForWalking();
                                    Microbot.log("Teleport successful, walking to Falador patch...");
                                    travelledToFalador = true;
                                }
                            }
                            if (Rs2Walker.walkTo(FarmPatch.FARMING_FALADOR.getPatchLocation())) {
                                if (performFarmActions(config, FarmPatch.FARMING_FALADOR, 8389)) {
                                    state = FarmRunState.FARMING_LUMBRIDGE;
                                    travelledToFalador = false;  // Reset flag for next use
                                }
                            }
                        } else {
                            state = FarmRunState.FARMING_TAVERLEY;  // Skip Falador and go to Lumbridge
                        }
                        break;

                    case FARMING_TAVERLEY:
                        if (config.taverleyPatch()) {  // Check if Taverley patch is enabled
                            if (!travelledToTaverley) {  // Prevent logging multiple times
                                Microbot.log("Walking to Taverley patch...");
                                travelledToTaverley = true;
                            }
                            if (Rs2Walker.walkTo(FarmPatch.FARMING_TAVERLEY.getPatchLocation())) {
                                if (performFarmActions(config, FarmPatch.FARMING_TAVERLEY, 8392)) {
                                    state = FarmRunState.FARMING_CATHERBY;
                                    travelledToTaverley = false;  // Reset flag for next use
                                }
                            }
                        } else {
                            state = FarmRunState.FARMING_LUMBRIDGE;  // Skip Taverley and go to Catherby
                        }
                        break;

                    case FARMING_LUMBRIDGE:
                        if (config.lumbridgePatch()) {  // Check if Lumbridge patch is enabled
                            if (!travelledToLumbridge) {  // Prevent logging multiple times
                                Microbot.log("Teleporting to Lumbridge...");
                                if (Rs2Inventory.hasItem(ItemID.LUMBRIDGE_TELEPORT)) {  // Check if the player has a Lumbridge teleport
                                    Rs2Inventory.interact(ItemID.LUMBRIDGE_TELEPORT, "break");
                                    sleepUntil(() -> !Rs2Player.isAnimating() && !Rs2Player.isMoving(), 10000);  // Wait for teleport
                                    Rs2Walker.setTarget(null);
                                    Rs2Player.waitForWalking();
                                    Microbot.log("Teleport successful, walking to Lumbridge patch...");
                                    travelledToLumbridge = true;
                                }
                            }
                            if (Rs2Walker.walkTo(FarmPatch.FARMING_LUMBRIDGE.getPatchLocation())) {
                                if (performFarmActions(config, FarmPatch.FARMING_LUMBRIDGE, 8391)) {
                                    state = FarmRunState.FARMING_TAVERLEY;
                                    travelledToLumbridge = false;  // Reset flag for next use
                                }
                            }
                        } else {
                            state = FarmRunState.FARMING_CATHERBY;  // Skip Lumbridge and go to Taverley
                        }
                        break;

                    case FARMING_CATHERBY:
                        if (config.catherbyPatch()) {  // Check if Catherby patch is enabled
                            if (!travelledToCatherby) {  // Prevent logging multiple times
                                Microbot.log("Walking to Catherby patch...");
                                travelledToCatherby = true;
                            }
                            if (Rs2Walker.walkTo(FarmPatch.FARMING_CATHERBY.getPatchLocation())) {
                                if (performFarmActions(config, FarmPatch.FARMING_CATHERBY, 19532)) {
                                    state = FarmRunState.FINISHED;
                                    travelledToCatherby = false;  // Reset flag for next use
                                }
                            }
                        } else {
                            state = FarmRunState.FARMING_GUILD_NORMAL;  // Skip Catherby and go to Gnome
                        }
                        break;

                    // Add Farming Guild Normal Tree Patch
                    case FARMING_GUILD_NORMAL:
                        if (config.farmingGuildNormalPatch()) {  // Check if Farming Guild normal patch is enabled
                            if (!travelledToFarmingGuildNormal) {  // Prevent logging multiple times
                                Microbot.log("Teleporting to Farming Guild Normal Tree patch...");
                                if (Rs2Inventory.hasItem(ItemID.SKILLS_NECKLACE)) {  // Check if player has a Skills Necklace for teleport
                                    Rs2Inventory.interact(ItemID.SKILLS_NECKLACE, "rub");
                                    sleepUntil(() -> !Rs2Player.isAnimating() && !Rs2Player.isMoving(), 10000);
                                    Rs2Walker.setTarget(null);
                                    Rs2Player.waitForWalking();
                                    Microbot.log("Teleport successful, walking to Farming Guild Normal Tree patch...");
                                    travelledToFarmingGuildNormal = true;
                                }
                            }
                            if (Rs2Walker.walkTo(FarmPatch.FARMING_GUILD_NORMAL.getPatchLocation())) {
                                if (performFarmActions(config, FarmPatch.FARMING_GUILD_NORMAL, 8389)) {  // Use the correct object ID for the patch
                                    state = FarmRunState.FARMING_GUILD_FRUIT;  // Move to next patch
                                    travelledToFarmingGuildNormal = false;  // Reset flag for next use
                                }
                            }
                        } else {
                            state = FarmRunState.FARMING_GUILD_FRUIT;  // Skip the normal tree and go to fruit tree
                        }
                        break;

                    case FARMING_GUILD_FRUIT:
                        if (config.farmingGuildFruitPatch()) {  // Check if Farming Guild fruit patch is enabled
                            if (!travelledToFarmingGuildFruit) {  // Prevent logging multiple times
                                Microbot.log("Teleporting to Farming Guild Fruit Tree patch...");
                                if (Rs2Inventory.hasItem(ItemID.SKILLS_NECKLACE)) {  // Check if player has a Skills Necklace for teleport
                                    Rs2Inventory.interact(ItemID.SKILLS_NECKLACE, "rub");
                                    sleepUntil(() -> !Rs2Player.isAnimating() && !Rs2Player.isMoving(), 10000);
                                    Rs2Walker.setTarget(null);
                                    Rs2Player.waitForWalking();
                                    Microbot.log("Teleport successful, walking to Farming Guild Fruit Tree patch...");
                                    travelledToFarmingGuildFruit = true;
                                }
                            }
                            if (Rs2Walker.walkTo(FarmPatch.FARMING_GUILD_FRUIT.getPatchLocation())) {
                                if (performFarmActions(config, FarmPatch.FARMING_GUILD_FRUIT, 19532)) {  // Use the correct object ID for the patch
                                    state = FarmRunState.FINISHED;  // Finished farm run
                                    travelledToFarmingGuildFruit = false;  // Reset flag for next use
                                }
                            }
                        } else {
                            state = FarmRunState.FINISHED;  // Skip the fruit tree and finish
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

    private boolean performFarmActions(FarmRunConfig config, FarmPatch patch, int objectId) {
        if (patch.isFruitTree()) {
            FruitTreeMaterial material = config.fruitTree(); // Use FruitTreeMaterial for fruit trees
            return performFruitTreeActions(config, patch, objectId, material);
        } else {
            NormalTreeMaterial material = config.normalTree(); // Use NormalTreeMaterial for normal trees
            return performNormalTreeActions(config, patch, objectId, material);
        }
    }

    // Actions specific to fruit trees
    private boolean performFruitTreeActions(FarmRunConfig config, FarmPatch patch, int objectId, FruitTreeMaterial material) {
        return performTreeActions(config, patch, objectId, material);
    }

    // Actions specific to normal trees
    private boolean performNormalTreeActions(FarmRunConfig config, FarmPatch patch, int objectId, NormalTreeMaterial material) {
        return performTreeActions(config, patch, objectId, material);
    }

    // Common tree action logic
    private <T> boolean performTreeActions(FarmRunConfig config, FarmPatch patch, int objectId, T material) {
        ObjectComposition tree = Rs2GameObject.findObjectComposition(objectId);

        // Step 1: Check health if applicable
        if (tree != null && Rs2GameObject.hasAction(tree, "check-health")) {
            if (checkTree(config, objectId)) {
                sleep(2000); // Ensure the check-health action is fully completed
                if (!removeTree(config, objectId, patch.getNpcId())) {
                    return false; // Tree removal failed
                }
            }
        }

        // Step 2: Check if chopping down the tree is required
        if (tree != null && Rs2GameObject.hasAction(tree, "chop down")) {
            if (!removeTree(config, objectId, patch.getNpcId())) {
                return false;
            }
        }

        // Step 3: Rake the patch if needed
        GameObject farmingPatch = Rs2GameObject.findObjectByImposter(objectId, "rake");
        if (farmingPatch != null) {
            if (!rakePatch(config, objectId)) {
                return false;
            }
        }

        // Step 4: Plant the tree sapling
        if (tree == null || Rs2GameObject.hasAction(tree, "Inspect")) {
            if (!plantTree(config, objectId, material)) {
                return false;
            }
        }

        // Step 5: Protect the tree with compost or items
        protectTree(config, objectId, patch.isFruitTree()); // Protect with items or apply compost
        return true;
    }





    // Check the health of the tree
    private boolean checkTree(FarmRunConfig config, int objectId) {
        if (!Rs2Player.isMoving() && !Rs2Player.isAnimating() && !Rs2Player.isInteracting() && !Rs2Player.isWalking()) {
            ObjectComposition tree = Rs2GameObject.findObjectComposition(objectId);
            if (tree != null && Rs2GameObject.hasAction(tree, "check-health")) {
                Microbot.log("Checking the health of the tree...");
                Rs2GameObject.interact(objectId, "check-health");
                sleepUntil(Rs2Player::isInteracting);
                sleepUntil(() -> !Rs2Player.isInteracting());
                return true;
            }
        }
        return false;
    }

    // Pay to remove the tree
    private boolean removeTree(FarmRunConfig config, int objectId, int npcId) {
        if (!Rs2Player.isMoving() && !Rs2Player.isAnimating() && !Rs2Player.isInteracting() && !Rs2Player.isWalking()) {
            ObjectComposition tree = Rs2GameObject.findObjectComposition(objectId);
            if (tree != null && Rs2GameObject.hasAction(tree, "chop down")) {
                Microbot.log("Paying to remove the tree...");

                // Interact with the NPC to pay for tree removal
                Rs2Npc.interact(npcId, "pay");
                sleepUntil(() -> Rs2Widget.hasWidget("pay 200 coins"));

                if (Rs2Widget.hasWidget("pay 200 coins")) {
                    Rs2Keyboard.typeString("1"); // Confirm payment
                    sleepUntil(Rs2Player::isInteracting); // Wait for the player to start interacting
                    sleepUntil(() -> !Rs2Player.isInteracting()); // Wait until the interaction is complete
                    return true;
                } else {
                    Microbot.log("Pay widget did not appear, could not pay to remove the tree.");
                }
            }
        }
        return false;
    }


    // Rake the patch
    private boolean rakePatch(FarmRunConfig config, int objectId) {
        if (!Rs2Player.isMoving() && !Rs2Player.isAnimating() && !Rs2Player.isInteracting() && !Rs2Player.isWalking()) {
            GameObject farmingPatch = Rs2GameObject.findObjectByImposter(objectId, "rake");
            if (farmingPatch != null) {
                Microbot.log("Raking the patch...");
                Rs2GameObject.interact(farmingPatch.getId(), "rake");
                sleepUntil(Rs2Player::isInteracting);
                sleepUntil(() -> !Rs2Player.isInteracting());
                return true;
            }
        }
        return false;
    }

    private boolean plantTree(FarmRunConfig config, int objectId, Object material) {
        if (!Rs2Player.isMoving() && !Rs2Player.isAnimating() && !Rs2Player.isInteracting() && !Rs2Player.isWalking()) {
            String itemName;
            int itemId;

            if (material instanceof NormalTreeMaterial) {
                itemName = ((NormalTreeMaterial) material).getItemName();
                itemId = ((NormalTreeMaterial) material).getItemId();
            } else if (material instanceof FruitTreeMaterial) {
                itemName = ((FruitTreeMaterial) material).getItemName();
                itemId = ((FruitTreeMaterial) material).getItemId();
            } else {
                return false; // Unknown material type
            }

            Microbot.log("Planting the tree: " + itemName);
            boolean success = Rs2Inventory.useItemOnObject(itemId, objectId);  // Use sapling on the patch
            if (success) {
                sleepUntil(Rs2Player::isInteracting);
                sleepUntil(() -> !Rs2Player.isInteracting());
                return true;
            }
        }
        return false;
    }




    private boolean useCompost(FarmRunConfig config, int objectId) {
        if (!Rs2Player.isMoving() && !Rs2Player.isAnimating() && !Rs2Player.isInteracting() && !Rs2Player.isWalking()) {
            CompostType CompostType = config.compostType();
            Microbot.log("Using compost: " + CompostType.name());

            // Select the compost and apply it to the patch
            Rs2Inventory.use(CompostType.getItemId());
            Rs2GameObject.interact(objectId, "use");
            sleepUntil(Rs2Player::isInteracting);
            sleepUntil(() -> !Rs2Player.isInteracting());

            return true;  // Compost applied successfully
        }
        return false;
    }


    private void protectTree(FarmRunConfig config, int objectId, boolean isFruitTree) {
        if (!Rs2Player.isMoving() && !Rs2Player.isAnimating() && !Rs2Player.isInteracting() && !Rs2Player.isWalking()) {
            if (config.protectionMode() == ProtectionMode.USE_COMPOST) {
                Microbot.log("Applying compost to the patch...");
                useCompost(config, objectId);
            } else if (config.protectionMode() == ProtectionMode.PROTECT_PATCH_WITH_ITEMS) {
                if (isFruitTree) {
                    FruitTreeMaterial material = config.fruitTree();
                    Microbot.log("Protecting the fruit tree with items: " + material.getProtectionItem());

                    if (Rs2Inventory.hasItemAmount(material.getProtectionItem(), material.getProtectionItemAmount())) {
                        Rs2Npc.interact(getNpcIdForPatch(state), "pay");
                        sleepUntil(() -> Rs2Inventory.hasItemAmount(material.getProtectionItem(), material.getProtectionItemAmount(), true));
                        sleepUntil(Rs2Player::isInteracting);
                        sleepUntil(() -> !Rs2Player.isInteracting());
                    } else {
                        Microbot.log("Missing protection items for " + material.getName());
                    }
                } else {
                    NormalTreeMaterial material = config.normalTree();
                    Microbot.log("Protecting the normal tree with items: " + material.getProtectionItem());

                    if (Rs2Inventory.hasItemAmount(material.getProtectionItem(), material.getProtectionItemAmount())) {
                        Rs2Npc.interact(getNpcIdForPatch(state), "pay");
                        sleepUntil(() -> Rs2Inventory.hasItemAmount(material.getProtectionItem(), material.getProtectionItemAmount(), true));
                        sleepUntil(Rs2Player::isInteracting);
                        sleepUntil(() -> !Rs2Player.isInteracting());
                    } else {
                        Microbot.log("Missing protection items for " + material.getName());
                    }
                }
            } else {
                Microbot.log("No protection selected.");
            }
        }
    }





    private boolean withdrawFarmingItems(FarmRunConfig config) {
        // Withdraw normal tree saplings
        NormalTreeMaterial normalTreeMaterial = config.normalTree();
        Microbot.log("Withdrawing normal tree items: " + normalTreeMaterial.getItemName());

        // Withdraw spade
        Microbot.log("Attempting to withdraw Spade...");
        if (!withdrawItemWithRetry(ItemID.SPADE)) return false;

        // Withdraw rake
        Microbot.log("Attempting to withdraw Rake...");
        if (!withdrawItemWithRetry(ItemID.RAKE)) return false;

        // Withdraw stamina potion
        Microbot.log("Attempting to withdraw Stamina Potion (4)...");
        if (!withdrawItemWithRetry(ItemID.STAMINA_POTION4, 1)) return false;

        // Withdraw teleport items for normal tree patches
        Microbot.log("Attempting to withdraw Varrock Teleport...");
        if (!withdrawItemWithRetry(getTeleportItemForPatch(FarmRunState.FARMING_VARROCK), 2)) return false;

        Microbot.log("Attempting to withdraw Falador Teleport...");
        if (!withdrawItemWithRetry(getTeleportItemForPatch(FarmRunState.FARMING_FALADOR), 1)) return false;

        Microbot.log("Attempting to withdraw Lumbridge Teleport...");
        if (!withdrawItemWithRetry(getTeleportItemForPatch(FarmRunState.FARMING_LUMBRIDGE), 1)) return false;

        // Withdraw normal tree saplings
        Microbot.log("Attempting to withdraw Normal Tree Saplings: " + normalTreeMaterial.getItemName() + "...");
        if (!withdrawItemWithRetry(normalTreeMaterial.getItemId(), 4)) return false;

        // Withdraw fruit tree saplings
        FruitTreeMaterial fruitTreeMaterial = config.fruitTree();
        Microbot.log("Withdrawing fruit tree items: " + fruitTreeMaterial.getItemName());

        // Withdraw fruit tree teleports
        Microbot.log("Attempting to withdraw Catherby Teleport...");
        if (!withdrawItemWithRetry(getTeleportItemForPatch(FarmRunState.FARMING_CATHERBY), 1)) return false;

        Microbot.log("Attempting to withdraw Gnome Teleport...");
        if (!withdrawItemWithRetry(getTeleportItemForPatch(FarmRunState.FARMING_GNOME_STRONGHOLD_FRUIT), 1)) return false;

        // Withdraw fruit tree saplings
        Microbot.log("Attempting to withdraw Fruit Tree Saplings: " + fruitTreeMaterial.getItemName() + "...");
        if (!withdrawItemWithRetry(fruitTreeMaterial.getItemId(), 4)) return false;

        // Withdraw protection items or compost based on the selected protection mode
        if (config.protectionMode() == ProtectionMode.PROTECT_PATCH_WITH_ITEMS) {
            Microbot.log("Attempting to withdraw Protection Items: " + normalTreeMaterial.getProtectionItem() + "...");
            if (!withdrawItemWithRetry(normalTreeMaterial.getProtectionItemId(), normalTreeMaterial.getProtectionItemAmount())) return false;
        } else if (config.protectionMode() == ProtectionMode.USE_COMPOST) {
            CompostType compostType = config.compostType();
            Microbot.log("Attempting to withdraw " + compostType.name() + "...");
            if (!withdrawItemWithRetry(compostType.getItemId(), 1)) return false;  // Withdraw selected compost
        } else {
            Microbot.log("Skipping protection item as none selected.");
        }

        // Withdraw coins for payment
        Microbot.log("Attempting to withdraw Coins (10000)...");
        if (!withdrawItemWithRetry(ItemID.COINS_995, 10000)) return false;  // Ensure enough coins for payments

        Microbot.log("Successfully withdrew all required items.");
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

    // Helper method to withdraw one item
    private boolean withdrawItemWithRetry(int itemId) {
        return withdrawItemWithRetry(itemId, 1);  // Default to withdraw one if no amount is specified
    }

    // Get the teleport item ID for a specific patch
    private int getTeleportItemForPatch(FarmRunState state) {
        return FarmPatch.valueOf(state.name()).getTeleportItemId();
    }

    // Get the patch location for a specific patch
    private WorldPoint getPatchLocationForPatch(FarmRunState state) {
        return FarmPatch.valueOf(state.name()).getPatchLocation();
    }

    // Get the NPC ID for protecting the patch
    private int getNpcIdForPatch(FarmRunState state) {
        return FarmPatch.valueOf(state.name()).getNpcId();
    }


    @Override
    public void shutdown() {
        if (mainScheduledFuture != null) {
            mainScheduledFuture.cancel(true);  // Stop the scheduled tasks
        }
        super.shutdown();
        state = FarmRunState.RESET;  // Reset state when stopped
    }

    // Helper methods for withdrawing items, getting NPC ID, teleport items, etc., remain the same

}
