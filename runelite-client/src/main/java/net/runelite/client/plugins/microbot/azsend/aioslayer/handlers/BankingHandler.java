package net.runelite.client.plugins.microbot.azsend.aioslayer.handlers;

import net.runelite.client.plugins.microbot.azsend.aioslayer.AioSlayerConfig;
import net.runelite.client.plugins.microbot.azsend.aioslayer.enums.SlayerBotState;
import net.runelite.client.plugins.microbot.azsend.aioslayer.models.TaskConfiguration;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.bank.enums.BankLocation;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.misc.Rs2Food;
import net.runelite.client.plugins.microbot.inventorysetups.InventorySetup;
import net.runelite.client.plugins.microbot.inventorysetups.InventorySetupsItem;
import net.runelite.client.plugins.microbot.inventorysetups.InventorySetupsStackCompareID;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.Microbot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles banking operations for slayer tasks
 */
public class BankingHandler extends BaseTaskHandler {
    
    private InventorySetupHandler inventoryHandler;
    private EquipmentHandler equipmentHandler;
    
    public BankingHandler(AioSlayerConfig config, TaskConfiguration taskConfig, StateCallback stateCallback) {
        super(config, taskConfig, stateCallback);
        this.inventoryHandler = new InventorySetupHandler(config, taskConfig, stateCallback);
        this.equipmentHandler = new EquipmentHandler(config, taskConfig, stateCallback);
    }
    
    @Override
    public boolean execute() {
        logInfo("Starting banking process for task: " + taskConfig.getTaskName());
        
        try {
            stateCallback.updateState(SlayerBotState.BANKING, "Walking to bank");
            
            // Walk to bank
            if (!Rs2Bank.walkToBank()) {
                logError("Failed to reach bank");
                return false;
            }
            
            stateCallback.updateState(SlayerBotState.BANKING, "Opening bank");
            
            // Open bank
            if (!Rs2Bank.openBank()) {
                logError("Failed to open bank");
                return false;
            }
            
            sleep(1200);
            
            stateCallback.updateState(SlayerBotState.BANKING, "Depositing items");
            
            // Deposit current inventory (except certain items we want to keep)
            depositCurrentItems();
            
            stateCallback.updateState(SlayerBotState.BANKING, "Restocking supplies");
            
            // Restock food and potions
            restockSupplies();
            
            // Setup inventory for the task
            if (!inventoryHandler.execute()) {
                logInfo("Inventory handler completed basic validation");
                // Don't fail immediately, we might have manually restocked above
            }
            
            stateCallback.updateState(SlayerBotState.BANKING, "Checking equipment");
            
            // Handle equipment requirements
            if (!equipmentHandler.execute()) {
                logError("Failed to setup equipment");
                Rs2Bank.closeBank();
                return false;
            }
            
            stateCallback.updateState(SlayerBotState.BANKING, "Finalizing setup");
            
            // Final validation
            if (!validateSetup()) {
                logError("Setup validation failed");
                Rs2Bank.closeBank();
                return false;
            }
            
            // Close bank
            Rs2Bank.closeBank();
            sleep(600);
            
            logInfo("Banking completed successfully");
            return true;
            
        } catch (Exception e) {
            logError("Error in banking handler", e);
            stateCallback.reportError("Banking error: " + e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean canExecute() {
        return taskConfig != null;
    }
    
    @Override
    public String getHandlerName() {
        return "BankingHandler";
    }
    
    // /**
    //  * Walks to the nearest suitable bank
    //  */
    // private boolean walkToBank() {

    //     CompletableFuture.supplyAsync(Rs2Bank::getNearestBank)
    //         .thenAccept(nearestBank -> {
    //             if (nearestBank != null)
    //             {
    //                 startWalking(nearestBank.getWorldPoint());
    //             }
    //         })
    //         .exceptionally(ex -> {
    //             Microbot.log("Error while finding the nearest bank: " + ex.getMessage());
    //             return false;
    //         });


    //     // // Find the closest bank first (before checking if we're near one)
    //     // BankLocation closestBank = Rs2Bank.getNearestBank();
    //     // if (closestBank == null) {
    //     //     logError("No bank location found");
    //     //     return false;
    //     // }
        
    //     // // Check if we're already at the specific bank we found
    //     // if (Rs2Bank.isNearBank(closestBank, 10)) {
    //     //     logInfo("Already near bank: " + closestBank.name());
    //     //     return true;
    //     // }
        
    //     // logInfo("Walking to bank: " + closestBank.name() + " at " + closestBank.getWorldPoint());
        
    //     // // Walk to the bank
    //     // if (!Rs2Walker.walkTo(closestBank.getWorldPoint())) {
    //     //     logError("Failed to walk to bank location");
    //     //     return false;
    //     // }
        
    //     // // Wait until we arrive
    //     // int attempts = 0;
    //     // while (!Rs2Bank.isNearBank(closestBank, 10) && attempts < 20) {
    //     //     sleep(1000);
    //     //     attempts++;
    //     // }
        
    //     // boolean arrived = Rs2Bank.isNearBank(closestBank, 10);
    //     // if (arrived) {
    //     //     logInfo("Successfully arrived at bank: " + closestBank.name());
    //     // } else {
    //     //     logError("Failed to arrive at bank within timeout");
    //     // }
        
    //     return true;
    // }
    
    /**
     * Deposits current inventory items (except items we want to keep)
     */
    private void depositCurrentItems() {
        // Items to keep (e.g., house teleports, useful items, currently equipped gear)
        String[] itemsToKeep = {
            "House teleport",
            "Teleport to house",
            "Games necklace",
            "Combat bracelet",
            "Skills necklace",
            "Ring of dueling"
        };
        
        // Only deposit inventory items, NOT equipped gear
        logInfo("Depositing inventory items except essential teleports");
        Rs2Bank.depositAllExcept(itemsToKeep);
        sleep(1200);
        
        // DO NOT deposit equipment during regular banking for slayer tasks
        // Equipment should only be managed by the EquipmentHandler when needed
        logInfo("Keeping all equipped gear - only depositing inventory");
    }
    
    /**
     * Deposits unnecessary equipment
     * NOTE: This method is disabled for slayer tasks to prevent depositing combat gear
     */
    private void depositUnnecessaryEquipment() {
        // For slayer tasks, we want to keep all combat equipment equipped
        // Only deposit equipment if we need to switch to a completely different setup
        logInfo("Skipping equipment deposit - keeping combat gear equipped for slayer task");
        
        // If we ever need to deposit specific equipment in the future, we can do it here
        // with more targeted logic instead of Rs2Bank.depositEquipment() which deposits everything
    }
    
    /**
     * Restocks using InventorySetup integration and configured food type
     */
    private void restockSupplies() {
        logInfo("Restocking using InventorySetup and configured settings");
        
        // Get the InventorySetup for this task
        InventorySetup inventorySetup = getInventorySetupForTask();
        
        if (inventorySetup != null) {
            logInfo("Using InventorySetup: " + inventorySetup.getName());
            
            // Use Rs2InventorySetup to handle equipment and inventory
            setupEquipmentWithOverrides(inventorySetup);
            setupInventoryWithOverrides(inventorySetup);
        } else {
            logInfo("No InventorySetup configured, using basic restocking");
            // Fallback to basic restocking
            restockConfiguredFood();
            
            // Restock potions if needed
            if (taskConfig.isRequiresPrayerPotions()) {
                restockPrayerPotions();
            }
        }
        
        // Always check for finishing blow items regardless of inventory setup
        if (taskConfig.isRequiresSpecialKill()) {
            restockFinishingBlowItems();
        }
    }
    
    /**
     * Gets the InventorySetup configured for the current task
     */
    private InventorySetup getInventorySetupForTask() {
        if (taskConfig == null || taskConfig.getTaskName() == null) {
            return null;
        }
        
        String taskName = taskConfig.getTaskName().toLowerCase().trim();
        
        // Map task names to their config methods
        switch (taskName) {
            case "wolves":
                return config.wolvesSetup();
            case "werewolves":
                return config.werewolvesSetup();
            case "banshees":
                return config.bansheesSetup();
            // Add more cases as needed
            default:
                logInfo("No InventorySetup method found for task: " + taskName);
                return null;
        }
    }
    
    /**
     * Sets up equipment using InventorySetup with protective item overrides
     */
    private void setupEquipmentWithOverrides(InventorySetup inventorySetup) {
        logInfo("Setting up equipment with protective overrides");
        
        // Create a copy of the equipment to modify
        List<InventorySetupsItem> originalEquipment = inventorySetup.getEquipment();
        List<InventorySetupsItem> modifiedEquipment = new ArrayList<>(originalEquipment);
        
        // Override equipment slots with task-specific protective equipment
        Integer protectiveItemId = taskConfig.getProtectiveEquipmentId();
        if (protectiveItemId != null) {
            logInfo("Overriding equipment slot for protective item: " + protectiveItemId);
            
            // Determine which equipment slot to override based on the protective item
            int slotToOverride = getEquipmentSlotForProtectiveItem(protectiveItemId);
            if (slotToOverride >= 0 && slotToOverride < modifiedEquipment.size()) {
                // Create new protective item for that slot
                InventorySetupsItem protectiveItem = createInventorySetupsItem(protectiveItemId, 1, slotToOverride);
                modifiedEquipment.set(slotToOverride, protectiveItem);
                logInfo("Override equipment slot " + slotToOverride + " with protective item");
            }
        }
        
        // Withdraw and equip items using modified setup
        withdrawAndEquipItems(modifiedEquipment);
    }
    
    /**
     * Sets up inventory using InventorySetup but respects configured food type
     */
    private void setupInventoryWithOverrides(InventorySetup inventorySetup) {
        logInfo("Setting up inventory with food override");
        
        List<InventorySetupsItem> inventoryItems = inventorySetup.getInventory();
        
        // Consolidate items by ID and calculate total quantities needed
        Map<Integer, ConsolidatedItem> consolidatedItems = consolidateInventoryItems(inventoryItems);
        
        // Withdraw consolidated items
        for (ConsolidatedItem consolidatedItem : consolidatedItems.values()) {
            // Check if this item is food and we have a configured food override
            if (isFoodItem(consolidatedItem.sampleItem) && hasConfiguredFood()) {
                // Replace with configured food type
                logInfo("Withdrawing configured food instead of: " + consolidatedItem.sampleItem.getName() + " (total needed: " + consolidatedItem.totalQuantity + ")");
                withdrawConfiguredFoodInstead(consolidatedItem.sampleItem, consolidatedItem.totalQuantity);
            } else {
                // Withdraw the original item with consolidated quantity
                logInfo("Withdrawing consolidated item: " + consolidatedItem.sampleItem.getName() + " x" + consolidatedItem.totalQuantity);
                withdrawInventoryItemConsolidated(consolidatedItem.sampleItem, consolidatedItem.totalQuantity);
            }
        }
    }
    
    /**
     * Consolidates inventory items by ID, summing up quantities
     */
    private Map<Integer, ConsolidatedItem> consolidateInventoryItems(List<InventorySetupsItem> inventoryItems) {
        Map<Integer, ConsolidatedItem> consolidated = new HashMap<>();
        
        for (InventorySetupsItem item : inventoryItems) {
            if (InventorySetupsItem.itemIsDummy(item)) continue;
            
            int itemId = item.getId();
            if (consolidated.containsKey(itemId)) {
                // Add to existing quantity
                ConsolidatedItem existing = consolidated.get(itemId);
                existing.totalQuantity += item.getQuantity();
            } else {
                // Create new consolidated item
                consolidated.put(itemId, new ConsolidatedItem(item, item.getQuantity()));
            }
        }
        
        return consolidated;
    }
    
    /**
     * Helper class to store consolidated item information
     */
    private static class ConsolidatedItem {
        final InventorySetupsItem sampleItem; // Representative item for properties
        int totalQuantity;
        
        ConsolidatedItem(InventorySetupsItem sampleItem, int totalQuantity) {
            this.sampleItem = sampleItem;
            this.totalQuantity = totalQuantity;
        }
    }
    
    /**
     * Gets the equipment slot index for a protective item
     */
    private int getEquipmentSlotForProtectiveItem(int itemId) {
        // Map item IDs to equipment slots (EquipmentInventorySlot ordinal values)
        switch (itemId) {
            case 4168: // Slayer helmet
            case 11866: // Slayer helmet (i)
            case 4166: // Earmuffs  
            case 4164: // Facemask
            case 4167: // Nose peg
            case 4513: // Spiny helmet
            case 23056: // Reinforced goggles
                return 0; // HEAD slot
            case 4087: // Mirror shield  
            case 4206: // Witchwood icon
                return 5; // SHIELD slot
            case 4799: // Insulated boots
                return 10; // BOOTS slot
            case 4675: // Slayer gloves
                return 9; // GLOVES slot
            default:
                return -1; // Unknown item
        }
    }
    
    /**
     * Creates an InventorySetupsItem for a given item ID and quantity
     */
    private InventorySetupsItem createInventorySetupsItem(int itemId, int quantity, int slot) {
        String itemName = Microbot.getItemManager().getItemComposition(itemId).getName();
        return new InventorySetupsItem(itemId, itemName, quantity, false, InventorySetupsStackCompareID.None, false, slot);
    }
    
    /**
     * Withdraws and equips equipment items
     */
    private void withdrawAndEquipItems(List<InventorySetupsItem> equipment) {
        for (InventorySetupsItem item : equipment) {

            if (InventorySetupsItem.itemIsDummy(item)) continue;
            
            // Check if already equipped (for non-ammunition items)
            if (!isAmmunition(item) && Rs2Equipment.isWearing(item.getId())) {
                logError("Already wearing: " + item.getName());
                continue;
            }
            
            // Check if we need to withdraw the item
            boolean needsWithdrawal = false;
            int amountToWithdraw = 1; // Default for regular equipment
            
            // For ammunition (arrows, bolts, etc.), check quantity and withdraw what's needed
            if (isAmmunition(item)) {
                int currentInInventory = Rs2Inventory.count(item.getId());
                int currentEquipped = 0;
                Rs2ItemModel equippedItem = Rs2Equipment.get(item.getId());
                if (equippedItem != null) {
                    currentEquipped = equippedItem.getQuantity();
                }
                int currentAmmo = currentInInventory + currentEquipped;
                
                if (currentAmmo < item.getQuantity()) {
                    needsWithdrawal = true;
                    amountToWithdraw = item.getQuantity() - currentAmmo;
                    logError("Need " + amountToWithdraw + " more " + item.getName() + " (have " + currentAmmo + " total: " + currentInInventory + " in inventory + " + currentEquipped + " equipped, need " + item.getQuantity() + ")");
                }
            } else {
                // Regular equipment - just need 1
                if (!Rs2Inventory.hasItem(item.getId())) {
                    needsWithdrawal = true;
                    amountToWithdraw = 1;
                }
            }
            
            if (needsWithdrawal) {
                // Withdraw from bank
                if (Rs2Bank.hasItem(item.getId())) {
                    logError("Withdrawing equipment: " + item.getName() + " x" + amountToWithdraw);
                    if (amountToWithdraw == 1) {
                        Rs2Bank.withdrawOne(item.getId());
                    } else {
                        Rs2Bank.withdrawX(item.getId(), amountToWithdraw);
                    }
                    final int finalAmountToWithdraw = amountToWithdraw;
                    sleepUntil(() -> Rs2Inventory.count(item.getId()) >= finalAmountToWithdraw, 3000);
                } else {
                    logError("Missing equipment in bank: " + item.getName());
                    continue;
                }
            }

            if (isAmmunition(item)) {
                logError("Equipping ammunition: " + item.getName());
                Rs2Inventory.interact(item.getId(), "Wield");
                sleep(600);
            } else if (!Rs2Equipment.isWearing(item.getId())) {
                logError("Equipping: " + item.getName());
                Rs2Inventory.equip(item.getId());
                sleep(600);
            }
        }
    }
    
    /**
     * Withdraws an inventory item from the setup
     */
    private void withdrawInventoryItem(InventorySetupsItem item) {
        if (Rs2Inventory.hasItemAmount(item.getId(), item.getQuantity())) {
            return; // Already have enough
        }
        
        int needed = item.getQuantity() - Rs2Inventory.count(item.getId());
        if (needed <= 0) return;
        
        if (Rs2Bank.hasItem(item.getId())) {
            logInfo("Withdrawing: " + item.getName() + " x" + needed);
            Rs2Bank.withdrawX(item.getId(), needed);
            sleep(800);
        } else {
            logError("Missing item in bank: " + item.getName());
        }
    }
    
    /**
     * Withdraws an inventory item with consolidated quantity
     */
    private void withdrawInventoryItemConsolidated(InventorySetupsItem sampleItem, int totalQuantityNeeded) {
        int currentCount = Rs2Inventory.count(sampleItem.getId());
        if (currentCount >= totalQuantityNeeded) {
            logInfo("Already have enough " + sampleItem.getName() + " (" + currentCount + "/" + totalQuantityNeeded + ")");
            return; // Already have enough
        }
        
        int needed = totalQuantityNeeded - currentCount;
        if (needed <= 0) return;
        
        if (Rs2Bank.hasItem(sampleItem.getId())) {
            logInfo("Withdrawing consolidated: " + sampleItem.getName() + " x" + needed + " (have " + currentCount + ", need " + totalQuantityNeeded + ")");
            Rs2Bank.withdrawX(sampleItem.getId(), needed);
            sleep(800);
        } else {
            logError("Missing consolidated item in bank: " + sampleItem.getName());
        }
    }
    
    /**
     * Checks if an item is food
     */
    private boolean isFoodItem(InventorySetupsItem item) {
        // Check if item ID is in Rs2Food enum
        return Rs2Food.getIds().contains(item.getId());
    }
    
    /**
     * Checks if an item is ammunition (arrows, bolts, etc.)
     */
    private boolean isAmmunition(InventorySetupsItem item) {
        String itemName = item.getName().toLowerCase();
        
        // Check for common ammunition types
        return itemName.contains("arrow") || 
               itemName.contains("bolt") || 
               itemName.contains("dart") ||
               itemName.contains("knife") ||
               itemName.contains("javelin") ||
               itemName.contains("throwing") ||
               itemName.contains("chinchompa");
    }
    
    /**
     * Checks if we have a configured food type
     */
    private boolean hasConfiguredFood() {
        String configuredFood = config.foodType();
        return configuredFood != null && !configuredFood.trim().isEmpty();
    }
    
    /**
     * Withdraws configured food instead of the food from inventory setup
     */
    private void withdrawConfiguredFoodInstead(InventorySetupsItem originalFoodItem) {
        withdrawConfiguredFoodInstead(originalFoodItem, originalFoodItem.getQuantity());
    }
    
    /**
     * Withdraws configured food instead of the food from inventory setup with specified total quantity
     */
    private void withdrawConfiguredFoodInstead(InventorySetupsItem originalFoodItem, int totalQuantityNeeded) {
        String configuredFood = config.foodType();
        if (configuredFood == null || configuredFood.trim().isEmpty()) {
            // Fallback to original item
            withdrawInventoryItemConsolidated(originalFoodItem, totalQuantityNeeded);
            return;
        }
        
        int currentFoodCount = Rs2Inventory.count(item -> 
            item.getName().toLowerCase().contains(configuredFood.toLowerCase())
        );
        
        int needed = totalQuantityNeeded - currentFoodCount;
        if (needed <= 0) {
            logInfo("Already have enough configured food: " + configuredFood + " (" + currentFoodCount + "/" + totalQuantityNeeded + ")");
            return;
        }
        
        if (Rs2Bank.hasItem(configuredFood)) {
            logInfo("Withdrawing configured food: " + configuredFood + " x" + needed + " (have " + currentFoodCount + ", need " + totalQuantityNeeded + ")");
            Rs2Bank.withdrawX(configuredFood, needed);
            sleep(800);
        } else {
            logError("Configured food not found in bank: " + configuredFood + ", using original setup food");
            withdrawInventoryItemConsolidated(originalFoodItem, totalQuantityNeeded);
        }
    }
    
    /**
     * Restocks configured food type (fallback method)
     */
    private void restockConfiguredFood() {
        String configuredFood = config.foodType();
        if (configuredFood == null || configuredFood.trim().isEmpty()) {
            // Use default food hierarchy
            restockFood();
            return;
        }
        
        // Check current food count
        int currentFood = Rs2Inventory.count(item -> 
            item.getName().toLowerCase().contains(configuredFood.toLowerCase())
        );
        
        int targetFood = 12; // Target number of food items
        int needed = targetFood - currentFood;
        
        if (needed <= 0) {
            logInfo("Already have enough configured food: " + currentFood);
            return;
        }
        
        if (Rs2Bank.hasItem(configuredFood)) {
            logInfo("Withdrawing configured food: " + configuredFood + " x" + needed);
            Rs2Bank.withdrawX(configuredFood, needed);
            sleep(1000);
        } else {
            logError("Configured food not found in bank: " + configuredFood + ", trying alternatives");
            restockFood(); // Fallback to original method
        }
    }
    
    /**
     * Restocks food items (fallback method)
     */
    private void restockFood() {
        // Check current food count
        int currentFood = Rs2Inventory.count(item -> 
            item.getName().toLowerCase().contains("shark") ||
            item.getName().toLowerCase().contains("monkfish") ||
            item.getName().toLowerCase().contains("lobster") ||
            item.getName().toLowerCase().contains("tuna") ||
            item.getName().toLowerCase().contains("salmon") ||
            item.getName().toLowerCase().contains("karambwan")
        );
        
        int targetFood = 12; // Target number of food items
        int needed = targetFood - currentFood;
        
        if (needed <= 0) {
            logInfo("Already have enough food: " + currentFood);
            return;
        }
        
        logInfo("Need " + needed + " more food items");
        
        // Try to withdraw different types of food in order of preference
        String[] foodTypes = {"Shark", "Monkfish", "Lobster", "Tuna", "Salmon"};
        
        for (String foodType : foodTypes) {
            if (Rs2Bank.hasItem(foodType)) {
                logInfo("Withdrawing " + needed + " " + foodType);
                Rs2Bank.withdrawX(foodType, needed);
                sleep(1000);
                return;
            }
        }
        
        logError("No food found in bank!");
    }
    
    /**
     * Restocks prayer potions
     */
    private void restockPrayerPotions() {
        int currentPots = Rs2Inventory.count(item ->
            item.getName().toLowerCase().contains("prayer potion") ||
            item.getName().toLowerCase().contains("super restore")
        );
        
        if (currentPots >= 2) {
            logInfo("Already have enough prayer potions: " + currentPots);
            return;
        }
        
        int needed = 3 - currentPots;
        logInfo("Need " + needed + " prayer potions");
        
        if (Rs2Bank.hasItem("Prayer potion(4)")) {
            Rs2Bank.withdrawX("Prayer potion(4)", needed);
        } else if (Rs2Bank.hasItem("Super restore(4)")) {
            Rs2Bank.withdrawX("Super restore(4)", needed);
        }
        sleep(1000);
    }
    
    /**
     * Restocks finishing blow items
     */
    private void restockFinishingBlowItems() {
        if (taskConfig.getFinishingBlowItems() == null || taskConfig.getFinishingBlowItems().isEmpty()) {
            return;
        }
        
        int itemId = taskConfig.getFinishingBlowItems().get(0);
        String itemName = getItemName(itemId);
        
        if (itemName == null) {
            logError("Could not determine name for finishing blow item: " + itemId);
            return;
        }
        
        int currentCount = Rs2Inventory.count(itemId);
        if (currentCount >= 10) {
            logInfo("Already have enough finishing blow items: " + currentCount);
            return;
        }
        
        int needed = 20 - currentCount;
        logInfo("Withdrawing " + needed + " " + itemName);
        Rs2Bank.withdrawX(itemId, needed);
        sleep(1000);
    }
    
    /**
     * Gets item name from ID - basic mapping for common finishing blow items
     */
    private String getItemName(int itemId) {
        // This is a simplified mapping - in a real implementation you'd use the item manager
        switch (itemId) {
            case 4021: return "Rock hammer";
            case 4161: return "Bag of salt";
            case 6696: return "Ice cooler";
            case 7421: return "Fungicide spray 10";
            default: return null;
        }
    }

    /**
     * Validates that our setup is correct for the task
     */
    private boolean validateSetup() {
        logInfo("Validating inventory and equipment setup");
        
        // Check inventory setup
        if (!inventoryHandler.isInventorySetupCorrect()) {
            logError("Inventory setup validation failed");
            return false;
        }
        
        // Check that we have required protective equipment
        Integer protectiveItemId = taskConfig.getProtectiveEquipmentId();
        if (protectiveItemId != null && !equipmentHandler.hasRequiredProtection()) {
            logError("Missing required protective equipment");
            return false;
        }
        
        // Check finishing blow items if needed
        if (taskConfig.isRequiresSpecialKill()) {
            FinishingBlowHandler finishingHandler = new FinishingBlowHandler(config, taskConfig, stateCallback);
            if (!finishingHandler.hasEnoughFinishingBlowItems()) {
                logError("Insufficient finishing blow items");
                return false;
            }
        }
        
        // Check for minimum food/potions
        if (!hasMinimumSupplies()) {
            logError("Insufficient food or potions");
            return false;
        }
        
        logInfo("Setup validation passed");
        return true;
    }
    
    /**
     * Checks if we have minimum supplies for the task
     */
    private boolean hasMinimumSupplies() {
        // Check for food
        int foodCount = Rs2Inventory.count(item -> 
            item.getName().toLowerCase().contains("shark") ||
            item.getName().toLowerCase().contains("monkfish") ||
            item.getName().toLowerCase().contains("lobster") ||
            item.getName().toLowerCase().contains("tuna") ||
            item.getName().toLowerCase().contains("salmon") ||
            item.getName().toLowerCase().contains("karambwan")
        );
        
        if (foodCount < 5) {
            logError("Insufficient food items: " + foodCount);
            return false;
        }
        
        // Check for potions if required
        if (taskConfig.isRequiresPrayerPotions()) {
            int prayerPotCount = Rs2Inventory.count(item ->
                item.getName().toLowerCase().contains("prayer potion") ||
                item.getName().toLowerCase().contains("super restore")
            );
            
            if (prayerPotCount < 2) {
                logError("Insufficient prayer potions: " + prayerPotCount);
                return false;
            }
        }
        
        // Check for combat potions
        int combatPotCount = Rs2Inventory.count(item ->
            item.getName().toLowerCase().contains("combat potion") ||
            item.getName().toLowerCase().contains("super attack") ||
            item.getName().toLowerCase().contains("super strength") ||
            item.getName().toLowerCase().contains("super defence") ||
            item.getName().toLowerCase().contains("ranging potion") ||
            item.getName().toLowerCase().contains("magic potion")
        );
        
        if (combatPotCount < 1 && Rs2Player.getCombatLevel() < 70) {
            logInfo("No combat potions found - acceptable for higher level players");
        }
        
        return true;
    }
    
    /**
     * Checks if we need to bank before starting the task
     */
    public boolean needsToBankBefore() {
        // Always bank if specifically required by task
        if (taskConfig.isRequiresBankingBefore()) {
            return true;
        }
        
        // Check if our current setup is correct
        if (!inventoryHandler.isInventorySetupCorrect()) {
            logInfo("Banking needed - incorrect inventory setup");
            return true;
        }
        
        // Check if we have required protective equipment
        if (!equipmentHandler.hasRequiredProtection()) {
            logInfo("Banking needed - missing protective equipment");
            return true;
        }
        
        // Check supplies
        if (!hasMinimumSupplies()) {
            logInfo("Banking needed - insufficient supplies");
            return true;
        }
        
        return false;
    }
    
    /**
     * Quick banking for restocking during a task
     */
    public boolean quickRestock() {
        logInfo("Performing quick restock");
        
        if (!Rs2Bank.walkToBank()) {
            return false;
        }
        
        if (!Rs2Bank.openBank()) {
            return false;
        }
        
        // Quick restock of food and potions
        restockFood();
        restockPrayerPotions();
        restockFinishingBlowItems();
        
        Rs2Bank.closeBank();
        return true;
    }
}
