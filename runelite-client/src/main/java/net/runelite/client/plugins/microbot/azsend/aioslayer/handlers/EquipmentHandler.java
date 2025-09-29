package net.runelite.client.plugins.microbot.azsend.aioslayer.handlers;

import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.ItemID;
import net.runelite.api.NPC;
import net.runelite.client.plugins.microbot.azsend.aioslayer.AioSlayerConfig;
import net.runelite.client.plugins.microbot.azsend.aioslayer.enums.SlayerBotState;
import net.runelite.client.plugins.microbot.azsend.aioslayer.enums.SlayerShopEnum;
import net.runelite.client.plugins.microbot.azsend.aioslayer.models.TaskConfiguration;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.shop.Rs2Shop;
import net.runelite.client.plugins.microbot.util.skills.slayer.enums.SlayerMaster;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import java.util.Arrays;
import java.util.List;

/**
 * Handles equipment management for slayer tasks
 */
public class EquipmentHandler extends BaseTaskHandler {
    
    private static final List<Integer> SLAYER_HELMETS = Arrays.asList(
        ItemID.SLAYER_HELMET,
        ItemID.SLAYER_HELMET_I,
        ItemID.BLACK_SLAYER_HELMET,
        ItemID.BLACK_SLAYER_HELMET_I,
        ItemID.GREEN_SLAYER_HELMET,
        ItemID.GREEN_SLAYER_HELMET_I,
        ItemID.RED_SLAYER_HELMET,
        ItemID.RED_SLAYER_HELMET_I,
        ItemID.PURPLE_SLAYER_HELMET,
        ItemID.PURPLE_SLAYER_HELMET_I,
        ItemID.TURQUOISE_SLAYER_HELMET,
        ItemID.TURQUOISE_SLAYER_HELMET_I,
        ItemID.HYDRA_SLAYER_HELMET,
        ItemID.HYDRA_SLAYER_HELMET_I
    );
    
    public EquipmentHandler(AioSlayerConfig config, TaskConfiguration taskConfig, StateCallback stateCallback) {
        super(config, taskConfig, stateCallback);
    }
    
    @Override
    public boolean execute() {
        logInfo("Checking and equipping required protective equipment");
        
        try {
            // Check if we need protective equipment for this task
            Integer protectiveItemId = taskConfig.getProtectiveEquipmentId();
            if (protectiveItemId == null) {
                logInfo("No protective equipment required for this task");
                return true;
            }
            
            // Check if we already have the required protection equipped
            if (hasRequiredProtection()) {
                logInfo("Required protective equipment already equipped");
                return true;
            }
            
            // Try to equip protective equipment from inventory
            if (equipProtectiveGear()) {
                return true;
            }
            
            // If we don't have it, try to get it from bank
            if (Rs2Bank.isOpen() || Rs2Bank.openBank()) {
                if (withdrawProtectiveGear()) {
                    Rs2Bank.closeBank();
                    sleep(600);
                    return equipProtectiveGear();
                }
                Rs2Bank.closeBank();
            }
            
            // If we can't get it from bank, try to buy it from slayer master
            return purchaseProtectiveGear();
            
        } catch (Exception e) {
            logError("Error in equipment handler", e);
            stateCallback.reportError("Equipment error: " + e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean canExecute() {
        return taskConfig != null && taskConfig.getProtectiveEquipmentId() != null;
    }
    
    @Override
    public String getHandlerName() {
        return "EquipmentHandler";
    }
    
    /**
     * Checks if we have the required protective equipment equipped
     */
    public boolean hasRequiredProtection() {
        Integer requiredId = taskConfig.getProtectiveEquipmentId();
        if (requiredId == null) return true;
        
        // Check for slayer helmet first (preferred protection)
        if (isSlayerHelmetProtection(requiredId) && hasSlayerHelmetEquipped()) {
            return true;
        }
        
        // Check for specific protective equipment
        return Rs2Equipment.isWearing(requiredId);
    }
    
    /**
     * Checks if we have any slayer helmet equipped
     */
    private boolean hasSlayerHelmetEquipped() {
        return SLAYER_HELMETS.stream().anyMatch(Rs2Equipment::isWearing);
    }
    
    /**
     * Checks if the required protection can be provided by slayer helmet
     */
    private boolean isSlayerHelmetProtection(int itemId) {
        return itemId == ItemID.FACEMASK || 
               itemId == ItemID.EARMUFFS || 
               itemId == ItemID.NOSE_PEG || 
               itemId == ItemID.SPINY_HELMET ||
               itemId == ItemID.REINFORCED_GOGGLES;
    }
    
    /**
     * Tries to equip protective gear from inventory
     */
    private boolean equipProtectiveGear() {
        Integer requiredId = taskConfig.getProtectiveEquipmentId();
        if (requiredId == null) return true;
        
        // Try to equip slayer helmet first if it provides the required protection
        if (isSlayerHelmetProtection(requiredId)) {
            for (Integer helmetId : SLAYER_HELMETS) {
                if (Rs2Inventory.hasItem(helmetId)) {
                    logInfo("Equipping slayer helmet: " + helmetId);
                    Rs2Inventory.equip(helmetId);
                    sleep(600);
                    return Rs2Equipment.isWearing(helmetId);
                }
            }
        }
        
        // Equip specific protective equipment
        if (Rs2Inventory.hasItem(requiredId)) {
            logInfo("Equipping protective equipment: " + requiredId);
            Rs2Inventory.equip(requiredId);
            sleep(600);
            return Rs2Equipment.isWearing(requiredId);
        }
        
        logInfo("Required protective equipment not found in inventory");
        return false;
    }
    
    /**
     * Tries to withdraw protective gear from bank
     */
    private boolean withdrawProtectiveGear() {
        Integer requiredId = taskConfig.getProtectiveEquipmentId();
        if (requiredId == null) return true;
        
        // Try to withdraw slayer helmet first
        if (isSlayerHelmetProtection(requiredId)) {
            for (Integer helmetId : SLAYER_HELMETS) {
                if (Rs2Bank.hasItem(helmetId)) {
                    logInfo("Withdrawing slayer helmet from bank");
                    Rs2Bank.withdrawOne(helmetId);
                    sleep(600);
                    return true;
                }
            }
        }
        
        // Withdraw specific protective equipment
        if (Rs2Bank.hasItem(requiredId)) {
            logInfo("Withdrawing protective equipment from bank: " + requiredId);
            Rs2Bank.withdrawOne(requiredId);
            sleep(600);
            return true;
        }
        
        logInfo("Required protective equipment not found in bank");
        return false;
    }
    
    /**
     * Attempts to purchase protective gear from slayer master
     */
    private boolean purchaseProtectiveGear() {
        Integer requiredId = taskConfig.getProtectiveEquipmentId();
        if (requiredId == null) return true;
        
        // Find the slayer shop item for our required equipment
        SlayerShopEnum shopItem = getShopItemForEquipment(requiredId);
        if (shopItem == null) {
            logError("Cannot purchase required equipment - not available in slayer shop");
            stateCallback.reportError("Required protective equipment not available for purchase", null);
            return false;
        }
        
        logInfo("Attempting to purchase " + shopItem.getItemName() + " from slayer master");
        
        // Get the configured slayer master or find the closest one
        SlayerMaster slayerMaster = getSlayerMaster();
        if (slayerMaster == null) {
            logError("No slayer master configured");
            return false;
        }
        
        // Walk to slayer master
        if (!Rs2Walker.walkTo(slayerMaster.getWorldPoint())) {
            logError("Failed to walk to slayer master");
            return false;
        }
        
        sleep(2000);
        
        // Find and interact with slayer master NPC
        NPC slayerNpc = Rs2Npc.getNpc(slayerMaster.getName());
        if (slayerNpc == null) {
            logError("Cannot find slayer master NPC: " + slayerMaster.getName());
            return false;
        }
        
        // Open shop
        if (!Rs2Shop.openShop(slayerMaster.getName())) {
            logError("Failed to open slayer shop");
            return false;
        }
        
        sleep(1200);
        
        // Purchase the item
        if (Rs2Shop.buyItem(shopItem.getItemName(), "1")) {
            logInfo("Successfully purchased " + shopItem.getItemName());
            Rs2Shop.closeShop();
            sleep(600);
            return equipProtectiveGear();
        } else {
            logError("Failed to purchase " + shopItem.getItemName());
            Rs2Shop.closeShop();
            return false;
        }
    }
    
    /**
     * Gets the slayer shop item for the required equipment
     */
    private SlayerShopEnum getShopItemForEquipment(int itemId) {
        switch (itemId) {
            case ItemID.FACEMASK:
                return SlayerShopEnum.FACEMASK;
            case ItemID.EARMUFFS:
                return SlayerShopEnum.EARMUFFS;
            case ItemID.NOSE_PEG:
                return SlayerShopEnum.NOSE_PEG;
            case ItemID.MIRROR_SHIELD:
                return SlayerShopEnum.MIRROR_SHIELD;
            case ItemID.WITCHWOOD_ICON:
                return SlayerShopEnum.WITCHWOOD_ICON;
            case ItemID.INSULATED_BOOTS:
                return SlayerShopEnum.INSULATED_BOOTS;
            case ItemID.SLAYER_GLOVES:
                return SlayerShopEnum.SLAYER_GLOVES;
            case ItemID.SPINY_HELMET:
                return SlayerShopEnum.SPINY_HELMET;
            case ItemID.ROCK_HAMMER:
                return SlayerShopEnum.ROCK_HAMMER;
            case ItemID.BAG_OF_SALT:
                return SlayerShopEnum.BAG_OF_SALT;
            case ItemID.ICE_COOLER:
                return SlayerShopEnum.ICE_COOLER;
            case ItemID.FUNGICIDE_SPRAY_10:
                return SlayerShopEnum.FUNGICIDE_SPRAY;
            case ItemID.FUNGICIDE:
                return SlayerShopEnum.FUNGICIDE;
            default:
                return null;
        }
    }
    
    /**
     * Gets the configured slayer master
     */
    private SlayerMaster getSlayerMaster() {
        // For now, return a default slayer master - this could be configurable
        try {
            return SlayerMaster.valueOf(config.slayerMaster().name());
        } catch (Exception e) {
            // Default to Nieve if config is invalid
            return SlayerMaster.NIEVE;
        }
    }
    
}
