package net.runelite.client.plugins.microbot.zerozero.farmrun;

import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.microbot.zerozero.farmrun.enums.*;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.bank.enums.BankLocation;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class FarmRunScript extends Script {

    public static double version = 1.0;
    public static FarmRunState state = FarmRunState.RESET;
    private FarmRunState lastState = null; // To track state changes and log only once per state
    private static final int MAX_RETRY_COUNT = 5;  // Max number of retries for missing items
    private Map<Integer, Integer> missingItemRetries = new HashMap<>(); // Tracks retries for missing items
    private int currentPatchIndex = 0;  // Track the current patch
    private List<FarmPatch> selectedPatches = new ArrayList<>();  // Store the selected patches
    private boolean performingActions = false; // New flag to ensure we only perform actions after arriving at the patch

    // Travel flags
    private boolean travelledToVarrock = false;
    private boolean travelledToFalador = false;
    private boolean travelledToLumbridge = false;
    private boolean travelledToTaverley = false;
    private boolean travelledToCatherby = false;
    private boolean travelledToGnomeStronghold = false;
    private boolean travelledToFarmingGuildNormal = false;
    private boolean travelledToFarmingGuildFruit = false;
    private boolean travelledToGnomeStrongholdFruit = false;
    private boolean travelledToGnomeVillage = false;

    // Define the Grand Exchange area
    WorldArea grandExchange = new WorldArea(3142, 3470, 50, 50, 0); // Grand Exchange coordinates

    // Check if the player is in the Grand Exchange
    private boolean isInGrandExchange() {
        Player localPlayer = Microbot.getClient().getLocalPlayer();
        return localPlayer != null && grandExchange.contains(localPlayer.getWorldLocation());
    }

    // Log state changes (only once per state)
    private void logStateChange(FarmRunState newState) {
        if (newState != lastState) {
            Microbot.log("State changed to: " + newState);
            lastState = newState;  // Update the last logged state
        }
    }

    private final List<FarmPatch> priorityPatches = List.of(
            FarmPatch.FARMING_GNOME_STRONGHOLD_FRUIT,
            FarmPatch.FARMING_GNOME_STRONGHOLD,
            FarmPatch.FARMING_VARROCK,
            FarmPatch.FARMING_FALADOR,
            FarmPatch.FARMING_TAVERLEY,
            FarmPatch.FARMING_LUMBRIDGE,
            FarmPatch.FARMING_CATHERBY,
            FarmPatch.FARMING_GNOME_VILLAGE,
            FarmPatch.FARMING_GUILD_NORMAL,
            FarmPatch.FARMING_GUILD_FRUIT
    );

    private void initializeSelectedPatches(FarmRunConfig config) {
        selectedPatches.clear();

        for (FarmPatch patch : priorityPatches) {
            switch (patch) {
                case FARMING_GNOME_STRONGHOLD_FRUIT:
                    if (config.gnomeStrongholdFruitPatch()) selectedPatches.add(patch);
                    break;
                case FARMING_GNOME_STRONGHOLD:
                    if (config.gnomeStrongholdNormalPatch()) selectedPatches.add(patch);
                    break;
                case FARMING_GNOME_VILLAGE:
                    if (config.gnomeVillageFruit()) selectedPatches.add(patch);
                    break;
                case FARMING_VARROCK:
                    if (config.varrockPatch()) selectedPatches.add(patch);
                    break;
                case FARMING_FALADOR:
                    if (config.faladorPatch()) selectedPatches.add(patch);
                    break;
                case FARMING_LUMBRIDGE:
                    if (config.lumbridgePatch()) selectedPatches.add(patch);
                    break;
                case FARMING_TAVERLEY:
                    if (config.taverleyPatch()) selectedPatches.add(patch);
                    break;
                case FARMING_CATHERBY:
                    if (config.catherbyPatch()) selectedPatches.add(patch);
                    break;
                case FARMING_GUILD_NORMAL:
                    if (config.farmingGuildNormalPatch()) selectedPatches.add(patch);
                    break;
                case FARMING_GUILD_FRUIT:
                    if (config.farmingGuildFruitPatch()) selectedPatches.add(patch);
                    break;
            }
        }
    }

    public boolean run(FarmRunConfig config) {
        initializeSelectedPatches(config);
        if (selectedPatches.isEmpty()) {
            Microbot.log("No patches selected in the configuration.");
            return false;
        }

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;

                logStateChange(state);

                switch (state) {
                    case RESET:
                        if (!isInGrandExchange()) {
                            Microbot.log("Not in the Grand Exchange. Please start the script in the Grand Exchange.");
                            shutdown();
                        } else {
                            state = FarmRunState.BANKING;
                        }
                        break;

                    case BANKING:
                        Microbot.log("Opening bank to withdraw farming items...");
                        if (Rs2Bank.openBank()) {
                            Rs2Bank.depositAll();
                            boolean success = withdrawFarmingItems(config);
                            Rs2Bank.closeBank();
                            if (success) {
                                currentPatchIndex = 0;
                                state = FarmRunState.RUNNING;
                            } else {
                                Microbot.log("Failed to withdraw required items. Shutting down the script.");
                                shutdown();
                            }
                        }
                        break;

                    case RUNNING:
                        if (currentPatchIndex < selectedPatches.size()) {
                            FarmPatch currentPatch = selectedPatches.get(currentPatchIndex);
                            processPatch(config, currentPatch);
                        } else {
                            state = FarmRunState.FINISHED;
                        }
                        break;

                    case FINISHED:
                        Microbot.log("Farm run finished.");

                        // Check if returnAfterFinish is enabled in the config
                        if (config.returnAfterFinish()) {
                            Microbot.log("Returning to the Grand Exchange using Varrock Teleport...");
                            if (Rs2Inventory.hasItem(getTeleportItemForPatch(FarmRunState.FARMING_VARROCK))) {
                                Rs2Inventory.interact(getTeleportItemForPatch(FarmRunState.FARMING_VARROCK), "Break");
                                sleepUntil(() -> !Rs2Player.isAnimating() && !Rs2Player.isMoving(), 10000);
                            } else {
                                Microbot.log("No Varrock Teleport found! Walking to the Grand Exchange...");
                                Rs2Walker.walkTo(BankLocation.GRAND_EXCHANGE.getWorldPoint());
                            }
                        } else {
                            Microbot.log("Walking back to the Grand Exchange.");
                            Rs2Walker.walkTo(BankLocation.GRAND_EXCHANGE.getWorldPoint());
                        }

                        shutdown();
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

    // Process each patch based on the configuration
    // Process each patch based on the configuration
    private void processPatch(FarmRunConfig config, FarmPatch patch) {
        switch (patch) {
            case FARMING_GNOME_VILLAGE:
                if (!travelledToGnomeVillage) {
                    Microbot.log("Teleporting to Gnome Village patch...");
                    if (useTeleport(FarmRunState.FARMING_GNOME_VILLAGE)) {
                        travelledToGnomeVillage = true;
                    }
                }
                if (Rs2Walker.walkTo(FarmPatch.FARMING_GNOME_VILLAGE.getPatchLocation())) {
                    if (performFarmActions(config, FarmPatch.FARMING_GNOME_VILLAGE, 7963)) {
                        travelledToGnomeVillage = false;
                        currentPatchIndex++;
                    }
                }
                break;

            case FARMING_GNOME_STRONGHOLD:
                if (!travelledToGnomeStronghold) {
                    Microbot.log("Teleporting to Gnome Stronghold Normal patch...");
                    if (useTeleport(FarmRunState.FARMING_GNOME_STRONGHOLD)) {
                        travelledToGnomeStronghold = true;
                    }
                }
                if (Rs2Walker.walkTo(FarmPatch.FARMING_GNOME_STRONGHOLD.getPatchLocation())) {
                    if (performFarmActions(config, FarmPatch.FARMING_GNOME_STRONGHOLD, 19147)) {
                        travelledToGnomeStronghold = false;
                        currentPatchIndex++;
                    }
                }
                break;

            case FARMING_GNOME_STRONGHOLD_FRUIT:
                if (!travelledToGnomeStrongholdFruit) {
                    Microbot.log("Teleporting to Gnome Stronghold Fruit patch...");
                    if (useTeleport(FarmRunState.FARMING_GNOME_STRONGHOLD_FRUIT)) {
                        travelledToGnomeStrongholdFruit = true;
                    }
                }
                if (Rs2Walker.walkTo(FarmPatch.FARMING_GNOME_STRONGHOLD_FRUIT.getPatchLocation())) {
                    if (performFarmActions(config, FarmPatch.FARMING_GNOME_STRONGHOLD_FRUIT, 7962)) {
                        travelledToGnomeStrongholdFruit = false;
                        currentPatchIndex++;
                    }
                }
                break;

            case FARMING_VARROCK:
                if (!travelledToVarrock) {
                    Microbot.log("Teleporting to Varrock patch...");
                    if (useTeleport(FarmRunState.FARMING_VARROCK)) {
                        travelledToVarrock = true;
                    }
                }
                if (Rs2Walker.walkTo(FarmPatch.FARMING_VARROCK.getPatchLocation())) {
                    if (performFarmActions(config, FarmPatch.FARMING_VARROCK, 8390)) {
                        travelledToVarrock = false;
                        currentPatchIndex++;
                    }
                }
                break;

            case FARMING_FALADOR:
                if (!travelledToFalador) {
                    Microbot.log("Teleporting to Falador patch...");
                    if (useTeleport(FarmRunState.FARMING_FALADOR)) {
                        travelledToFalador = true;
                    }
                }
                if (Rs2Walker.walkTo(FarmPatch.FARMING_FALADOR.getPatchLocation())) {
                    if (performFarmActions(config, FarmPatch.FARMING_FALADOR, 8389)) {
                        travelledToFalador = false;
                        currentPatchIndex++;
                    }
                }
                break;

            case FARMING_LUMBRIDGE:
                if (!travelledToLumbridge) {
                    Microbot.log("Teleporting to Lumbridge patch...");
                    if (useTeleport(FarmRunState.FARMING_LUMBRIDGE)) {
                        travelledToLumbridge = true;
                    }
                }
                if (Rs2Walker.walkTo(FarmPatch.FARMING_LUMBRIDGE.getPatchLocation())) {
                    if (performFarmActions(config, FarmPatch.FARMING_LUMBRIDGE, 8391)) {
                        travelledToLumbridge = false;
                        currentPatchIndex++;
                    }
                }
                break;

            case FARMING_TAVERLEY:
                if (!travelledToTaverley) {
                    Microbot.log("Teleporting to Taverley patch...");
                    if (useTeleport(FarmRunState.FARMING_TAVERLEY)) {
                        travelledToTaverley = true;
                    }
                }
                if (Rs2Walker.walkTo(FarmPatch.FARMING_TAVERLEY.getPatchLocation())) {
                    if (performFarmActions(config, FarmPatch.FARMING_TAVERLEY, 8388)) {
                        travelledToTaverley = false;
                        currentPatchIndex++;
                    }
                }
                break;

            case FARMING_CATHERBY:
                if (!travelledToCatherby) {
                    Microbot.log("Teleporting to Catherby patch...");
                    if (useTeleport(FarmRunState.FARMING_CATHERBY)) {
                        travelledToCatherby = true;
                    }
                }
                if (Rs2Walker.walkTo(FarmPatch.FARMING_CATHERBY.getPatchLocation())) {
                    if (performFarmActions(config, FarmPatch.FARMING_CATHERBY, 7965)) {
                        travelledToCatherby = false;
                        currentPatchIndex++;
                    }
                }
                break;

            case FARMING_GUILD_NORMAL:
                if (!travelledToFarmingGuildNormal) {
                    Microbot.log("Teleporting to Farming Guild Normal Tree patch...");
                    if (Rs2Inventory.hasItem(ItemID.SKILLS_NECKLACE)) {
                        Rs2Inventory.interact(ItemID.SKILLS_NECKLACE, "Rub");
                        sleepUntil(() -> !Rs2Player.isAnimating() && !Rs2Player.isMoving(), 10000);
                        travelledToFarmingGuildNormal = true;
                    }
                }
                if (Rs2Walker.walkTo(FarmPatch.FARMING_GUILD_NORMAL.getPatchLocation())) {
                    if (performFarmActions(config, FarmPatch.FARMING_GUILD_NORMAL, 33732)) {
                        travelledToFarmingGuildNormal = false;
                        currentPatchIndex++;
                    }
                }
                break;

            case FARMING_GUILD_FRUIT:
                if (!travelledToFarmingGuildFruit) {
                    Microbot.log("Walking to Farming Guild Fruit patch...");
                    travelledToFarmingGuildFruit = true;
                }
                if (Rs2Walker.walkTo(FarmPatch.FARMING_GUILD_FRUIT.getPatchLocation())) {
                    if (performFarmActions(config, FarmPatch.FARMING_GUILD_FRUIT, 34007)) {
                        travelledToFarmingGuildFruit = false;
                        currentPatchIndex++;
                    }
                }
                break;
        }
    }



    // Perform farming actions and ensure all are completed before moving to the next patch
    private boolean performFarmActions(FarmRunConfig config, FarmPatch patch, int objectId) {
        boolean allActionsCompleted = true;

        if (patch.isFruitTree()) {
            FruitTreeMaterial material = config.fruitTree();
            allActionsCompleted &= performFruitTreeActions(config, patch, objectId, material);
        } else {
            NormalTreeMaterial material = config.normalTree();
            allActionsCompleted &= performNormalTreeActions(config, patch, objectId, material);
        }

        return allActionsCompleted;
    }

    private boolean performNormalTreeActions(FarmRunConfig config, FarmPatch patch, int objectId, NormalTreeMaterial material) {
        return performTreeActions(config, patch, objectId, material);
    }

    private boolean performFruitTreeActions(FarmRunConfig config, FarmPatch patch, int objectId, FruitTreeMaterial material) {
        return performTreeActions(config, patch, objectId, material);
    }


    // Common tree action logic with added checks after each step
    private <T> boolean performTreeActions(FarmRunConfig config, FarmPatch patch, int objectId, T material) {
        boolean allActionsCompleted = true;
        boolean compostUsed = false; // Track if compost has already been applied

        ObjectComposition tree = Rs2GameObject.findObjectComposition(objectId);

        // Step 1: Check health if applicable
        if (tree != null && Rs2GameObject.hasAction(tree, "check-health")) {
            allActionsCompleted &= checkTree(config, objectId);
            sleep(2000); // Ensure the check-health action is fully completed
        }

        // Step 2: Remove the tree if it's fully grown and ready to be chopped down
        if (tree != null && Rs2GameObject.hasAction(tree, "chop down")) {
            allActionsCompleted &= removeTree(config, objectId, patch.getNpcId());
            sleep(2000); // Adding a short delay to ensure the action is fully completed
        }

        // Step 3: Rake the patch if needed
        GameObject farmingPatch = Rs2GameObject.findObjectByImposter(objectId, "rake");
        if (farmingPatch != null) {
            allActionsCompleted &= rakePatch(config, objectId);
            sleep(2000); // Small delay after raking
        }

        // Step 4: Apply compost (before planting)
        if (tree == null || Rs2GameObject.hasAction(tree, "Inspect")) {
            if (config.protectionMode() == ProtectionMode.USE_COMPOST) {
                compostUsed = useCompost(config, objectId); // Apply compost before planting
            }

            allActionsCompleted &= plantTree(config, objectId, material); // Plant the tree
            sleep(2000); // Ensure the planting process completes
        }

        // Step 5: Protect the tree (skip compost if already applied)
        if (!compostUsed) {
            protectTree(config, objectId, patch.isFruitTree());
        }

        return allActionsCompleted;
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
            if (config.protectionMode() == ProtectionMode.PROTECT_PATCH_WITH_ITEMS) {
                // Protect with NPC interaction
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
            } else if (config.protectionMode() == ProtectionMode.NO_PROTECTION) {
                Microbot.log("No protection selected.");
            } else {
                Microbot.log("Will protect with compost.");
            }
        }
    }


    private boolean withdrawFarmingItems(FarmRunConfig config) {
        NormalTreeMaterial normalTreeMaterial = config.normalTree();
        FruitTreeMaterial fruitTreeMaterial = config.fruitTree();
        int normalTreeCount = 0;
        int fruitTreeCount = 0;

        // Counters for teleports
        int varrockTeleports = 0;
        int faladorTeleports = 0;
        int lumbridgeTeleports = 0;
        int taverleyTeleports = 0;
        int catherbyTeleports = 0;
        int gnomeTeleports = 0;
        int gnomeStrongholdFruitTeleports = 0;
        int farmingGuildNormalTeleports = 0;
        int farmingGuildFruitTeleports = 0;

        // Count the selected patches and determine what items are needed
        for (FarmPatch patch : selectedPatches) {
            switch (patch) {
                case FARMING_VARROCK:
                    normalTreeCount++;
                    varrockTeleports++;
                    break;
                case FARMING_FALADOR:
                    normalTreeCount++;
                    faladorTeleports++;
                    break;
                case FARMING_LUMBRIDGE:
                    normalTreeCount++;
                    lumbridgeTeleports++;
                    break;
                case FARMING_TAVERLEY:
                    normalTreeCount++;
                    taverleyTeleports++;
                    break;
                case FARMING_CATHERBY:
                    fruitTreeCount++;
                    catherbyTeleports++;
                    break;
                case FARMING_GNOME_VILLAGE:
                    fruitTreeCount++;
                    gnomeTeleports++;
                    break;
                case FARMING_GNOME_STRONGHOLD_FRUIT:
                    fruitTreeCount++;
                    gnomeStrongholdFruitTeleports++;
                    break;
                case FARMING_GUILD_NORMAL:
                    normalTreeCount++;
                    farmingGuildNormalTeleports++;
                    break;
                case FARMING_GUILD_FRUIT:
                    fruitTreeCount++;
                    farmingGuildFruitTeleports++;
                    break;
            }
        }

        Microbot.log("Withdrawing items for " + normalTreeCount + " normal trees and " + fruitTreeCount + " fruit trees.");

        // Withdraw tools (spade, rake, stamina potion)
        Microbot.log("Attempting to withdraw Spade...");
        if (!withdrawItemWithCheck(ItemID.SPADE)) return false;

        if (!config.autoWeed()) {
            Microbot.log("Attempting to withdraw Rake...");
            if (!withdrawItemWithCheck(ItemID.RAKE)) return false;
        }

        Microbot.log("Attempting to withdraw Stamina Potion (4)...");
        if (!withdrawItemWithCheck(ItemID.STAMINA_POTION4, 1)) return false;

        // Withdraw teleport items based on the selected teleport method
        if (config.teleportMethod() == FarmRunConfig.TeleportMethod.TELEPORT_TABLET) {
            // Withdraw teleport tablets
            if (varrockTeleports > 0) {
                Microbot.log("Withdrawing " + varrockTeleports + " Varrock Teleport Tablets...");
                if (!withdrawItemWithCheck(ItemID.VARROCK_TELEPORT, varrockTeleports)) return false;
            }
            if (faladorTeleports > 0) {
                Microbot.log("Withdrawing " + faladorTeleports + " Falador Teleport Tablets...");
                if (!withdrawItemWithCheck(ItemID.FALADOR_TELEPORT, faladorTeleports)) return false;
            }
            if (lumbridgeTeleports > 0) {
                Microbot.log("Withdrawing " + lumbridgeTeleports + " Lumbridge Teleport Tablets...");
                if (!withdrawItemWithCheck(ItemID.LUMBRIDGE_TELEPORT, lumbridgeTeleports)) return false;
            }
            if (taverleyTeleports > 0) {
                Microbot.log("Withdrawing " + taverleyTeleports + " Taverley Teleport Tablets...");
                if (!withdrawItemWithCheck(ItemID.FALADOR_TELEPORT, taverleyTeleports)) return false;
            }
            if (catherbyTeleports > 0) {
                Microbot.log("Withdrawing " + catherbyTeleports + " Camelot Teleport Tablets...");
                if (!withdrawItemWithCheck(ItemID.CAMELOT_TELEPORT, catherbyTeleports)) return false;
            }
            if (config.returnAfterFinish()) {
                Microbot.log("Return on finish enabled, withdrawing teleport for returning to Grand Exchange.");
                if (!withdrawItemWithCheck(ItemID.VARROCK_TELEPORT, varrockTeleports)) return false;
            }
        } else {
            // Withdraw runes for magic teleport
            if (varrockTeleports > 0) {
                Microbot.log("Withdrawing runes for " + varrockTeleports + " Varrock Teleports...");
                if (!withdrawRunesForTeleport(ItemID.LAW_RUNE, ItemID.FIRE_RUNE, ItemID.AIR_RUNE, varrockTeleports)) return false;
            }
            if (faladorTeleports > 0) {
                Microbot.log("Withdrawing runes for " + faladorTeleports + " Falador Teleports...");
                if (!withdrawRunesForTeleport(ItemID.LAW_RUNE, ItemID.WATER_RUNE, ItemID.AIR_RUNE, faladorTeleports)) return false;
            }
            if (lumbridgeTeleports > 0) {
                Microbot.log("Withdrawing runes for " + lumbridgeTeleports + " Lumbridge Teleports...");
                if (!withdrawRunesForTeleport(ItemID.LAW_RUNE, ItemID.EARTH_RUNE, ItemID.AIR_RUNE, lumbridgeTeleports)) return false;
            }
            if (taverleyTeleports > 0) {
                Microbot.log("Withdrawing runes for " + taverleyTeleports + " Taverley Teleports...");
                if (!withdrawRunesForTeleport(ItemID.LAW_RUNE, ItemID.FIRE_RUNE, ItemID.AIR_RUNE, taverleyTeleports)) return false;
            }
            if (catherbyTeleports > 0) {
                Microbot.log("Withdrawing runes for " + catherbyTeleports + " Camelot Teleports...");
                if (!withdrawRunesForTeleport(ItemID.LAW_RUNE, ItemID.AIR_RUNE, 0, catherbyTeleports)) return false;
            }
            if (config.returnAfterFinish()) {
                Microbot.log("Return on finish enabled, withdrawing teleport for returning to Grand Exchange.");
                if (!withdrawRunesForTeleport(ItemID.LAW_RUNE, ItemID.FIRE_RUNE, ItemID.AIR_RUNE, varrockTeleports)) return false;
            }
        }

        // Withdraw normal and fruit tree saplings
        if (normalTreeCount > 0) {
            Microbot.log("Withdrawing " + normalTreeCount + " normal tree saplings...");
            if (!withdrawItemWithCheck(normalTreeMaterial.getItemId(), normalTreeCount)) return false;
        }

        if (fruitTreeCount > 0) {
            Microbot.log("Withdrawing " + fruitTreeCount + " fruit tree saplings...");
            if (!withdrawItemWithCheck(fruitTreeMaterial.getItemId(), fruitTreeCount)) return false;
        }

        // Withdraw protection items or compost based on the selected protection mode
        if (config.protectionMode() == ProtectionMode.PROTECT_PATCH_WITH_ITEMS) {
            Microbot.log("Withdrawing protection items...");
            if (!withdrawItemWithCheck(normalTreeMaterial.getProtectionItemId(), normalTreeMaterial.getProtectionItemAmount())) return false;
        } else if (config.protectionMode() == ProtectionMode.USE_COMPOST) {
            CompostType compostType = config.compostType();
            Microbot.log("Withdrawing " + compostType.name() + "...");
            if (!withdrawItemWithCheck(compostType.getItemId(), 1)) return false;  // Withdraw selected compost
        }

        // Withdraw coins for payment
        Microbot.log("Withdrawing Coins (10000) for payments...");
        if (!withdrawItemWithCheck(ItemID.COINS_995, 10000)) return false;

        Microbot.log("Successfully withdrew all required items.");
        return true;
    }
    // Helper method to withdraw the necessary runes for a teleport
    private boolean withdrawRunesForTeleport(int lawRuneId, int secondaryRuneId, int tertiaryRuneId, int count) {
        if (!withdrawItemWithCheck(lawRuneId, count)) return false;
        if (secondaryRuneId != 0 && !withdrawItemWithCheck(secondaryRuneId, count)) return false;
        if (tertiaryRuneId != 0 && !withdrawItemWithCheck(tertiaryRuneId, count)) return false;

        return true;
    }


    // Helper method to withdraw an item and ensure it's in the inventory
    private boolean withdrawItemWithCheck(int itemId, int amount) {
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
        sleepUntil(() -> Rs2Inventory.hasItem(itemId), 5000); // Ensure the item is in the inventory
        if (!Rs2Inventory.hasItem(itemId)) {
            Microbot.log("Failed to withdraw item: " + itemId);
            return false;
        }

        missingItemRetries.remove(itemId);  // Reset retries if item is found
        return true;  // Successfully withdrew the item
    }

    private boolean useTeleport(FarmRunState state) {
        int teleportItemId = getTeleportItemForPatch(state);
        if (Rs2Inventory.hasItem(teleportItemId)) {
            Rs2Inventory.interact(teleportItemId, "Break");
            Rs2Player.waitForAnimation();
            sleepUntil(() -> !Rs2Player.isAnimating());
            Rs2Walker.setTarget(null);  // Reset walking target to prevent conflict
            Rs2Player.waitForWalking();
            return true;
        } else {
            Microbot.log("No teleport item found for: " + state.name());
            return false;
        }
    }


    // Helper method to withdraw one item with check
    private boolean withdrawItemWithCheck(int itemId) {
        return withdrawItemWithCheck(itemId, 1);  // Default to withdraw one if no amount is specified
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
}
