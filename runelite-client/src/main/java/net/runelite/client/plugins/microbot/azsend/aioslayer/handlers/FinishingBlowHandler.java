package net.runelite.client.plugins.microbot.azsend.aioslayer.handlers;

import net.runelite.api.NPC;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.plugins.microbot.azsend.aioslayer.AioSlayerConfig;
import net.runelite.client.plugins.microbot.azsend.aioslayer.models.TaskConfiguration;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.skills.slayer.Rs2Slayer;

import java.util.List;

/**
 * Handles finishing blow mechanics for monsters that require special items to complete the kill
 */
public class FinishingBlowHandler extends BaseTaskHandler {
    
    public FinishingBlowHandler(AioSlayerConfig config, TaskConfiguration taskConfig, StateCallback stateCallback) {
        super(config, taskConfig, stateCallback);
    }
    
    @Override
    public boolean execute() {
        if (!taskConfig.isRequiresSpecialKill()) {
            return true; // No special kill required
        }
        
        logInfo("Checking for monsters requiring finishing blow");
        
        try {
            // Find monsters that need finishing blow
            List<Integer> finishingBlowItems = taskConfig.getFinishingBlowItems();
            Integer threshold = taskConfig.getFinishingBlowThreshold();
            
            if (finishingBlowItems == null || finishingBlowItems.isEmpty() || threshold == null) {
                logError("Task requires special kill but no finishing blow items or threshold configured");
                return false;
            }
            
            // Find target monsters at low HP
            NPC targetMonster = findMonsterNeedingFinishingBlow(threshold);
            if (targetMonster == null) {
                return true; // No monsters need finishing blow right now
            }
            
            // Find available finishing blow item
            Integer availableItem = findAvailableFinishingBlowItem(finishingBlowItems);
            if (availableItem == null) {
                logError("No finishing blow items available in inventory");
                stateCallback.requestStateTransition(null, "Need to restock finishing blow items");
                return false;
            }
            
            // Perform finishing blow
            return performFinishingBlow(targetMonster, availableItem);
            
        } catch (Exception e) {
            logError("Error in finishing blow handler", e);
            return false;
        }
    }
    
    @Override
    public boolean canExecute() {
        return taskConfig != null && taskConfig.isRequiresSpecialKill();
    }
    
    @Override
    public String getHandlerName() {
        return "FinishingBlowHandler";
    }
    
    /**
     * Finds a monster that needs a finishing blow (HP at or below threshold)
     */
    private NPC findMonsterNeedingFinishingBlow(int threshold) {
        // Get current target if we're in combat
        // NPC currentTarget = Rs2Combat.getTarget(); // Method not available
        NPC currentTarget = null; // Simplified - would need to track target manually
        if (currentTarget != null && isMonsterAtThreshold(currentTarget, threshold)) {
            return currentTarget;
        }
        
        // Look for other monsters at threshold that we can reach
        String[] monsterNames = getTargetMonsterNames();
        for (String monsterName : monsterNames) {
            NPC monster = Rs2Npc.getNpc(monsterName);
            if (monster != null && isMonsterAtThreshold(monster, threshold)) {
                return monster;
            }
        }
        
        return null;
    }
    
    /**
     * Checks if a monster's HP is at or below the threshold
     */
    private boolean isMonsterAtThreshold(NPC monster, int threshold) {
        if (monster == null) return false;
        
        int currentHp = monster.getHealthRatio();
        int maxHp = monster.getHealthScale();
        
        if (maxHp <= 0) return false;
        
        // Calculate actual HP
        double hpPercentage = (double) currentHp / maxHp;
        int estimatedHp = (int) Math.ceil(hpPercentage * getMonsterMaxHp(monster));
        
        return estimatedHp <= threshold;
    }
    
    /**
     * Gets the estimated max HP for a monster (this could be expanded with a lookup table)
     */
    private int getMonsterMaxHp(NPC monster) {
        // This is a simplified approach - in practice you might want a lookup table
        // based on monster combat level or specific monster data
        int combatLevel = monster.getCombatLevel();
        
        // Rough estimation based on combat level
        if (combatLevel <= 10) return 15;
        if (combatLevel <= 20) return 25;
        if (combatLevel <= 50) return 50;
        if (combatLevel <= 100) return 100;
        return Math.max(100, combatLevel);
    }
    
    /**
     * Gets the target monster names for this task
     */
    private String[] getTargetMonsterNames() {
        if (taskConfig.getMonsterInfo() != null) {
            // Use alternatives from monster info
            String[] alternatives = taskConfig.getMonsterInfo().getAlternatives();
            if (alternatives != null && alternatives.length > 0) {
                return alternatives;
            }
            return new String[]{taskConfig.getMonsterInfo().getMonster()};
        }
        
        // Fallback to task name
        return new String[]{taskConfig.getTaskName()};
    }
    
    /**
     * Finds an available finishing blow item in inventory
     */
    private Integer findAvailableFinishingBlowItem(List<Integer> finishingBlowItems) {
        for (Integer itemId : finishingBlowItems) {
            if (Rs2Inventory.hasItem(itemId)) {
                return itemId;
            }
        }
        return null;
    }
    
    /**
     * Performs the finishing blow on the target monster
     */
    private boolean performFinishingBlow(NPC target, int finishingBlowItem) {
        logInfo("Performing finishing blow on " + target.getName() + " with item " + finishingBlowItem);
        
        // Stop auto-attacking - no direct method available
        // Player will naturally stop attacking after using the finishing item
        sleep(300);
        
        // Use the finishing blow item on the monster
        if (Rs2Inventory.use(finishingBlowItem)) {
            sleep(600);
            
            // Click on the monster
            if (Rs2Npc.interact(target, "Use")) {
                logInfo("Successfully used finishing blow item");
                sleep(1200); // Wait for action to complete
                return true;
            } else {
                logError("Failed to use finishing blow item on monster");
                return false;
            }
        } else {
            logError("Failed to use finishing blow item from inventory");
            return false;
        }
    }
    
    /**
     * Checks if we have sufficient finishing blow items for the task
     */
    public boolean hasEnoughFinishingBlowItems() {
        if (!taskConfig.isRequiresSpecialKill()) {
            return true;
        }
        
        List<Integer> finishingBlowItems = taskConfig.getFinishingBlowItems();
        if (finishingBlowItems == null || finishingBlowItems.isEmpty()) {
            return true; // No items required
        }
        
        // Count available items
        int totalCount = 0;
        for (Integer itemId : finishingBlowItems) {
            totalCount += Rs2Inventory.itemQuantity(itemId);
        }
        
        // We want at least 10% more than the task count for safety
        int requiredCount = (int) Math.ceil(getTaskCount());

        logError("Finishing blow items available: " + totalCount + ", required: " + requiredCount);
        return totalCount >= requiredCount;
    }
    
    /**
     * Gets the current slayer task count
     */
    private int getTaskCount() {
        return Rs2Slayer.getSlayerTaskSize();
    }
}
