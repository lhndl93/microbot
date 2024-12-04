package net.runelite.client.plugins.microbot.zerozero.aiopvmhelper;


import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.NpcID;
import net.runelite.api.VarPlayer;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.zerozero.aiopvmhelper.utils.Utils;
import net.runelite.client.plugins.microbot.zerozero.aiopvmhelper.models.*;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcManager;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum;


import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.microbot.util.npc.Rs2NpcManager.attackStyleMap;


public class AIOPvmScript extends Script {
    public static double version = 0.1;

    public BOSS currentBoss = BOSS.NONE;

    private BossMonster currentTarget = null;
    public PRAYSTYLE prayStyle = PRAYSTYLE.OFF;
    public AIOPvmConfig config;

    public boolean run(AIOPvmConfig config) throws Exception {
        Microbot.enableAutoRunOn = false;
        Rs2NpcManager.loadJson();
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run()) return;
            if (!Microbot.isLoggedIn() || Microbot.pauseAllScripts) return;
            this.config = config;
            try {
                WorldPoint instancePoint = WorldPoint.fromLocalInstance(Microbot.getClient(), Rs2Player.getLocalLocation());

                if (config.isScurriusOn()) {
                    handleScurrius(instancePoint);
                } else if (config.isArcheoOn()) {
                    handleArcheologist();
                } else if (config.isOborOn()) {
                    handleObor();
                } else {
                    Microbot.status = "IDLE";
                    currentBoss = BOSS.NONE;
                }
            } catch (Exception ex) {
                Utils.logOnceToChat("AIOPVM", "Error: " + ex.getMessage(), "error", true, config);
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);

        return true;
    }

    private void handleScurrius(WorldPoint instancePoint) {
        if (instancePoint.getRegionID() != 13210 || instancePoint.getRegionX() < 23) return;

        if (currentTarget == null ||
                ((currentTarget.id != NpcID.SCURRIUS_7222 && config.GET_INSTANCE() == INSTANCES.PRIVATE) ||
                        (currentTarget.id != NpcID.SCURRIUS && config.GET_INSTANCE() == INSTANCES.PUBLIC))) {
            setupScurriusTarget();
        }

        if (isNearNPC(currentTarget.id)) {
            currentBoss = BOSS.SCURRIUS;
            Microbot.status = "PRAYING";

            if (prayStyle == null || prayStyle == PRAYSTYLE.OFF) {
                prayStyle = PRAYSTYLE.MELEE;
            }

            currentTarget.npc = Rs2Npc.getNpc(currentTarget.id);

            switch (config.PRAYER_MODE()) {
                case AUTO:
                    handleScurryPrayers(true, false); // AUTO mode does not flick prayers
                    break;
                case FLICK:
                    handleScurryPrayers(true, true); // FLICK mode activates flicking
                    break;
                case NONE:
                    turnOffPrayers(); // NONE disables all prayers
                    break;
                case VISUAL:
                    Microbot.status = "Visual prayer mode active.";
                    break;
            }
        } else if (currentTarget.npc != null && currentTarget.npc.isDead()) {
            resetBoss("Scurry");
        }
    }


    private void setupScurriusTarget() {
        if (config.GET_INSTANCE() == INSTANCES.PRIVATE) {
            this.currentTarget = new BossMonster(NpcID.SCURRIUS_7222, new int[]{10693}, new int[]{10695, 10694}, new int[]{10697, 10698});
        } else {
            this.currentTarget = new BossMonster(NpcID.SCURRIUS, new int[]{10693}, new int[]{10695, 10694}, new int[]{10696, 10697});
        }
        Utils.logOnceToChat("AIOPVM", "Generated Scurry in code with known values", "info", true, config);
    }

    private void handleArcheologist() {
        if (currentTarget == null || currentTarget.id != NpcID.DERANGED_ARCHAEOLOGIST) {
            this.currentTarget = new BossMonster(NpcID.DERANGED_ARCHAEOLOGIST);
            Utils.logOnceToChat("AIOPVM", "Generated Archeologist in code with known values", "info", true, config);
        }

        //Utils.logOnceToChat("AIOPVM", "Current animation of target: " + currentTarget.npc.getAnimation(), "debug", true, config);
        //Utils.logOnceToChat("AIOPVM", "Current world area of target: " + currentTarget.npc.getWorldArea(), "debug", true, config);

        if (isNearNPC(currentTarget.id)) {
            currentBoss = BOSS.DERANGED_ARCHEOLGIST;
            Microbot.status = "PRAYING";

            if (prayStyle == null) {
                prayStyle = PRAYSTYLE.RANGED;
            }

            currentTarget.npc = Rs2Npc.getNpc(currentTarget.id);

            switch (config.PRAYER_MODE()) {
                case AUTO:
                    handleArcheoPrayers(true);
                    break;
                case FLICK:
                case NONE:
                    break;
            }
        } else if (currentTarget.npc != null && currentTarget.npc.isDead()) {
            resetBoss("Archeologist");
        }
    }

    private void handleObor() {
        if (currentTarget == null || currentTarget.id != NpcID.OBOR) {
            this.currentTarget = new BossMonster(NpcID.OBOR, new int[]{-1}, new int[]{-1});
            Utils.logOnceToChat("AIOPVM", "Generated Obor in code with known values", "info", true, config);
        }

        if (isNearNPC(currentTarget.id)) {
            currentBoss = BOSS.OBOR;
            Microbot.status = "PRAYING";

            //Utils.logOnceToChat("AIOPVM", "Current animation of target: " + currentTarget.npc.getAnimation(), "debug", true, config);
            //Utils.logOnceToChat("AIOPVM", "Current world area of target: " + currentTarget.npc.getWorldArea(), "debug", true, config);

            if (prayStyle == null || prayStyle == PRAYSTYLE.OFF) {
                prayStyle = PRAYSTYLE.RANGED;
            }

            currentTarget.npc = Rs2Npc.getNpc(currentTarget.id);

            switch (config.PRAYER_MODE()) {
                case AUTO:
                    handleOborPrayers(true);
                    break;
                case FLICK:
                case NONE:
                    break;
            }
        } else if (currentTarget.npc != null && currentTarget.npc.isDead()) {
            resetBoss("Obor");
        }
    }

    private void resetBoss(String bossName) {
        Utils.logOnceToChat("AIOPVM", bossName + " is dead, resetting", "info", true, config);
        turnOffPrayers();
        prayStyle = PRAYSTYLE.OFF;
        Microbot.status = "IDLE";
        sleepUntil(() -> isNearNPC(currentTarget.id));
    }


    public void handlePrayers()
    {
        if (config == null) return;

        if (config.PRAYER_MODE() == PRAY_MODE.NONE) {
            turnOffPrayers();
            return;
        }

        if (Microbot.getClient().getLocalPlayer() == null) {
            return;
        }

        if(Rs2Prayer.isOutOfPrayer()) {
            return;
        }

        if (config.isScurriusOn() && currentBoss == BOSS.SCURRIUS) {
            handleScurryPrayers(true, config.PRAYER_MODE() == PRAY_MODE.FLICK);
        }

        if (config.SPEC_WEAPON() != SPEC_WEAPON.NONE && currentBoss != BOSS.NONE) {
            handleSpec();
        }
    }

    private  boolean isNearNPC(int id) {
        net.runelite.api.NPC boss = Rs2Npc.getNpc(id);
        return boss != null;
    }

    private void handleSpec() {
        if (Microbot.getClient().getLocalPlayer() == null) {
            return;
        }
        if(currentTarget == null || currentBoss == BOSS.NONE) {
            return;
        }

        if (!Rs2Inventory.contains(config.SPEC_WEAPON().getName()) || config.SPEC_WEAPON() == SPEC_WEAPON.NONE) {
            return;
        }

        int currentSpecEnergy = Microbot.getClient().getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT);
        if(currentSpecEnergy >= config.SPEC_WEAPON().getSpecEnergy() && Rs2Equipment.get(EquipmentInventorySlot.WEAPON).name != config.SPEC_WEAPON().getName()) {
            Rs2Inventory.wield(config.SPEC_WEAPON().getName());
            toggleSpecialAttack();
            if(Rs2Combat.getSpecState()) {
                Rs2Npc.interact(currentTarget.id, "attack");
            }

        }
        else if (currentSpecEnergy >= config.SPEC_WEAPON().getSpecEnergy() && Rs2Equipment.get(EquipmentInventorySlot.WEAPON).name == config.SPEC_WEAPON().getName()) {
            toggleSpecialAttack();
            if(Rs2Combat.getSpecState()) {
                Rs2Npc.interact(currentTarget.id, "attack");
            }
        }
    }

    public void toggleSpecialAttack() {
        Utils.logOnceToChat("AIOPVM", "Current spec is " + Rs2Combat.getSpecState(), "info", true, config);
        if (!Rs2Combat.getSpecState()) {
            Utils.logOnceToChat("AIOPVM", "Turning spec on", "info", true, config);
            Rs2Combat.setSpecState(true, config.SPEC_WEAPON().getSpecEnergy());
        }
    }

    private void handleScurryPrayers(boolean on, boolean flick) {
        if (!on || config.PRAYER_MODE() == PRAY_MODE.NONE) {
            turnOffPrayers();
            return;
        }

        if (config.PRAYER_MODE() == PRAY_MODE.AUTO) {
            handleProtectMelee(flick);
            handleProtectRange(flick);
            handleProtectMagic(flick);
            handleDamagePrayers();
        }
    }


    private void handleArcheoPrayers(boolean on) {
        if (!on || config.PRAYER_MODE() == PRAY_MODE.NONE) {
            turnOffPrayers();
            return;
        }

        if (config.PRAYER_MODE() == PRAY_MODE.AUTO) {
            if (!Rs2Prayer.isPrayerActive(Rs2PrayerEnum.PROTECT_RANGE)) {
                Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_RANGE, true);
                Utils.logOnceToChat("AIOPVM", "Activated PROTECT_RANGE for Archeologist.", "info", true, config);
            }

            handleDamagePrayers();
        }
    }


    private void handleOborPrayers(boolean on) {
        if (!on || config.PRAYER_MODE() == PRAY_MODE.NONE) {
            turnOffPrayers();
            return;
        }

        if (config.PRAYER_MODE() == PRAY_MODE.AUTO) {
            if (!Rs2Prayer.isPrayerActive(Rs2PrayerEnum.PROTECT_RANGE)) {
                Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_RANGE, true);
                Utils.logOnceToChat("AIOPVM", "Activated PROTECT_RANGE for Obor.", "info", true, config);
            }

            handleDamagePrayers();
        }
    }


    public void turnOffPrayers() {
        String logMessage = "Turning off all prayers.";

        switch (prayStyle) {
            case MAGE:
                if (Rs2Prayer.isPrayerActive(Rs2PrayerEnum.PROTECT_MAGIC)) {
                    Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_MAGIC, false);
                    Utils.logOnceToChat("AIOPVM", "Deactivated PROTECT_MAGIC.", "info", true, config);
                }
                break;
            case RANGED:
                if (Rs2Prayer.isPrayerActive(Rs2PrayerEnum.PROTECT_RANGE)) {
                    Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_RANGE, false);
                    Utils.logOnceToChat("AIOPVM", "Deactivated PROTECT_RANGE.", "info", true, config);
                }
                break;
            case MELEE:
                if (Rs2Prayer.isPrayerActive(Rs2PrayerEnum.PROTECT_MELEE)) {
                    Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_MELEE, false);
                    Utils.logOnceToChat("AIOPVM", "Deactivated PROTECT_MELEE.", "info", true, config);
                }
                break;
        }

        if (config.DAMAGE_PRAYER() != DAMAGE_PRAYERS.NONE) {
            if (config.DAMAGE_PRAYER() == DAMAGE_PRAYERS.PIETY && Rs2Prayer.isPrayerActive(Rs2PrayerEnum.PIETY)) {
                Rs2Prayer.toggle(Rs2PrayerEnum.PIETY, false);
                Utils.logOnceToChat("AIOPVM", "Deactivated PIETY.", "info", true, config);
            }
            if (config.DAMAGE_PRAYER() == DAMAGE_PRAYERS.AUGURY && Rs2Prayer.isPrayerActive(Rs2PrayerEnum.AUGURY)) {
                Rs2Prayer.toggle(Rs2PrayerEnum.AUGURY, false);
                Utils.logOnceToChat("AIOPVM", "Deactivated AUGURY.", "info", true, config);
            }
            if (config.DAMAGE_PRAYER() == DAMAGE_PRAYERS.RIGOUR && Rs2Prayer.isPrayerActive(Rs2PrayerEnum.RIGOUR)) {
                Rs2Prayer.toggle(Rs2PrayerEnum.RIGOUR, false);
                Utils.logOnceToChat("AIOPVM", "Deactivated RIGOUR.", "info", true, config);
            }
        }

        prayStyle = PRAYSTYLE.OFF;
    }


    private void handleDamagePrayers () {
        if(config.DAMAGE_PRAYER() != DAMAGE_PRAYERS.NONE) {
            if(config.DAMAGE_PRAYER() == DAMAGE_PRAYERS.PIETY && !Rs2Prayer.isPrayerActive(Rs2PrayerEnum.PIETY)) {
                    Rs2Prayer.toggle(Rs2PrayerEnum.PIETY, true);
            }
            if(config.DAMAGE_PRAYER() == DAMAGE_PRAYERS.AUGURY && !Rs2Prayer.isPrayerActive(Rs2PrayerEnum.AUGURY)) {
                Rs2Prayer.toggle(Rs2PrayerEnum.AUGURY, true);
            }
            if(config.DAMAGE_PRAYER() == DAMAGE_PRAYERS.RIGOUR && !Rs2Prayer.isPrayerActive(Rs2PrayerEnum.RIGOUR)) {
                Rs2Prayer.toggle(Rs2PrayerEnum.RIGOUR, true);
            }
        }
    }

    private void handleProtectMelee(boolean flick) {
        if ((Arrays.stream(currentTarget.attackAnimsMelee).anyMatch(x -> x == currentTarget.npc.getAnimation())  && !Rs2Prayer.isPrayerActive(Rs2PrayerEnum.PROTECT_MELEE)) || (prayStyle == PRAYSTYLE.MELEE && !Rs2Prayer.isPrayerActive(Rs2PrayerEnum.PROTECT_MELEE))) {
            if(!flick) {
                Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_MELEE, true);
            } else {
                Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_MELEE, true);
                sleep(400);
                Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_MELEE, false);

            }
            prayStyle = PRAYSTYLE.MELEE;
        }
    }

    private void handleProtectRange (boolean flick) {
        if ((Arrays.stream(currentTarget.attackAnimsRange).anyMatch(x -> x == currentTarget.npc.getAnimation())  && !Rs2Prayer.isPrayerActive(Rs2PrayerEnum.PROTECT_RANGE)) || (prayStyle == PRAYSTYLE.RANGED && !Rs2Prayer.isPrayerActive(Rs2PrayerEnum.PROTECT_RANGE))) {
            if(!flick) {
                Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_RANGE, true);

            } else {
                Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_RANGE, true);
                sleep(400);
                Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_RANGE, false);
            }
            prayStyle = PRAYSTYLE.RANGED;
        }
    }

    private void handleProtectMagic (boolean flick) {
        if ((Arrays.stream(currentTarget.attackAnimsMage).anyMatch(x -> x == currentTarget.npc.getAnimation())  && !Rs2Prayer.isPrayerActive(Rs2PrayerEnum.PROTECT_MAGIC))|| (prayStyle == PRAYSTYLE.MAGE && !Rs2Prayer.isPrayerActive(Rs2PrayerEnum.PROTECT_MAGIC))) {
            if(!flick) {
                Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_MAGIC, true);

            } else {
                Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_MAGIC, true);
                sleep(400);
                Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_MAGIC, false);
            }
            prayStyle = PRAYSTYLE.MAGE;
        }
    }

    public void updateDamagePrayer(DAMAGE_PRAYERS newPrayer) {
        String logMessage;
        if (newPrayer == DAMAGE_PRAYERS.NONE) {
            turnOffPrayers();
            logMessage = "All damage prayers deactivated.";
        } else {
            handleDamagePrayers();
            logMessage = "Activated damage prayer: " + newPrayer.toString();
        }

        Utils.logOnceToChat("BossAssist", logMessage, "info", true, config);
    }


    @Override
    public void shutdown() {
        super.shutdown();
    }
}
