package net.runelite.client.plugins.microbot.zerozero.farmrun;

import net.runelite.api.GameObject;
import net.runelite.api.ItemID;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.farming.enums.FarmingMaterial;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.zerozero.farmrun.enums.FarmRunState;

import java.util.concurrent.TimeUnit;

public class FarmRunScript extends Script {

    private static final WorldPoint TREE_PATCH_VARROCK = new WorldPoint(3226, 3458, 0);
    private static final WorldPoint TREE_PATCH_FALADOR = new WorldPoint(3003, 3376, 0);
    private static final WorldPoint TREE_PATCH_LUMBRIDGE = new WorldPoint(3193, 3228, 0);
    private static final WorldPoint TREE_PATCH_TAVERLEY = new WorldPoint(2933, 3436, 0);

    private static final WorldPoint FRUIT_TREE_PATCH_CATHERBY = new WorldPoint(2809, 3452, 0);
    private static final WorldPoint FRUIT_TREE_PATCH_GNOME = new WorldPoint(2436, 3416, 0);

    public static double version = 1.0;

    private FarmRunState state = FarmRunState.RESET;

    public boolean run(FarmRunConfig config) {
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;

                switch (state) {
                    case RESET:
                        startRun(config);
                        break;
                    case FARMING_VARROCK:
                        processTreePatch(config, TREE_PATCH_VARROCK, ItemID.VARROCK_TELEPORT, FarmRunState.FARMING_FALADOR);
                        break;
                    case FARMING_FALADOR:
                        processTreePatch(config, TREE_PATCH_FALADOR, ItemID.FALADOR_TELEPORT, FarmRunState.FARMING_LUMBRIDGE);
                        break;
                    case FARMING_LUMBRIDGE:
                        processTreePatch(config, TREE_PATCH_LUMBRIDGE, ItemID.LUMBRIDGE_TELEPORT, FarmRunState.FARMING_TAVERLEY);
                        break;
                    case FARMING_TAVERLEY:
                        processTreePatch(config, TREE_PATCH_TAVERLEY, ItemID.TAVERLEY_TELEPORT, FarmRunState.FARMING_CATHERBY);
                        break;
                    case FARMING_CATHERBY:
                        processFruitTreePatch(config, FRUIT_TREE_PATCH_CATHERBY, ItemID.CATHERBY_TELEPORT, FarmRunState.FARMING_GNOME);
                        break;
                    case FARMING_GNOME:
                        processFruitTreePatch(config, FRUIT_TREE_PATCH_GNOME, ItemID.GNOME_STRONGHOLD_TELEPORT, FarmRunState.FINISHED);
                        break;
                    case FINISHED:
                        finishRun();
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

    private void startRun(FarmRunConfig config) {
        // Check if teleporting is enabled and start farming
        if (config.TELEPORT()) {
            state = FarmRunState.FARMING_VARROCK;
        }
    }

    private void processTreePatch(FarmRunConfig config, WorldPoint patchLocation, int teleportItem, FarmRunState nextState) {
        if (Rs2Inventory.hasItem(teleportItem)) {
            Rs2Inventory.interact(teleportItem, "Break");
            sleep(4000);
        }

        if (Rs2Walker.walkTo(patchLocation)) {
            if (plantTree(config, patchLocation)) {
                state = nextState;
            }
        }
    }

    private void processFruitTreePatch(FarmRunConfig config, WorldPoint patchLocation, int teleportItem, FarmRunState nextState) {
        if (Rs2Inventory.hasItem(teleportItem)) {
            Rs2Inventory.interact(teleportItem, "Break");
            sleep(4000);
        }

        if (Rs2Walker.walkTo(patchLocation)) {
            if (plantTree(config, patchLocation)) {
                state = nextState;
            }
        }
    }

    private boolean plantTree(FarmRunConfig config, int objectId, int npcId, int totalItemsForNextStep, FarmRunState nextState) {
        try {
            FarmingMaterial material = config.farmingMaterial();  // Get the selected material from the config

            // Get the tree object from the game
            final ObjectComposition tree = Rs2GameObject.findObjectComposition(objectId);

            // If a tree is present and its name matches the selected material
            if (tree != null && tree.getImpostor().getName().equalsIgnoreCase(material.getName())) {

                // Check if the tree can be checked for health
                if (Rs2GameObject.hasAction(tree, "check-health")) {
                    Rs2GameObject.interact(objectId, "check-health");
                    int currentFarmingExp = Microbot.getClient().getSkillExperience(Skill.FARMING);
                    sleepUntilOnClientThread(() -> currentFarmingExp != Microbot.getClient().getSkillExperience(Skill.FARMING));

                    // If the tree can be chopped down
                } else if (Rs2GameObject.hasAction(tree, "chop down")) {
                    Rs2Npc.interact(npcId, "pay");
                    sleepUntil(() -> Rs2Widget.hasWidget("pay 200 coins"));
                    Rs2Keyboard.typeString("1");

                    // Otherwise, pay to protect the tree
                } else {
                    if (tree != null
                            && !Rs2GameObject.hasAction(tree, "chop down")
                            && !Rs2GameObject.hasAction(tree, "check-health")
                            && tree.getImpostor().getName().equalsIgnoreCase(material.getName())) {

                        Rs2Npc.interact(npcId, "pay");
                        sleepUntil(() -> Rs2Inventory.hasItemAmount(material.getProtectionItem(), totalItemsForNextStep, true));
                    }
                }
            } else {
                // If no tree is planted, rake the patch and plant a new tree
                if (!rakeAndPlantTree(objectId, material.getItemName(), tree)) return true;
            }

            // If the required items for the next step are in the inventory
            if (Rs2Inventory.hasItemAmount(material.getItemName(), totalItemsForNextStep, false, true) &&
                    Rs2Inventory.hasItemAmount(material.getProtectionItem(), totalItemsForNextStep, false, true)) {

                // Move to the next state in the farming run
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




    private void finishRun() {
        // Logic to wrap up the farm run, such as teleporting to a bank
        System.out.println("Farming run completed.");
        shutdown();
    }

    @Override
    public void shutdown() {
        super.shutdown();
        state = FarmRunState.RESET;
    }
}
