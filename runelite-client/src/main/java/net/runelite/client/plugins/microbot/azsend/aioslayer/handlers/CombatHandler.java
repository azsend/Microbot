package net.runelite.client.plugins.microbot.azsend.aioslayer.handlers;

import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.azsend.aioslayer.AioSlayerConfig;
import net.runelite.client.plugins.microbot.azsend.aioslayer.enums.SlayerBotState;
import net.runelite.client.plugins.microbot.azsend.aioslayer.models.TaskConfiguration;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.plugins.microbot.util.skills.slayer.Rs2Slayer;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles combat operations for slayer tasks
 */
public class CombatHandler extends BaseTaskHandler {
    
    private FinishingBlowHandler finishingBlowHandler;
    private Map<String, NpcBlacklistEntry> npcBlacklist = new HashMap<>();
    private Instant lastCombatTime;
    private Instant lastFoodTime;
    private Instant lastPotionTime;
    
    private static final int COMBAT_TIMEOUT_SECONDS = 15;
    private static final int FOOD_COOLDOWN_MS = 3000;
    private static final int POTION_COOLDOWN_MS = 5000;
    private static final int MAX_FAILED_ATTEMPTS = 3;
    
    public CombatHandler(AioSlayerConfig config, TaskConfiguration taskConfig, StateCallback stateCallback) {
        super(config, taskConfig, stateCallback);
        this.finishingBlowHandler = new FinishingBlowHandler(config, taskConfig, stateCallback);
        this.lastCombatTime = Instant.now();
    }
    
    @Override
    public boolean execute() {
        try {
            // Update progress
            int currentTaskCount = Rs2Slayer.getSlayerTaskSize();
            stateCallback.updateProgress(currentTaskCount, currentTaskCount, "Killing monsters");
            
            // Check if task is complete
            if (currentTaskCount == 0 || !Rs2Slayer.hasSlayerTask()) {
                logInfo("Slayer task completed");
                stateCallback.updateState(SlayerBotState.TASK_COMPLETE);
                return true;
            }
            
            // Handle finishing blows for special kill tasks
            if (taskConfig.isRequiresSpecialKill()) {
                finishingBlowHandler.execute();
            }
            
            // Check if we need food or potions
            if (needsFood() || needsPotions()) {
                handleConsumables();
            }
            
            // Check if we need to bank
            if (needsToBank()) {
                stateCallback.requestStateTransition(SlayerBotState.BANKING, "Need supplies");
                return false;
            }
            
            // Loot valuable items
            lootItems();
            
            // Handle combat
            return handleCombat();
            
        } catch (Exception e) {
            logError("Error in combat handler", e);
            stateCallback.reportError("Combat error: " + e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean canExecute() {
        return taskConfig != null && Rs2Slayer.hasSlayerTask();
    }
    
    @Override
    public String getHandlerName() {
        return "CombatHandler";
    }
    
    /**
     * Main combat logic
     */
    private boolean handleCombat() {
        // Check if we're already in combat
        if (Rs2Combat.inCombat()) {
            lastCombatTime = Instant.now();
            
            // Handle special attacks and prayers
            handleCombatMechanics();
            
            return true;
        }
        
        // Find a new target
        NPC target = findTarget();
        if (target == null) {
            logInfo("No suitable targets found");
            sleep(2000);
            return true;
        }
        
        // Attack the target
        return attackTarget(target);
    }
    
    /**
     * Finds a suitable target monster
     */
    private NPC findTarget() {
        String taskName = Rs2Slayer.getSlayerTask();
        List<String> monsterNames = Rs2Slayer.getSlayerMonsters();
        
        if (monsterNames == null || monsterNames.isEmpty()) {
            return null;
        }
        
        // Find the closest non-blacklisted monster
        for (String monsterName : monsterNames) {
            List<NPC> monsters = Rs2Npc.getNpcs(monsterName)
                .map(npcModel -> (NPC) npcModel)
                .collect(Collectors.toList());
            
            for (NPC monster : monsters) {
                if (isValidTarget(monster)) {
                    return monster;
                }
            }
        }
        
        // Clear old blacklist entries if no targets found
        clearOldBlacklistEntries();
        
        return null;
    }
    
    /**
     * Checks if an NPC is a valid target
     */
    private boolean isValidTarget(NPC npc) {
        if (npc == null || npc.isDead() || npc.getHealthRatio() == 0) {
            return false;
        }
        
        // Check if NPC is already in combat with another player
        if (npc.getInteracting() != null && !npc.getInteracting().equals(Rs2Player.getLocalPlayer())) {
            return false;
        }
        
        // Check if NPC is blacklisted
        String npcKey = getNpcKey(npc);
        NpcBlacklistEntry blacklistEntry = npcBlacklist.get(npcKey);
        if (blacklistEntry != null && blacklistEntry.failedAttempts >= MAX_FAILED_ATTEMPTS) {
            return false;
        }
        
        // Check if NPC is within reasonable distance
        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        if (playerLocation.distanceTo(npc.getWorldLocation()) > 10) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Attacks a target monster
     */
    private boolean attackTarget(NPC target) {
        logInfo("Attacking: " + target.getName());
        
        if (Rs2Npc.attack(target)) {
            lastCombatTime = Instant.now();
            sleep(600);
            return true;
        } else {
            // Record failed attack attempt
            recordFailedAttack(target);
            logInfo("Failed to attack " + target.getName());
            return false;
        }
    }
    
    /**
     * Handles combat mechanics like special attacks and prayers
     */
    private void handleCombatMechanics() {
        // Use special attack if configured and we have enough energy
        if (config.useSpecialAttack() && !Rs2Combat.getSpecState()) {
            int specEnergy = Rs2Combat.getSpecEnergy() / 10; // Convert to percentage (0-100)
            int configThreshold = config.specialAttackAt();
            
            if (specEnergy >= configThreshold) {
                logInfo("Activating special attack - Energy: " + specEnergy + "% >= Threshold: " + configThreshold + "%");
                Rs2Combat.setSpecState(true);
            }
        }
        
        // Handle prayers based on task requirements
        handlePrayers();
        
        // Handle protection prayers for dangerous monsters
        handleProtectionPrayers();
    }
    
    /**
     * Handles prayer usage
     */
    private void handlePrayers() {
        if (!config.usePrayer()) return;
        
        // Check if player has sufficient prayer points (simplified)
        if (Rs2Prayer.isOutOfPrayer()) return; // Don't use prayers if out of prayer
        
        // Use offensive prayers
        if (config.useOffensivePrayers()) {
            // Simplified prayer logic - just enable basic strength prayer
            // Note: We'd need Rs2PrayerEnum constants for proper implementation
            logInfo("Prayer system would activate offensive prayers here");
            // Rs2Prayer.toggle(Rs2PrayerEnum.INCREDIBLE_REFLEXES, true);
        }
    }
    
    /**
     * Handles protection prayers for dangerous monsters
     */
    private void handleProtectionPrayers() {
        // NPC target = Rs2Combat.getTarget(); // Method not available
        NPC target = null; // Simplified - would need to track current target
        if (target == null) return;
        
        String monsterName = target.getName().toLowerCase();
        
        // Protection prayers for specific monsters
        if (monsterName.contains("dragon")) {
            // if (!Rs2Prayer.isActivated("Protect from Magic")) {
            //     Rs2Prayer.toggle("Protect from Magic", true);
            // }
            // Prayer protection logic simplified out
        } else if (monsterName.contains("ranger") || monsterName.contains("archer")) {
            // if (!Rs2Prayer.isActivated("Protect from Missiles")) {
            //     Rs2Prayer.toggle("Protect from Missiles", true);
            // }
            // Prayer protection logic simplified out
        }
    }
    
    /**
     * Handles food and potion consumption
     */
    private void handleConsumables() {
        if (needsFood() && canEatFood()) {
            eatFood();
        }
        
        if (needsPotions() && canDrinkPotion()) {
            drinkPotion();
        }
    }
    
    /**
     * Checks if we need food based on configured HP threshold
     */
    private boolean needsFood() {
        int currentHp = Rs2Player.getBoostedSkillLevel(Skill.HITPOINTS);
        int hpThreshold = config.eatAtHp();
        
        boolean shouldEat = currentHp <= hpThreshold;
        
        // Only log when we need to eat to avoid spam
        if (shouldEat) {
            logInfo("Current HP: " + currentHp + " <= Threshold: " + hpThreshold + " - Need to eat!");
        }
        
        return shouldEat;
    }
    
    /**
     * Checks if we need potions
     */
    private boolean needsPotions() {
        // Check prayer potions - simplified approach
        if (taskConfig.isRequiresPrayerPotions()) {
            if (Rs2Prayer.isOutOfPrayer()) {
                return true;
            }
        }
        
        // Check combat potions - simplified for now
        return false; // Combat potions logic would need more work
    }
    
    /**
     * Checks if we need combat potions
     */
    private boolean needsCombatPotion() {
        // Simplified: assume we need combat potion if configured
        return config.useCombatPotions();
    }
    
    private boolean canEatFood() {
        return lastFoodTime == null || 
               Instant.now().toEpochMilli() - lastFoodTime.toEpochMilli() > FOOD_COOLDOWN_MS;
    }
    
    private boolean canDrinkPotion() {
        return lastPotionTime == null || 
               Instant.now().toEpochMilli() - lastPotionTime.toEpochMilli() > POTION_COOLDOWN_MS;
    }
    
    /**
     * Eats food
     */
    private void eatFood() {
        String[] foodItems = {"Shark", "Monkfish", "Lobster", "Tuna", "Salmon"};
        
        for (String food : foodItems) {
            if (Rs2Inventory.hasItem(food)) {
                logInfo("Eating " + food);
                Rs2Inventory.interact(food, "Eat");
                lastFoodTime = Instant.now();
                sleep(1800);
                return;
            }
        }
    }
    
    /**
     * Drinks potions
     */
    private void drinkPotion() {
        // Priority: Prayer > Combat > Other
        if (taskConfig.isRequiresPrayerPotions() && needsPrayerPotion()) {
            drinkPrayerPotion();
        } else if (needsCombatPotion()) {
            drinkCombatPotion();
        }
    }
    
    private boolean needsPrayerPotion() {
        return Rs2Prayer.isOutOfPrayer(); // Simplified approach
    }
    
    private void drinkPrayerPotion() {
        String[] prayerPots = {"Prayer potion(4)", "Prayer potion(3)", "Prayer potion(2)", "Prayer potion(1)"};
        
        for (String pot : prayerPots) {
            if (Rs2Inventory.hasItem(pot)) {
                logInfo("Drinking " + pot);
                Rs2Inventory.interact(pot, "Drink");
                lastPotionTime = Instant.now();
                sleep(1200);
                return;
            }
        }
    }
    
    private void drinkCombatPotion() {
        String[] combatPots = {"Combat potion(4)", "Super attack(4)", "Super strength(4)", "Super defence(4)"};
        
        for (String pot : combatPots) {
            if (Rs2Inventory.hasItem(pot)) {
                logInfo("Drinking " + pot);
                Rs2Inventory.interact(pot, "Drink");
                lastPotionTime = Instant.now();
                sleep(1200);
                return;
            }
        }
    }
    
    /**
     * Loots valuable items
     */
    private void lootItems() {
        if (Rs2Inventory.isFull()) return;
        
        // Loot valuable items nearby
        Rs2GroundItem.lootItemBasedOnValue(config.minLootValue(), 5); // Loot valuable items
    }
    
    /**
     * Checks if we need to bank
     */
    private boolean needsToBank() {
        // Check if we're out of food
        boolean hasFood = Rs2Inventory.hasItem("Shark") || Rs2Inventory.hasItem("Monkfish") || 
                         Rs2Inventory.hasItem("Lobster") || Rs2Inventory.hasItem("Tuna");
        
        if (!hasFood) {
            logInfo("Out of food - need to bank");
            return true;
        }
        
        // Check prayer potions if required
        if (taskConfig.isRequiresPrayerPotions()) {
            boolean hasPrayerPot = Rs2Inventory.hasItem("Prayer potion");
            if (!hasPrayerPot) {
                logInfo("Out of prayer potions - need to bank");
                return true;
            }
        }
        
        // Check finishing blow items
        if (taskConfig.isRequiresSpecialKill()) {
            if (!finishingBlowHandler.hasEnoughFinishingBlowItems()) {
                logInfo("Low on finishing blow items - need to bank");
                return true;
            }
        }
        
        return false;
    }
    
    // Blacklist management methods
    private String getNpcKey(NPC npc) {
        return npc.getName() + "_" + npc.getWorldLocation().toString();
    }
    
    private void recordFailedAttack(NPC npc) {
        String npcKey = getNpcKey(npc);
        NpcBlacklistEntry existing = npcBlacklist.get(npcKey);
        
        int attempts = existing != null ? existing.failedAttempts + 1 : 1;
        npcBlacklist.put(npcKey, new NpcBlacklistEntry(npc.getWorldLocation(), attempts));
        
        logInfo("Recorded failed attack on " + npc.getName() + " (attempt " + attempts + ")");
    }
    
    private void clearOldBlacklistEntries() {
        long cutoff = System.currentTimeMillis() - 30000; // 30 seconds
        npcBlacklist.entrySet().removeIf(entry -> entry.getValue().timestamp < cutoff);
    }
    
    private static class NpcBlacklistEntry {
        final net.runelite.api.coords.WorldPoint lastKnownPosition;
        final int failedAttempts;
        final long timestamp;
        
        NpcBlacklistEntry(net.runelite.api.coords.WorldPoint position, int attempts) {
            this.lastKnownPosition = position;
            this.failedAttempts = attempts;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
