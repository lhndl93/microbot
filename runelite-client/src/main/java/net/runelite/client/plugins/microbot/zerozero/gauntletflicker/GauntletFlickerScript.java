package net.runelite.client.plugins.microbot.zerozero.gauntletflicker;

import lombok.SneakyThrows;
import net.runelite.api.Client;
import net.runelite.api.HeadIcon;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class GauntletFlickerScript extends Script {

    private static final Set<Integer> HUNLLEF_IDS = Set.of(
            NpcID.TORMENTED_DEMON,
            NpcID.TORMENTED_DEMON_13600
    );

    private boolean isRunning = false;  // Track script running state
    private HeadIcon lastHeadIcon = null;  // Store the last logged head icon

    public boolean run(GauntletFlickerConfig config) {
        // Now you can use `config` in this method if needed
        Microbot.enableAutoRunOn = false;  // Disable auto-run if applicable
        isRunning = true;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;  // Check if the player is logged in
                if (!super.run()) return;

                onGameTick();  // Execute the core game logic

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);  // Runs every 1 second
        return true;
    }


    @Override
    public void shutdown() {
        super.shutdown();  // Cleanup when the script is stopped
        isRunning = false;
    }

    public void onGameTick() {
        if (!isRunning) return;

        Client client = Microbot.getClient();
        if (client.getLocalPlayer().isDead() || client.getLocalPlayer().getHealthRatio() == 0) {
            return;
        }


        NPC hunllef = getHunllef();  // Find the specific Hunllef NPC
        if (hunllef != null) {
            HeadIcon currentHeadIcon = getHeadIcon(hunllef);  // Get the current head icon

            if (currentHeadIcon != null && currentHeadIcon != lastHeadIcon) {  // Log only if the head icon has changed
                Microbot.log("Head Icon changed: " + currentHeadIcon);
                lastHeadIcon = currentHeadIcon;  // Update the last head icon
            }
        }
    }

    private NPC getHunllef() {
        // Using the getNpcs() method to filter by Hunllef NPCs
        return Rs2Npc.getNpcs()
                .filter(npc -> HUNLLEF_IDS.contains(npc.getId()))  // Check if the NPC ID is one of the Hunllef IDs
                .findFirst()
                .orElse(null);  // Return the first matching Hunllef NPC or null if none are found
    }

    @SneakyThrows
    public static HeadIcon getHeadIcon(NPC npc) {
        Field aq = npc.getClass().getDeclaredField("ay");  // Using reflection to access the head icon field
        aq.setAccessible(true);
        Object aqObj = aq.get(npc);
        if (aqObj == null) {
            aq.setAccessible(false);
            HeadIcon icon = getOldHeadIcon(npc);  // Fallback to older versions of head icons
            if(icon == null){
                return getOlderHeadicon(npc);
            }
            return icon;
        }
        Field aeField = aqObj.getClass().getDeclaredField("aw");
        aeField.setAccessible(true);
        short[] ae = (short[]) aeField.get(aqObj);
        aeField.setAccessible(false);
        aq.setAccessible(false);
        if (ae == null) {
            HeadIcon icon = getOldHeadIcon(npc);  // Another fallback to older versions
            if(icon == null){
                return getOlderHeadicon(npc);
            }
            return icon;
        }
        if (ae.length == 0) {
            HeadIcon icon = getOldHeadIcon(npc);
            if(icon == null){
                return getOlderHeadicon(npc);
            }
            return icon;
        }
        short headIcon = ae[0];
        if (headIcon == -1) {
            HeadIcon icon = getOldHeadIcon(npc);
            if(icon == null){
                return getOlderHeadicon(npc);
            }
            return icon;
        }
        return HeadIcon.values()[headIcon];
    }

    @SneakyThrows
    private static HeadIcon getOldHeadIcon(NPC npc) {
        // Logic for older versions of HeadIcon, if necessary
        return null;
    }

    @SneakyThrows
    private static HeadIcon getOlderHeadicon(NPC npc) {
        // Logic for even older versions of HeadIcon, if necessary
        return null;
    }

}
