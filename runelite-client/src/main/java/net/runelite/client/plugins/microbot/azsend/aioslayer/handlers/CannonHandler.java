package net.runelite.client.plugins.microbot.azsend.aioslayer.handlers;

import net.runelite.client.plugins.microbot.azsend.aioslayer.AioSlayerConfig;
import net.runelite.client.plugins.microbot.azsend.aioslayer.models.TaskConfiguration;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2Cannon;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;

/**
 * Simplified cannon handler that uses available Rs2Cannon methods
 */
public class CannonHandler extends BaseTaskHandler {
    
    private long lastCannonCheck = 0;
    private static final long CANNON_CHECK_INTERVAL = 30000; // 30 seconds
    
    public CannonHandler(AioSlayerConfig config, TaskConfiguration taskConfig, StateCallback stateCallback) {
        super(config, taskConfig, stateCallback);
    }
    
    @Override
    public boolean execute() {
        if (!taskConfig.isCannonCompatible()) {
            return true; // Cannon not needed for this task
        }
        
        logInfo("Managing cannon for task: " + taskConfig.getTaskName());
        
        try {
            // Check if it's time to check cannon
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastCannonCheck < CANNON_CHECK_INTERVAL) {
                return true; // Not time to check yet
            }
            
            lastCannonCheck = currentTime;
            
            // Try to repair cannon if broken
            if (Rs2Cannon.repair()) {
                logInfo("Repaired cannon");
                sleep(1000);
            }
            
            // Try to refill cannon if we have cannonballs
            if (Rs2Inventory.hasItem("Cannonball")) {
                if (Rs2Cannon.refill()) {
                    logInfo("Refilled cannon");
                    sleep(1000);
                }
            } else {
                logInfo("No cannonballs available for refill");
            }
            
            return true;
            
        } catch (Exception e) {
            logError("Error in cannon handler", e);
            stateCallback.reportError("Cannon error: " + e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean canExecute() {
        return taskConfig != null && taskConfig.isCannonCompatible();
    }
    
    @Override
    public String getHandlerName() {
        return "CannonHandler";
    }
    
    /**
     * Checks if we have enough cannonballs for the task
     */
    public boolean hasEnoughCannonballs() {
        return Rs2Inventory.count("Cannonball") >= 20; // Minimum threshold
    }
    
    /**
     * Gets the minimum cannonballs needed
     */
    public int getMinimumCannonballs() {
        return 50; // Default minimum
    }
}