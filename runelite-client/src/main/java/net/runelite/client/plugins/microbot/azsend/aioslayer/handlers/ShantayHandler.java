package net.runelite.client.plugins.microbot.azsend.aioslayer.handlers;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.azsend.aioslayer.AioSlayerConfig;
import net.runelite.client.plugins.microbot.azsend.aioslayer.enums.SlayerBotState;
import net.runelite.client.plugins.microbot.azsend.aioslayer.models.TaskConfiguration;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.shop.Rs2Shop;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

/**
 * Handles purchasing desert items from Shantay
 */
public class ShantayHandler extends BaseTaskHandler {
    
    private static final WorldPoint SHANTAY_LOCATION = new WorldPoint(3304, 3123, 0);
    private static final int SHANTAY_NPC_ID = 4642;
    
    // Purchase tracking
    private boolean needsShantayPass = false;
    private int waterskinsToPurchase = 0;
    
    public ShantayHandler(AioSlayerConfig config, TaskConfiguration taskConfig, StateCallback stateCallback) {
        super(config, taskConfig, stateCallback);
    }
    
    @Override
    public boolean execute() {
        logError("Executing Shantay handler for desert items");
        
        try {
            // Determine what we need to purchase
            if (!calculatePurchaseRequirements()) {
                logError("No desert items need to be purchased from Shantay");
                return true;
            }
            
            // Navigate to Shantay
            if (!navigateToShantay()) {
                logError("Failed to navigate to Shantay");
                return false;
            }
            
            // Open trade with Shantay
            if (!openTradeWithShantay()) {
                logError("Failed to open trade with Shantay");
                return false;
            }
            
            // Purchase required items
            if (!purchaseDesertItems()) {
                logError("Failed to purchase desert items from Shantay");
                return false;
            }
            
            // Close shop and finish
            Rs2Shop.closeShop();
            sleep(600);
            
            logError("Successfully purchased all required desert items from Shantay");
            return true;
            
        } catch (Exception e) {
            logError("Error in Shantay handler", e);
            stateCallback.reportError("Shantay handler error: " + e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean canExecute() {
        return taskConfig != null && taskConfig.isRequiresDesertGear();
    }
    
    @Override
    public String getHandlerName() {
        return "ShantayHandler";
    }
    
    /**
     * Calculates what items need to be purchased from Shantay
     */
    private boolean calculatePurchaseRequirements() {
        needsShantayPass = false;
        waterskinsToPurchase = 0;
        
        // Check if we need Shantay pass (only if not already in desert)
        if (!isPlayerInDesert()) {
            if (!Rs2Inventory.hasItem("Shantay pass")) {
                logError("Need to purchase Shantay pass");
                needsShantayPass = true;
            }
        }
        
        // Check waterskin requirements
        int currentWaterskins = getWaterskinCount();
        if (currentWaterskins < 3) {
            waterskinsToPurchase = 3 - currentWaterskins;
            logError("Need to purchase " + waterskinsToPurchase + " waterskins");
        }
        
        return needsShantayPass || waterskinsToPurchase > 0;
    }
    
    /**
     * Navigates to Shantay's location
     */
    private boolean navigateToShantay() {
        logError("Navigating to Shantay at: " + SHANTAY_LOCATION);
        
        stateCallback.updateState(SlayerBotState.BANKING, "Walking to Shantay");
        
        if (!Rs2Walker.walkTo(SHANTAY_LOCATION)) {
            logError("Failed to walk to Shantay at: " + SHANTAY_LOCATION);
            return false;
        }
        
        // Wait for arrival with timeout
        boolean arrived = sleepUntil(() -> {
            WorldPoint playerLocation = Rs2Player.getWorldLocation();
            return playerLocation != null && playerLocation.distanceTo(SHANTAY_LOCATION) <= 3;
        }, 15000);
        
        if (!arrived) {
            logError("Failed to reach Shantay within reasonable time");
            return false;
        }
        
        logError("Successfully reached Shantay");
        return true;
    }
    
    /**
     * Opens trade interface with Shantay
     */
    private boolean openTradeWithShantay() {
        logError("Opening trade with Shantay");
        
        stateCallback.updateState(SlayerBotState.BANKING, "Trading with Shantay");
        
        // Find and interact with Shantay
        if (!Rs2Npc.interact(SHANTAY_NPC_ID, "Trade")) {
            // Try alternative approach - by name
            if (!Rs2Npc.interact("Shantay", "Trade")) {
                logError("Failed to find or interact with Shantay NPC");
                return false;
            }
        }
        
        // Wait for shop interface to open
        boolean shopOpened = sleepUntil(() -> Rs2Shop.isOpen(), 8000);
        
        if (!shopOpened) {
            logError("Shop interface did not open when trading with Shantay");
            return false;
        }
        
        logError("Successfully opened trade with Shantay");
        sleep(1000); // Give interface time to fully load
        return true;
    }
    
    /**
     * Purchases the required desert items from Shantay
     */
    private boolean purchaseDesertItems() {
        logError("Purchasing desert items from Shantay");
        
        boolean overallSuccess = true;
        
        // Purchase Shantay pass if needed
        if (needsShantayPass) {
            stateCallback.updateState(SlayerBotState.BANKING, "Buying Shantay pass");
            logError("Purchasing Shantay pass from Shantay");
            
            try {
                if (Rs2Shop.buyItem("Shantay pass", "1")) {
                    logError("Successfully purchased Shantay pass");
                    sleep(1500);
                } else {
                    logError("Failed to purchase Shantay pass");
                    overallSuccess = false;
                }
            } catch (Exception e) {
                logError("Exception while purchasing Shantay pass: " + e.getMessage());
                overallSuccess = false;
            }
        }
        
        // Purchase waterskins if needed
        if (waterskinsToPurchase > 0) {
            stateCallback.updateState(SlayerBotState.BANKING, "Buying waterskins");
            logError("Purchasing " + waterskinsToPurchase + " waterskins from Shantay");
            
            try {
                // Use optimal buying for multiple waterskins
                Rs2Shop.buyItemOptimally("Waterskin(4)", waterskinsToPurchase);
                logError("Successfully purchased " + waterskinsToPurchase + " waterskins");
                sleep(1500);
            } catch (Exception e) {
                logError("Exception while purchasing waterskins: " + e.getMessage());
                overallSuccess = false;
            }
        }
        
        return overallSuccess;
    }
    
    /**
     * Checks if player is currently in desert region
     */
    private boolean isPlayerInDesert() {
        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        if (playerLocation == null) {
            return false;
        }
        
        int x = playerLocation.getX();
        int y = playerLocation.getY();
        
        // Kharidian Desert region bounds - south of Shantay Pass
        return x >= 3200 && x <= 3520 && y >= 2880 && y <= 3100;
    }
    
    /**
     * Gets count of all waterskins in inventory
     */
    private int getWaterskinCount() {
        return Rs2Inventory.count("Waterskin(4)") +
               Rs2Inventory.count("Waterskin(3)") +
               Rs2Inventory.count("Waterskin(2)") +
               Rs2Inventory.count("Waterskin(1)");
    }
    
    /**
     * Calculates the total cost of required desert items
     */
    public int calculatePurchaseCost() {
        int totalCost = 0;
        
        // Check if we need Shantay pass (only if not already in desert)
        if (!isPlayerInDesert() && !Rs2Inventory.hasItem("Shantay pass")) {
            totalCost += 5; // Shantay pass costs 5gp
        }
        
        // Check waterskin requirements
        int currentWaterskins = getWaterskinCount();
        if (currentWaterskins < 3) {
            int waterskinsToBuy = 3 - currentWaterskins;
            totalCost += (30 * waterskinsToBuy); // Each waterskin costs 30gp
        }
        
        return totalCost;
    }
    
    /**
     * Checks if we need to purchase anything from Shantay
     */
    public boolean needsToPurchase() {
        if (!taskConfig.isRequiresDesertGear()) {
            return false;
        }
        
        // Check Shantay pass requirement
        if (!isPlayerInDesert() && !Rs2Inventory.hasItem("Shantay pass")) {
            return true;
        }
        
        // Check waterskin requirement
        int currentWaterskins = getWaterskinCount();
        return currentWaterskins < 3;
    }
}

