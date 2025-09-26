package net.runelite.client.plugins.microbot.azsend.aioslayer;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.ItemComposition;
import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.azsend.aioslayer.enums.SlayerBotState;
import net.runelite.client.plugins.microbot.azsend.aioslayer.enums.SlayerTaskEnum;
import net.runelite.client.plugins.microbot.inventorysetups.InventorySetup;
import net.runelite.client.plugins.microbot.inventorysetups.MInventorySetupsPlugin;
import net.runelite.client.plugins.microbot.util.Global;
import net.runelite.client.plugins.microbot.util.Rs2InventorySetup;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.antiban.enums.Activity;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.bank.enums.BankLocation;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.shop.Rs2Shop;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.models.RS2Item;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2Cannon;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.npc.MonsterLocation;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcManager;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.skills.slayer.Rs2Slayer;
import net.runelite.client.plugins.microbot.util.skills.slayer.enums.SlayerTaskMonster;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class AioSlayerScript extends Script {
    
    public static SlayerBotState botState = SlayerBotState.INITIALIZING;
    public static String currentTask = "";
    public static int taskCount = 0;
    public static int tasksCompleted = 0;
    public static Instant startTime;
    
    private AioSlayerConfig config;
    private MonsterLocation currentTaskLocation;
    private SlayerTaskEnum currentTaskEnum;
    private boolean cannonPlaced = false;
    private WorldPoint cannonLocation;
    private Instant lastCombatTime;
    private Instant lastCombatAction;
    private static final int COMBAT_TIMEOUT_SECONDS = 15;

    private static final int SHANTAY = 4642;
    
    // Anti-ban NPC blacklisting system
    private Map<String, NpcBlacklistEntry> npcBlacklist = new HashMap<>();
    private static final int MAX_FAILED_ATTEMPTS = 2;
    private static final long BLACKLIST_CLEAR_TIME_MS = 30000; // 30 seconds
    private net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel lastAttemptedNpc = null;
    
    private static class NpcBlacklistEntry {
        final WorldPoint lastKnownPosition;
        final int failedAttempts;
        final long timestamp;
        
        NpcBlacklistEntry(WorldPoint position, int attempts) {
            this.lastKnownPosition = position;
            this.failedAttempts = attempts;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    // Anti-ban blacklist management methods
    private String getNpcKey(net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel npc) {
        return npc.getName() + "_" + npc.getWorldLocation().toString();
    }
    
    private void recordFailedAttack(net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel npc) {
        String npcKey = getNpcKey(npc);
        NpcBlacklistEntry existing = npcBlacklist.get(npcKey);
        
        if (existing != null) {
            // Increment failed attempts if still at same position
            if (existing.lastKnownPosition.equals(npc.getWorldLocation())) {
                npcBlacklist.put(npcKey, new NpcBlacklistEntry(npc.getWorldLocation(), existing.failedAttempts + 1));
                log.error("Increased failed attempts for {} at {} to {}", npc.getName(), npc.getWorldLocation(), existing.failedAttempts + 1);
            } else {
                // NPC moved, reset attempts
                npcBlacklist.put(npcKey, new NpcBlacklistEntry(npc.getWorldLocation(), 1));
                log.error("NPC {} moved from {} to {}, resetting failed attempts", npc.getName(), existing.lastKnownPosition, npc.getWorldLocation());
            }
        } else {
            // First failed attempt
            npcBlacklist.put(npcKey, new NpcBlacklistEntry(npc.getWorldLocation(), 1));
            log.error("Recording first failed attack for {} at {}", npc.getName(), npc.getWorldLocation());
        }
    }
    
    private boolean isNpcBlacklisted(net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel npc) {
        cleanupBlacklist(); // Remove old entries
        
        String npcKey = getNpcKey(npc);
        NpcBlacklistEntry entry = npcBlacklist.get(npcKey);
        
        if (entry != null) {
            // Check if NPC is still at same position and has exceeded max attempts
            if (entry.lastKnownPosition.equals(npc.getWorldLocation()) && entry.failedAttempts >= MAX_FAILED_ATTEMPTS) {
                return true;
            }
            // If NPC moved, remove from blacklist
            if (!entry.lastKnownPosition.equals(npc.getWorldLocation())) {
                npcBlacklist.remove(npcKey);
                log.error("Removing {} from blacklist - moved to new position", npc.getName());
            }
        }
        
        return false;
    }
    
    private void cleanupBlacklist() {
        long currentTime = System.currentTimeMillis();
        npcBlacklist.entrySet().removeIf(entry -> 
            currentTime - entry.getValue().timestamp > BLACKLIST_CLEAR_TIME_MS);
    }
    
    private void checkForFailedAttackMessages() {
        // Simple approach: Check if we attempted an attack recently and player hasn't moved into combat
        if (lastAttemptedNpc != null) {
            // Give it a short delay to see if attack succeeded
            Global.sleep(1500, 2000);
            
            // If we're not in combat and still have the attempted NPC, it probably failed
            if (!Rs2Combat.inCombat()) {
                log.error("Attack appears to have failed (not in combat after attempt), blacklisting {} at {}", 
                    lastAttemptedNpc.getName(), lastAttemptedNpc.getWorldLocation());
                recordFailedAttack(lastAttemptedNpc);
            }
            
            // Clear the attempted NPC regardless
            lastAttemptedNpc = null;
        }
    }
    
    /**
     * Finds a reachable alternative destination from all coordinates in the monster location
     */
    private WorldPoint findReachableAlternative(MonsterLocation location) {
        if (location == null || location.getCoords() == null) {
            return null;
        }
        
        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        
        // Sort coordinates by distance to player and try each one
        List<WorldPoint> sortedCoords = location.getCoords().stream()
            .sorted((a, b) -> Integer.compare(
                playerLocation.distanceTo(a), 
                playerLocation.distanceTo(b)))
            .collect(java.util.stream.Collectors.toList());
        
        for (WorldPoint coord : sortedCoords) {
            if (Rs2Walker.canReach(coord)) {
                log.error("Found reachable alternative at: {}", coord);
                return coord;
            }
        }
        
        log.warn("No reachable alternatives found from {} coordinates", sortedCoords.size());
        return null;
    }
    
    /**
     * Data classes for efficient banking flow
     */
    private static class TaskRequirements {
        public final List<TaskItem> specialItems = new ArrayList<>();
        public final List<TaskItem> desertItems = new ArrayList<>();
        
        public int getTotalSlotsNeeded() {
            int total = 0;
            
            // Check each special slayer item for stackability
            for (TaskItem item : specialItems) {
                if (isItemStackable(item.name)) {
                    total += 1; // Stackable items take 1 slot regardless of quantity
                } else {
                    total += item.quantity; // Non-stackable items take 1 slot each
                }
            }
            
            // Check each desert item for stackability
            for (TaskItem item : desertItems) {
                if (isItemStackable(item.name)) {
                    total += 1; // Stackable items take 1 slot regardless of quantity
                } else {
                    total += item.quantity; // Non-stackable items take 1 slot each
                }
            }
            
            return total;
        }
    }
    
    private static class TaskItem {
        public final String name;
        public final int quantity;
        
        public TaskItem(String name, int quantity) {
            this.name = name;
            this.quantity = quantity;
        }
    }
    
    private static class MissingItems {
        public final List<TaskItem> slayerMasterItems = new ArrayList<>();
        public final List<TaskItem> shantayItems = new ArrayList<>();
        
        public boolean isEmpty() {
            return slayerMasterItems.isEmpty() && shantayItems.isEmpty();
        }
        
        public int getTotalCoinCost() {
            int cost = 0;
            // Slayer master items cost 1 coin each
            for (TaskItem item : slayerMasterItems) {
                cost += item.quantity;
            }
            // Shantay pass costs 5 coins, waterskins cost 30 coins each
            for (TaskItem item : shantayItems) {
                if (item.name.toLowerCase().contains("shantay pass")) {
                    cost += 5;
                } else if (item.name.toLowerCase().contains("waterskin")) {
                    cost += item.quantity * 30;
                }
            }
            return cost;
        }
    }

    /**
     * Helper method to check if an item is stackable
     */
    private static boolean isItemStackable(String itemName) {
        // First check our hardcoded list of known stackable items
        String lowerName = itemName.toLowerCase();
        if (lowerName.contains("ice cooler") || 
            lowerName.contains("fungicide") || 
            lowerName.contains("explosive") || 
            lowerName.contains("slayer bell")) {
            log.error("DEBUG: '{}' is a known stackable slayer item", itemName);
            return true;
        }
        
        try {
            // For other items, try the dynamic lookup approach
            int itemId = getItemIdByName(itemName);
            log.error("DEBUG: isItemStackable('{}') -> itemId: {}", itemName, itemId);
            
            if (itemId == -1) {
                // If we can't find the item ID, assume it's not stackable for safety
                log.error("DEBUG: Item ID not found for '{}', returning false", itemName);
                return false;
            }
            
            // Use the same logic as AmmoPlugin to check stackability
            ItemComposition itemComp = Microbot.getClient().getItemDefinition(itemId);
            boolean stackable = itemComp != null && itemComp.isStackable();
            log.error("DEBUG: ItemComposition for '{}' (id: {}) -> isStackable: {}", itemName, itemId, stackable);
            
            return stackable;
        } catch (Exception e) {
            // If any error occurs, assume not stackable for safety
            log.error("DEBUG: Exception in isItemStackable('{}'):", itemName, e);
            return false;
        }
    }
    
    /**
     * Helper method to get item ID by name (simplified lookup)
     */
    private static int getItemIdByName(String itemName) {
        // Common stackable slayer items
        if (itemName.toLowerCase().contains("ice cooler")) return 6696;
        if (itemName.toLowerCase().contains("fungicide")) return 7444;
        if (itemName.toLowerCase().contains("explosive")) return 6664;
        if (itemName.toLowerCase().contains("slayer bell")) return 25928;
        
        // Desert items
        if (itemName.toLowerCase().contains("shantay pass")) return 1854;
        if (itemName.toLowerCase().contains("waterskin")) {
            if (itemName.contains("(4)")) return 1823;
            if (itemName.contains("(3)")) return 1825;
            if (itemName.contains("(2)")) return 1827;
            if (itemName.contains("(1)")) return 1829;
            return 1831; // Default to empty waterskin
        }
        
        return -1; // Unknown item
    }

    /**
     * Efficient banking flow methods
     */
    private TaskRequirements determineTaskRequirements() {
        TaskRequirements requirements = new TaskRequirements();
        
        // Determine special slayer items
        String[] requiredItems = SlayerTaskMonster.getItemRequirements(currentTask);
        if (requiredItems.length == 1 && "None".equals(requiredItems[0]) && currentTask.endsWith("s")) {
            String singularTask = currentTask.substring(0, currentTask.length() - 1);
            requiredItems = SlayerTaskMonster.getItemRequirements(singularTask);
        }
        
        for (String item : requiredItems) {
            if (!"None".equalsIgnoreCase(item)) {
                int quantity = isConsumableSlayerItem(item) ? getRequiredAmount(item) : 1;
                requirements.specialItems.add(new TaskItem(item, quantity));
            }
        }
        
        // Determine desert items if this is a desert task
        if (isDesertTask()) {
            // Only require Shantay pass if we're not already in the desert
            if (!isPlayerInDesert() && !Rs2Inventory.hasItem("Shantay pass")) {
                requirements.desertItems.add(new TaskItem("Shantay pass", 1));
            }
            
            int currentWaterskins = getWaterskinCount();
            if (currentWaterskins < 3) {
                requirements.desertItems.add(new TaskItem("Waterskin(4)", 3 - currentWaterskins));
            }
        }
        
        return requirements;
    }
    
    private boolean makeInventorySpace(int slotsNeeded) {
        log.error("Making {} inventory slots by depositing excess food/potions", slotsNeeded);
        
        // Count food and potions
        int foodCount = 0;
        int potionCount = 0;
        
        for (int i = 0; i < 28; i++) {
            if (!Rs2Inventory.isSlotEmpty(i)) {
                String itemName = Rs2Inventory.getNameForSlot(i);
                if (itemName != null) {
                    if (itemName.toLowerCase().contains("lobster") || itemName.toLowerCase().contains("shark") || 
                        itemName.toLowerCase().contains("food") || itemName.toLowerCase().contains("bread")) {
                        foodCount++;
                    } else if (itemName.toLowerCase().contains("potion") || itemName.toLowerCase().contains("brew")) {
                        potionCount++;
                    }
                }
            }
        }
        
        // Deposit the type we have more of
        if (foodCount >= potionCount && foodCount > 0) {
            return depositExcessItems("food", Math.min(slotsNeeded, foodCount));
        } else if (potionCount > 0) {
            return depositExcessItems("potion", Math.min(slotsNeeded, potionCount));
        }
        
        // If no food or potions, just deposit random items
        for (int i = 0; i < Math.min(slotsNeeded, 28); i++) {
            if (!Rs2Inventory.isSlotEmpty(i)) {
                Rs2Bank.depositOne(Rs2Inventory.getIdForSlot(i));
                Global.sleep(300, 600);
            }
        }
        
        return true;
    }
    
    private boolean depositExcessItems(String itemType, int count) {
        int deposited = 0;
        for (int i = 0; i < 28 && deposited < count; i++) {
            if (!Rs2Inventory.isSlotEmpty(i)) {
                String itemName = Rs2Inventory.getNameForSlot(i);
                if (itemName != null && itemName.toLowerCase().contains(itemType)) {
                    Rs2Bank.depositOne(Rs2Inventory.getIdForSlot(i));
                    Global.sleep(300, 600);
                    deposited++;
                }
            }
        }
        return deposited > 0;
    }
    
    private void withdrawAvailableTaskItems(TaskRequirements requirements) {
        // Withdraw special slayer items
        for (TaskItem item : requirements.specialItems) {
            if (Rs2Bank.hasItem(item.name)) {
                log.error("Withdrawing {} {} from bank", item.quantity, item.name);
                Rs2Bank.withdrawX(item.name, item.quantity);
                Global.sleep(600, 1000);
            } else {
                // Try variations
                String[] variations = generateItemNameVariations(item.name);
                for (String variation : variations) {
                    if (Rs2Bank.hasItem(variation)) {
                        log.error("Withdrawing {} {} (variation) from bank", item.quantity, variation);
                        Rs2Bank.withdrawX(variation, item.quantity);
                        Global.sleep(600, 1000);
                        break;
                    }
                }
            }
        }
        
        // Withdraw desert items (including waterskins)
        for (TaskItem item : requirements.desertItems) {
            if (Rs2Bank.hasItem(item.name)) {
                log.error("Withdrawing {} {} from bank", item.quantity, item.name);
                Rs2Bank.withdrawX(item.name, item.quantity);
                Global.sleep(600, 1000);
            } else {
                // Try variations for desert items too (different waterskin types)
                String[] variations = generateItemNameVariations(item.name);
                for (String variation : variations) {
                    if (Rs2Bank.hasItem(variation)) {
                        log.error("Withdrawing {} {} (variation) from bank", item.quantity, variation);
                        Rs2Bank.withdrawX(variation, item.quantity);
                        Global.sleep(600, 1000);
                        break;
                    }
                }
            }
        }
    }
    
    private MissingItems evaluateMissingItems(TaskRequirements requirements) {
        MissingItems missing = new MissingItems();

        for (TaskItem item : requirements.specialItems) {
            int currentCount = Rs2Inventory.itemQuantity(item.name);

            log.error("DEBUG: evaluateMissingItems - {} current count: {}, required: {}", item.name, currentCount, item.quantity);
            if (currentCount < item.quantity) {
                int needed = item.quantity - currentCount;
                missing.slayerMasterItems.add(new TaskItem(item.name, needed));
            } else {
                log.error("DEBUG: Have sufficient {} ({}/{})", item.name, currentCount, item.quantity);
            }

        }
        
        // Check desert items
        for (TaskItem item : requirements.desertItems) {
            int currentCount = Rs2Inventory.count(item.name);
            log.error("DEBUG: evaluateMissingItems - {} current count: {}, required: {}", item.name, currentCount, item.quantity);
            
            if (currentCount < item.quantity) {
                int needed = item.quantity - currentCount;
                log.error("DEBUG: Need to purchase {} {} from Shantay", needed, item.name);
                missing.shantayItems.add(new TaskItem(item.name, needed));
            } else {
                log.error("DEBUG: Have sufficient {} ({}/{})", item.name, currentCount, item.quantity);
            }
        }
        
        return missing;
    }
    
    private boolean ensureSufficientCoins(int coinsNeeded) {
        int currentCoins = Rs2Inventory.itemQuantity("Coins");
        if (currentCoins >= coinsNeeded) {
            return true;
        }
        
        int toWithdraw = coinsNeeded - currentCoins; // Extra buffer
        log.error("Withdrawing {} coins for purchases (need {}, have {})", toWithdraw, coinsNeeded, currentCoins);
        
        if (Rs2Bank.hasItem("Coins")) {
            return Rs2Bank.withdrawX("Coins", toWithdraw);
        } else {
            log.error("No coins in bank to purchase required items");
            return false;
        }
    }
    
    private boolean purchaseAllMissingItems(MissingItems missingItems) {
        // Close bank before traveling
        Rs2Bank.closeBank();
        Global.sleep(1000, 1500);
        
        // Purchase from slayer master if needed
        if (!missingItems.slayerMasterItems.isEmpty()) {
            log.error("Purchasing {} items from slayer master", missingItems.slayerMasterItems.size());
            if (!purchaseFromSlayerMasterConsolidated(missingItems.slayerMasterItems)) {
                return false;
            }
        }
        
        // Purchase from Shantay if needed
        if (!missingItems.shantayItems.isEmpty()) {
            log.error("Purchasing {} items from Shantay", missingItems.shantayItems.size());
            if (!purchaseFromShantayConsolidated(missingItems.shantayItems)) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean purchaseFromSlayerMasterConsolidated(List<TaskItem> items) {
        // Navigate to slayer master
        if (!Rs2Walker.walkTo(config.slayerMaster().getWorldPoint())) {
            log.error("Failed to walk to slayer master");
            return false;
        }
        
        if (!Global.sleepUntil(() -> Rs2Player.getWorldLocation().distanceTo(config.slayerMaster().getWorldPoint()) <= 5, 15000)) {
            log.error("Failed to reach slayer master within timeout");
            return false;
        }
        
        // Open shop
        if (!Rs2Npc.interact(config.slayerMaster().getName(), "Trade")) {
            log.error("Failed to open slayer master shop");
            return false;
        }
        
        if (!Global.sleepUntil(() -> Rs2Shop.isOpen(), 5000)) {
            log.error("Slayer master shop failed to open");
            return false;
        }
        
        // Purchase all items
        for (TaskItem item : items) {
            log.error("Purchasing {} {} from slayer master", item.quantity, item.name);
            
            // Try to buy the full quantity at once
            if (!Rs2Shop.buyItem(item.name, String.valueOf(item.quantity))) {
                log.error("Failed to purchase {} {} in bulk, trying one by one", item.quantity, item.name);
                
                // Fallback: buy one by one if bulk purchase fails
                for (int i = 0; i < item.quantity; i++) {
                    if (!Rs2Shop.buyItem(item.name, "1")) {
                        log.error("Failed to purchase {} (attempt {}/{})", item.name, i + 1, item.quantity);
                        return false;
                    }
                    Global.sleep(600, 1000);
                }
            } else {
                log.error("Successfully purchased {} {} in bulk", item.quantity, item.name);
                Global.sleep(1000, 1500);
            }
        }
        
        Rs2Shop.closeShop();
        return true;
    }
    
    private boolean purchaseFromShantayConsolidated(List<TaskItem> items) {
        WorldPoint shantayLocation = new WorldPoint(3304, 3117, 0);
        
        // Navigate to Shantay Pass
        if (!Rs2Walker.walkTo(shantayLocation)) {
            log.error("Failed to walk to Shantay Pass");
            return false;
        }
        
        if (!Global.sleepUntil(() -> Rs2Player.getWorldLocation().distanceTo(shantayLocation) <= 5, 15000)) {
            log.error("Failed to reach Shantay Pass within timeout");
            return false;
        }
        
        // Open shop with Shantay
        if (!Rs2Npc.interact(SHANTAY, "Trade")) {
            log.error("Failed to trade with Shantay");
            return false;
        }
        
        if (!Global.sleepUntil(() -> Rs2Shop.isOpen(), 5000)) {
            log.error("Shantay's shop failed to open");
            return false;
        }
        
        // Purchase all items in one transaction
        for (TaskItem item : items) {
            log.error("Purchasing {} {} from Shantay", item.quantity, item.name);
            
            if (item.name.toLowerCase().contains("shantay pass")) {
                if (!Rs2Shop.buyItem("Shantay pass", String.valueOf(item.quantity))) {
                    log.error("Failed to purchase {} Shantay pass", item.quantity);
                    return false;
                }
            } else if (item.name.toLowerCase().contains("waterskin")) {
                // buy one by one
                for (int i = 0; i < item.quantity; i++) {
                    if (!Rs2Shop.buyItem("Waterskin(4)", "1")) {
                        log.error("Failed to purchase waterskin (attempt {}/{})", i + 1, item.quantity);
                        return false;
                    }
                    Global.sleep(600, 1000);
                }
            }
        }
        
        Rs2Shop.closeShop();
        return true;
    }

    /**
     * Helper methods for special slayer item requirements
     */
    private boolean needsSpecialSlayerItems() {
        String[] requiredItems = SlayerTaskMonster.getItemRequirements(currentTask);
        
        if (requiredItems == null || requiredItems.length == 0 || 
            (requiredItems.length == 1 && "None".equalsIgnoreCase(requiredItems[0]))) {
            return false;
        }
        
        for (String item : requiredItems) {
            if (!"None".equalsIgnoreCase(item) && !hasSpecialSlayerItem(item)) {
                log.error("Missing required slayer item: {}", item);
                return true;
            }
        }
        
        return false;
    }
    
    private boolean hasSpecialSlayerItem(String itemName) {
        // This method should only check if we have the item in INVENTORY with sufficient quantity
        // Bank availability is handled separately in obtainSpecialSlayerItem()
        
        if (Rs2Inventory.hasItem(itemName)) {
            // For consumable items like ice coolers, check if we have enough
            if (isConsumableSlayerItem(itemName)) {
                int required = getRequiredAmount(itemName);
                int current = Rs2Inventory.count(itemName);
                log.error("Have {} {} in inventory, need {}", current, itemName, required);
                return current >= required;
            }
            return true;
        }
        
        // If not in inventory, we don't "have" it regardless of bank status
        log.error("Item '{}' not in inventory - need to obtain", itemName);
        return false;
    }
    
    private boolean isConsumableSlayerItem(String itemName) {
        return itemName.toLowerCase().contains("ice cooler") ||
               itemName.toLowerCase().contains("fungicide") ||
               itemName.toLowerCase().contains("explosive");
    }
    
    private int getRequiredAmount(String itemName) {
        if (itemName.toLowerCase().contains("ice cooler")) {
            // Need one ice cooler per lizard
            return taskCount;
        }
        if (itemName.toLowerCase().contains("fungicide")) {
            // Need one fungicide per zygomite
            return taskCount;
        }
        if (itemName.toLowerCase().contains("explosive")) {
            // Need one explosive per mogre
            return taskCount;
        }
        // Default to task count for other consumables
        return taskCount;
    }
    
    private boolean obtainSpecialSlayerItems() {
        // Try both current task name and singular version (e.g., "Lizards" -> "Lizard")
        String[] requiredItems = SlayerTaskMonster.getItemRequirements(currentTask);
        
        // If not found, try singular version by removing 's' from the end
        if (requiredItems.length == 1 && "None".equals(requiredItems[0]) && currentTask.endsWith("s")) {
            String singularTask = currentTask.substring(0, currentTask.length() - 1);
            log.error("Trying singular version of task name: {}", singularTask);
            requiredItems = SlayerTaskMonster.getItemRequirements(singularTask);
        }
        
        if (requiredItems == null || requiredItems.length == 0) {
            log.error("No special items required for task: {}", currentTask);
            return true;
        }
        
        log.error("Special items required for {}: {}", currentTask, String.join(", ", requiredItems));
        
        for (String item : requiredItems) {
            if (!"None".equalsIgnoreCase(item)) {
                log.error("Checking for special slayer item: {}", item);
                if (!hasSpecialSlayerItem(item)) {
                    log.error("Missing special slayer item: {}, trying to obtain", item);
                    if (!obtainSpecialSlayerItem(item)) {
                        log.error("Failed to obtain special slayer item: {}", item);
                        return false;
                    }
                } else {
                    log.error("Already have special slayer item: {}", item);
                }
            }
        }
        
        return true;
    }
    
    private boolean obtainSpecialSlayerItem(String itemName) {
        // First try to withdraw from bank if we're at a bank
        if (Rs2Bank.isOpen()) {
            log.error("Bank is open, checking for item: {}", itemName);
            
            // Try exact name first
            if (Rs2Bank.hasItem(itemName)) {
                int required = isConsumableSlayerItem(itemName) ? getRequiredAmount(itemName) : 1;
                log.error("Found exact match, withdrawing {} {} from bank", required, itemName);
                if (Rs2Bank.withdrawX(itemName, required)) {
                    return true;
                }
            }
            
            // Try case-insensitive search with common variations
            String[] variations = generateItemNameVariations(itemName);
            for (String variation : variations) {
                if (Rs2Bank.hasItem(variation)) {
                    int required = isConsumableSlayerItem(itemName) ? getRequiredAmount(itemName) : 1;
                    log.error("Found variation '{}', withdrawing {} from bank", variation, required);
                    if (Rs2Bank.withdrawX(variation, required)) {
                        return true;
                    }
                }
            }
            
            log.error("Item '{}' not found in bank, will need to purchase", itemName);
        }
        
        // If not in bank, need to purchase from slayer master
        if (needsToPurchaseFromSlayerMaster(itemName)) {
            log.error("Item '{}' can be purchased from slayer master", itemName);
            return purchaseFromSlayerMaster(itemName);
        }
        
        log.error("Don't know how to obtain item: {}", itemName);
        return false;
    }
    
    /**
     * Generates common variations of item names to handle case-sensitivity and formatting differences
     */
    private String[] generateItemNameVariations(String itemName) {
        String lowerCase = itemName.toLowerCase();
        String upperCase = itemName.toUpperCase();
        String titleCase = toTitleCase(itemName);
        
        // Special handling for waterskins - try different charge levels
        if (itemName.toLowerCase().contains("waterskin")) {
            return new String[]{
                itemName, lowerCase, upperCase, titleCase,
                "Waterskin(4)", "Waterskin(3)", "Waterskin(2)", "Waterskin(1)", "Waterskin(0)",
                "waterskin(4)", "waterskin(3)", "waterskin(2)", "waterskin(1)", "waterskin(0)"
            };
        }
        
        // Return original and common variations for other items
        return new String[]{itemName, lowerCase, upperCase, titleCase};
    }
    
    /**
     * Converts a string to title case (first letter of each word capitalized)
     */
    private String toTitleCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        String[] words = input.split("\\s+");
        StringBuilder titleCase = new StringBuilder();
        
        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                titleCase.append(" ");
            }
            String word = words[i];
            if (word.length() > 0) {
                titleCase.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    titleCase.append(word.substring(1).toLowerCase());
                }
            }
        }
        
        return titleCase.toString();
    }
    
    private boolean needsToPurchaseFromSlayerMaster(String itemName) {
        // Items that can be purchased from slayer masters
        return itemName.toLowerCase().contains("ice cooler") ||
               itemName.toLowerCase().contains("fungicide") ||
               itemName.toLowerCase().contains("explosive") ||
               itemName.toLowerCase().contains("slayer bell");
    }
    
    private boolean purchaseFromSlayerMaster(String itemName) {
        int required = isConsumableSlayerItem(itemName) ? getRequiredAmount(itemName) : 1;
        int coinsNeeded = required; // Most items cost 1 coin each
        
        log.error("Need to purchase {} {} from slayer master for {} coins", required, itemName, coinsNeeded);
        
        // Check if we have enough coins in inventory
        if (!Rs2Inventory.hasItem("Coins") || Rs2Inventory.count("Coins") < coinsNeeded) {
            if (Rs2Bank.isOpen()) {
                if (Rs2Bank.hasItem("Coins")) {
                    log.error("Withdrawing {} coins for {}", coinsNeeded + 100, itemName); // Extra coins
                    if (!Rs2Bank.withdrawX("Coins", coinsNeeded + 100)) {
                        return false;
                    }
                } else {
                    log.error("No coins in bank to purchase {}", itemName);
                    return false;
                }
                
                // Close bank before traveling
                Rs2Bank.closeBank();
                Global.sleep(1000, 1500);
            } else {
                log.error("Not enough coins to purchase {} and bank is not open", itemName);
                return false;
            }
        }
        
        // Travel to slayer master
        log.error("Traveling to slayer master to purchase {}", itemName);
        if (!Rs2Walker.walkTo(config.slayerMaster().getWorldPoint())) {
            log.error("Failed to walk to slayer master");
            return false;
        }
        
        // Wait to arrive
        if (!Global.sleepUntil(() -> Rs2Player.getWorldLocation().distanceTo(config.slayerMaster().getWorldPoint()) <= 5, 15000)) {
            log.error("Failed to reach slayer master within timeout");
            return false;
        }
        
        // Interact with slayer master to open shop
        log.error("Opening slayer master shop");
        if (!Rs2Npc.interact(config.slayerMaster().getName(), "Trade")) {
            log.error("Failed to trade with slayer master");
            return false;
        }
        
        // Wait for shop to open
        if (!Global.sleepUntil(() -> Rs2Shop.isOpen(), 5000)) {
            log.error("Shop failed to open");
            return false;
        }
        
        // Purchase the required amount
        log.error("Purchasing {} {} from slayer master shop", required, itemName);
        for (int i = 0; i < required; i++) {
            if (!Rs2Shop.buyItem(itemName, "1")) {
                log.error("Failed to purchase {} (attempt {}/{})", itemName, i + 1, required);
                return false;
            }
            Global.sleep(600, 1000);
        }
        
        // Close shop
        Rs2Shop.closeShop();
        log.error("Successfully purchased {} {}", required, itemName);
        
        return true;
    }
    
    /**
     * Helper methods for desert survival mechanics
     */
    private boolean isDesertTask() {
        log.error("Checking if task '{}' is a desert task", currentTask);
        
        // First try to get task location if we don't have it yet
        if (currentTaskLocation == null) {
            try {
                currentTaskLocation = Rs2Slayer.getSlayerTaskLocation();
                log.error("Retrieved task location for desert check: {}", currentTaskLocation != null ? currentTaskLocation.getLocationName() : "null");
            } catch (Exception e) {
                log.error("Failed to get task location for desert check: {}", e.getMessage());
            }
        }
        
        if (currentTaskLocation == null) {
            log.error("No task location found, using monster name fallback");
        }
        
        // Check if any location is in the desert (Kharidian Desert)
        if (currentTaskLocation != null) {
            String locationName = currentTaskLocation.getLocationName();
            if (locationName != null) {
                log.error("Task location name: '{}'", locationName);
                String lowerLocation = locationName.toLowerCase();
                boolean isDesertLocation = lowerLocation.contains("desert") || 
                       lowerLocation.contains("kharid") || 
                       lowerLocation.contains("al-kharid") ||
                       lowerLocation.contains("pollnivneach") ||
                       lowerLocation.contains("nardah") ||
                       lowerLocation.contains("menaphos") ||
                       lowerLocation.contains("sophanem") ||
                       lowerLocation.contains("pyramid") ||
                       lowerLocation.contains("kalphite");
                
                if (isDesertLocation) {
                    log.error("Task location '{}' is in desert", locationName);
                    return true;
                }
            }
        }
        
        // Check by task monster type
        String[] desertMonsters = {"Lizard", "Desert lizard", "Kalphite", "Locust", "Scarab", "Ugthanki"};
        for (String monster : desertMonsters) {
            if (currentTask.toLowerCase().contains(monster.toLowerCase())) {
                log.error("Task monster '{}' matches desert monster '{}'", currentTask, monster);
                return true;
            }
        }
        
        log.error("Task '{}' is not a desert task", currentTask);
        return false;
    }
    
    /**
     * Checks if the player is currently in a desert region (south of Shantay Pass)
     */
    private boolean isPlayerInDesert() {
        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        if (playerLocation == null) {
            return false;
        }
        
        int x = playerLocation.getX();
        int y = playerLocation.getY();
        
        // Kharidian Desert region bounds - ONLY the actual desert (south of Shantay Pass)
        // Al-Kharid is NOT considered desert - you need Shantay Pass to enter the actual desert
        // Desert starts south of Shantay Pass around y=3100 and below
        // Main desert area: x 3200-3520, y 2880-3100
        if (x >= 3200 && x <= 3520 && y >= 2880 && y <= 3100) {
            log.error("Player is in desert at coordinates ({}, {})", x, y);
            return true;
        }
        
        log.error("Player is not in desert at coordinates ({}, {}) - Al-Kharid is not desert", x, y);
        return false;
    }
    
    private boolean needsDesertSurvivalItems() {
        if (!isDesertTask()) {
            log.error("Task '{}' is not a desert task", currentTask);
            return false;
        }
        
        log.error("Task '{}' is a desert task, checking survival items", currentTask);
        
        // Check for Shantay pass (only if not already in desert)
        if (!isPlayerInDesert()) {
            boolean hasShantayPass = Rs2Inventory.hasItem("Shantay pass") || (Rs2Bank.isOpen() && Rs2Bank.hasItem("Shantay pass"));
            if (!hasShantayPass) {
                log.error("Missing Shantay pass for desert task (not in desert yet)");
                return true;
            } else {
                log.error("Have Shantay pass for desert task (not in desert yet)");
            }
        } else {
            log.error("Already in desert, Shantay pass not required");
        }
        
        // Check for waterskins (need 3 for desert task)
        int waterskinCount = getWaterskinCount();
        if (waterskinCount < 1) {
            log.error("Need 3 waterskins for desert task, have {}", waterskinCount);
            return true;
        } else {
            log.error("Have {} waterskins for desert task", waterskinCount);
        }
        
        log.error("Have all required desert survival items");
        return false;
    }
    
    private int getWaterskinCount() {
        int count = 0;
        
        // Count all types of waterskins in inventory
        count += Rs2Inventory.count("Waterskin(4)");
        count += Rs2Inventory.count("Waterskin(3)");
        count += Rs2Inventory.count("Waterskin(2)");
        count += Rs2Inventory.count("Waterskin(1)");
        
        return count;
    }
    
    private int getFullWaterskinCount() {
        int count = Rs2Inventory.count("Waterskin(4)");
        
        if (Rs2Bank.isOpen()) {
            count += Rs2Bank.count("Waterskin(4)");
        }
        
        return count;
    }
    
    
    
    
    private boolean freeInventorySlots(int slotsNeeded) {
        int freedSlots = 0;
        
        // First try to drop/bank food (keep minimum for survival)
        String[] foodItems = {"Shark", "Tuna", "Lobster", "Swordfish", "Salmon", "Bread", "Cake"};
        for (String food : foodItems) {
            if (freedSlots >= slotsNeeded) break;
            
            int foodCount = Rs2Inventory.count(food);
            if (foodCount > 2) { // Keep at least 2 food items
                int toDrop = Math.min(foodCount - 2, slotsNeeded - freedSlots);
                for (int i = 0; i < toDrop; i++) {
                    if (Rs2Bank.isOpen()) {
                        Rs2Bank.depositOne(food);
                    } else {
                        Rs2Inventory.drop(food);
                    }
                    freedSlots++;
                }
            }
        }
        
        // If still need space, drop potions (keep minimum)
        if (freedSlots < slotsNeeded) {
            String[] potionItems = {"Prayer potion", "Super combat", "Combat potion", "Strength potion"};
            for (String potion : potionItems) {
                if (freedSlots >= slotsNeeded) break;
                
                int potionCount = Rs2Inventory.count(potion);
                if (potionCount > 1) { // Keep at least 1 potion
                    int toDrop = Math.min(potionCount - 1, slotsNeeded - freedSlots);
                    for (int i = 0; i < toDrop; i++) {
                        if (Rs2Bank.isOpen()) {
                            Rs2Bank.depositOne(potion);
                        } else {
                            Rs2Inventory.drop(potion);
                        }
                        freedSlots++;
                    }
                }
            }
        }
        
        return freedSlots >= slotsNeeded;
    }
    
    private boolean needsWaterskinRefill() {
        if (!isDesertTask()) {
            return false;
        }
        
        // Check if we're running low on water
        int fullWaterskins = Rs2Inventory.count("Waterskin(4)");
        int totalWaterskins = Rs2Inventory.count("Waterskin(4)") + 
                             Rs2Inventory.count("Waterskin(3)") + 
                             Rs2Inventory.count("Waterskin(2)") + 
                             Rs2Inventory.count("Waterskin(1)");
        
        // Need refill if we have less than 2 full waterskins or less than 3 total
        return fullWaterskins < 2 || totalWaterskins < 3;
    }

    public boolean run(AioSlayerConfig config) {
        this.config = config;
        Microbot.enableAutoRunOn = false;
        Rs2Antiban.resetAntibanSettings();
        Rs2Antiban.setActivity(Activity.GENERAL_SLAYER);
        startTime = Instant.now();
        botState = SlayerBotState.INITIALIZING;
        
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!super.run() || !Microbot.isLoggedIn() || Rs2AntibanSettings.actionCooldownActive) {
                    return;
                }
                
                // Update task count from game state if we have a task
                if (Rs2Slayer.hasSlayerTask()) {
                    int currentTaskCountFromGame = Rs2Slayer.getSlayerTaskSize();
                    if (currentTaskCountFromGame != taskCount) {
                        taskCount = currentTaskCountFromGame;
                        
                        // Check if task was completed
                        if (taskCount == 0) {
                            botState = SlayerBotState.TASK_COMPLETE;
                        }
                    }
                } else if (taskCount > 0) {
                    // If we had a task but now don't have one, it was completed
                    taskCount = 0;
                    botState = SlayerBotState.TASK_COMPLETE;
                }
                
                    executeStateMachine();

            } catch (Exception ex) {
                log.error("Error in AioSlayerScript: " + ex.getMessage(), ex);
                botState = SlayerBotState.ERROR;
            }
        }, 0, 600, TimeUnit.MILLISECONDS);

        return true;
    }

    private void executeStateMachine() {
        log.error("Current bot state: {}", botState);
        switch (botState) {
            case INITIALIZING:
                handleInitializing();
                break;
            case CHECKING_TASK:
                handleCheckingTask();
                break;
            case GETTING_TASK:
                handleGettingTask();
                break;
            case BANKING:
                handleBanking();
                break;
            case TRAVELING_TO_LOCATION:
                handleTravelingToLocation();
                break;
            case SETTING_UP_CANNON:
                handleSettingUpCannon();
                break;
            case COMBAT:
                handleCombat();
                break;
            case LOOTING:
                handleLooting();
                break;
            case SPECIAL_KILL:
                handleSpecialKill();
                break;
            case RESTORING_STATS:
                handleRestoringStats();
                break;
            case TASK_COMPLETE:
                handleTaskComplete();
                break;
            default:
                log.warn("Unknown bot state: " + botState);
                botState = SlayerBotState.CHECKING_TASK;
                break;
        }
    }

    private void handleInitializing() {
        Microbot.status = "Initializing...";
        Global.sleep(1000, 2000);
        botState = SlayerBotState.CHECKING_TASK;
    }

    private void handleCheckingTask() {
        Microbot.status = "Checking current task...";
        
        if (Rs2Slayer.hasSlayerTask()) {
            currentTask = Rs2Slayer.getSlayerTask();
            taskCount = Rs2Slayer.getSlayerTaskSize();
            currentTaskEnum = SlayerTaskEnum.fromTaskName(currentTask);
            
            log.error("Found slayer task: {} ({} remaining)", currentTask, taskCount);
            
            if (currentTaskEnum != null) {
                SlayerTaskEnum.TaskAction action = getTaskAction(currentTaskEnum);
                
                if (action == SlayerTaskEnum.TaskAction.SKIP) {
                    log.error("Task action is SKIP - going to get new task");
                    botState = SlayerBotState.GETTING_TASK;
                    // TODO: Implement skip task logic
                    return;
                } else if (action == SlayerTaskEnum.TaskAction.BLOCK) {
                    log.error("Task action is BLOCK - going to get new task");
                    botState = SlayerBotState.GETTING_TASK;
                    // TODO: Implement block task logic
                    return;
                }
            }
            
            // Smart state detection - determine best state to resume from
            SlayerBotState resumeState = determineResumeState();
            log.error("Resuming from state: {}", resumeState);
            botState = resumeState;
        } else {
            log.error("No current slayer task");
            botState = SlayerBotState.GETTING_TASK;
        }
    }

    private void handleGettingTask() {
        Microbot.status = "Getting new task...";
        
        if (!Rs2Walker.walkTo(config.slayerMaster().getWorldPoint())) {
            log.warn("Failed to walk to slayer master");
            Global.sleep(2000, 3000);
            return;
        }

        if (Rs2Npc.interact(config.slayerMaster().getName(), "Assignment")) {
            Global.sleepUntil(() -> Rs2Slayer.hasSlayerTask(), 10000);
            if (Rs2Slayer.hasSlayerTask()) {
                currentTask = Rs2Slayer.getSlayerTask();
                taskCount = Rs2Slayer.getSlayerTaskSize();
                currentTaskEnum = SlayerTaskEnum.fromTaskName(currentTask);
                log.error("Received new task: {} ({})", currentTask, taskCount);
                botState = SlayerBotState.BANKING;
            }
        }
    }

    private void handleBanking() {
        if (currentTaskEnum == null) {
            log.warn("No valid task enum for task: " + currentTask);
            botState = SlayerBotState.CHECKING_TASK;
            return;
        }

        // First check if we're at a bank
        if (!Rs2Bank.isOpen() && !Rs2Bank.isNearBank(10)) {
            Microbot.status = "Walking to bank...";
            log.error("Not at bank (current location: {}), finding nearest bank", Rs2Player.getWorldLocation());
            
            // Use the same pattern as ShortestPathPanel for finding nearest bank
            try {
                BankLocation nearestBank = Rs2Bank.getNearestBank();
                if (nearestBank != null) {
                    log.error("Found nearest bank: {} at {}", nearestBank.toString(), nearestBank.getWorldPoint());
                    
                    // Walk to the nearest bank using Rs2Walker
                    if (!Rs2Walker.walkTo(nearestBank.getWorldPoint())) {
                        log.error("Failed to start walking to nearest bank");
                        Global.sleep(3000, 4000);
                        return;
                    }
                    
                    // Wait to arrive at bank
                    log.error("Walking to nearest bank, waiting to arrive...");
                    if (!Global.sleepUntil(() -> Rs2Bank.isNearBank(10), 20000)) {
                        log.error("Failed to reach bank within timeout - current location: {}", Rs2Player.getWorldLocation());
                        return;
                    }
                    
                    log.error("Arrived near bank at location: {}", Rs2Player.getWorldLocation());
                } else {
                    log.error("Could not find nearest bank");
                    Global.sleep(3000, 4000);
                    return;
                }
            } catch (Exception ex) {
                log.error("Error while finding the nearest bank: {}", ex.getMessage());
                Global.sleep(3000, 4000);
                return;
            }
        }
        
        // Open bank if we're near one but it's not open
        if (Rs2Bank.isNearBank(10) && !Rs2Bank.isOpen()) {
            log.error("Near bank but not open, attempting to open bank");
            Microbot.status = "Opening bank...";
            
            if (!Rs2Bank.openBank()) {
                log.error("Failed to open bank - retrying bank navigation");
                // Try walking to bank again if opening failed
                if (!Rs2Bank.walkToBank()) {
                    log.warn("Failed to walk to bank on retry");
                    Global.sleep(2000, 3000);
                    return;
                }
                Global.sleep(1000, 2000);
                return;
            }
            
            // Wait for bank to open
            if (!Global.sleepUntil(() -> Rs2Bank.isOpen(), 5000)) {
                log.error("Bank failed to open within timeout - retrying");
                Global.sleep(2000, 3000);
                return;
            }
            
            log.error("Successfully opened bank");
        }
        
        // Verify we're actually at a bank before proceeding
        if (!Rs2Bank.isOpen()) {
            log.error("Bank is not open, cannot proceed with banking operations");
            return;
        }
        
        // Now we're at the bank and it's open, proceed with efficient banking flow
        Microbot.status = "Banking for supplies...";
        log.error("Bank is open, executing efficient banking flow");
        
        // Step 1-2: Load inventory setup (basic gear and consumables)
        InventorySetup setup = getInventorySetup(currentTaskEnum);
        if (setup == null) {
            log.warn("No inventory setup configured for task: {}.", currentTask);
            log.warn("Please configure an inventory setup for this task.");
            botState = SlayerBotState.TRAVELING_TO_LOCATION;
            return;
        }

        log.error("Step 1-2: Loading inventory setup: {}", setup.getName());
        Rs2InventorySetup inventorySetupHelper = new Rs2InventorySetup(setup, mainScheduledFuture);
        if (!inventorySetupHelper.loadInventory() || !inventorySetupHelper.loadEquipment()) {
            log.warn("Failed to load inventory setup completely");
            Global.sleep(2000, 3000);
            return;
        }
        
        // Step 3: Calculate available inventory space
        int availableSlots = Rs2Inventory.getEmptySlots();
        log.error("Step 3: Available inventory slots: {}", availableSlots);
        
        // Step 4-5: Determine task requirements and manage space
        TaskRequirements requirements = determineTaskRequirements();
        
        // Debug: Show detailed item breakdown
        log.error("Step 4: Task requirements breakdown:");
        for (TaskItem item : requirements.specialItems) {
            boolean stackable = isItemStackable(item.name);
            log.error("  Special item: {} x{} (stackable: {})", item.name, item.quantity, stackable);
        }
        for (TaskItem item : requirements.desertItems) {
            boolean stackable = isItemStackable(item.name);
            log.error("  Desert item: {} x{} (stackable: {})", item.name, item.quantity, stackable);
        }
        log.error("Step 4 Summary: Special items: {}, Desert items: {}", 
                  requirements.specialItems.size(), requirements.desertItems.size());
        
        int requiredSlots = requirements.getTotalSlotsNeeded();
        log.error("Step 4: Calculated required slots: {}", requiredSlots);
        if (requiredSlots > availableSlots) {
            log.error("Step 5: Need {} slots but only have {}, making space", requiredSlots, availableSlots);
            if (!makeInventorySpace(requiredSlots - availableSlots)) {
                log.warn("Failed to make sufficient inventory space");
                Global.sleep(2000, 3000);
                return;
            }
        }
        
        // Step 6: Withdraw all available task requirements from bank
        log.error("Step 6: Withdrawing available task requirements from bank");
        withdrawAvailableTaskItems(requirements);
        
        // Step 7-8: Evaluate what's missing and get coins if needed
        log.error("Step 7-8: Evaluating missing items and preparing for purchases");
        MissingItems missingItems = evaluateMissingItems(requirements);
        if (!missingItems.isEmpty()) {
            if (!ensureSufficientCoins(missingItems.getTotalCoinCost())) {
                log.warn("Failed to get sufficient coins for purchases");
                Global.sleep(2000, 3000);
                return;
            }
        }
        
        // Step 9-10: Purchase missing items (consolidated trips)
        if (!purchaseAllMissingItems(missingItems)) {
            log.warn("Failed to purchase missing items");
            Global.sleep(2000, 3000);
            return;
        }

        log.error("Banking complete - proceeding to task location");
        botState = SlayerBotState.TRAVELING_TO_LOCATION;
    }

    private void handleTravelingToLocation() {
        Microbot.status = "Traveling to task location...";
        
        // First check if we actually have a slayer task
        if (!Rs2Slayer.hasSlayerTask()) {
            log.warn("No active slayer task found. Going to get a new task.");
            botState = SlayerBotState.GETTING_TASK;
            return;
        }
        
        if (currentTaskLocation == null) {
            try {
                currentTaskLocation = Rs2Slayer.getSlayerTaskLocation();
            } catch (Exception e) {
                log.error("Failed to get slayer task location", e);
                if (e.getMessage() != null && e.getMessage().contains("statsMap")) {
                    log.error("Rs2NpcManager not initialized properly. Attempting to initialize...");
                    try {
                        Rs2NpcManager.loadJson();
                        currentTaskLocation = Rs2Slayer.getSlayerTaskLocation();
                    } catch (Exception e2) {
                        log.error("Failed to initialize Rs2NpcManager", e2);
                        botState = SlayerBotState.ERROR;
                        return;
                    }
                } else {
                    botState = SlayerBotState.ERROR;
                    return;
                }
            }
            
            if (currentTaskLocation == null) {
                log.warn("Could not find location for task: " + currentTask);
                log.warn("Current task: {}", Rs2Slayer.getSlayerTask());
                log.warn("Available monsters: {}", Rs2Slayer.getSlayerMonsters());
                botState = SlayerBotState.ERROR;
                return;
            }
        }

        WorldPoint destination = currentTaskLocation.getClosestToCenter();
        if (destination != null && Rs2Player.getWorldLocation().distanceTo(destination) <= 10) {
            log.error("Arrived at task location");
            
            // Check if we should place a cannon
            if (shouldUseCannon(currentTaskEnum) && !cannonPlaced) {
                botState = SlayerBotState.SETTING_UP_CANNON;
            } else {
                botState = SlayerBotState.COMBAT;
            }
        } else {
            if (destination != null) {
                log.error("Location WorldPoint(x={}, y={}, plane={}) impossible to reach", 
                    destination.getX(), destination.getY(), destination.getPlane());
                
                // Check if the destination is reachable first
                if (!Rs2Walker.canReach(destination)) {
                    log.error("Destination is not reachable, trying alternative locations");
                    
                    // Try to get alternative locations from all coordinates in the monster location
                    WorldPoint alternativeDestination = findReachableAlternative(currentTaskLocation);
                    
                    if (alternativeDestination != null) {
                        log.error("Found alternative reachable destination: {}", alternativeDestination);
                        destination = alternativeDestination;
                    } else {
                        log.warn("No reachable alternatives found, trying getBestClusterCenter");
                        destination = currentTaskLocation.getBestClusterCenter();
                        
                        if (destination == null || !Rs2Walker.canReach(destination)) {
                            log.error("Failed to find any reachable destination for {} task. Skipping task.", currentTask);
                            // Skip to next task
                            botState = SlayerBotState.GETTING_TASK;
                            return;
                        }
                    }
                }
                
                log.error("Attempting to walk to: {}", destination);
                if (!Rs2Walker.walkTo(destination)) {
                    log.warn("Failed to walk to task location");
                    Global.sleep(2000, 3000);
                }
            } else {
                log.warn("No valid destination found for task location");
                botState = SlayerBotState.ERROR;
            }
        }
    }

    private void handleSettingUpCannon() {
        Microbot.status = "Setting up cannon...";
        
        if (!Rs2Inventory.hasItem("dwarf multicannon")) {
            log.warn("No cannon in inventory");
            botState = SlayerBotState.COMBAT;
            return;
        }

        // Find a good spot to place cannon
        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        if (cannonLocation == null) {
            cannonLocation = findCannonSpot(playerLocation);
        }

        if (cannonLocation != null) {
            if (!Rs2Walker.walkTo(cannonLocation)) {
                log.warn("Failed to walk to cannon location");
                botState = SlayerBotState.COMBAT;
                return;
            }

            // Place cannon
            if (Rs2Inventory.interact("dwarf multicannon", "Set-up")) {
                Global.sleepUntil(() -> Rs2GameObject.findObjectById(6, 10) != null, 10000);
                cannonPlaced = true;
                log.error("Cannon placed successfully");
                botState = SlayerBotState.COMBAT;
            }
        } else {
            log.warn("Could not find suitable cannon location");
            botState = SlayerBotState.COMBAT;
        }
    }

    private void handleCombat() {
        log.error("=== ENTERING COMBAT HANDLER ===");
        log.error("In combat: {}", Rs2Combat.inCombat());
        log.error("Current task: {}", currentTask);
        log.error("Player location: {}", Rs2Player.getWorldLocation());

        // Check combat stats and consume potions/food
        handleCombatStats();

        // Check if task is complete
        if (!Rs2Slayer.hasSlayerTask() || Rs2Slayer.getSlayerTaskSize() == 0) {
            log.error("Task is complete, changing state");
            botState = SlayerBotState.TASK_COMPLETE;
            return;
        }

        // Check if we need to restock supplies before continuing
        if (needsSupplies()) {
            log.error("Low on supplies, going to bank to restock");
            botState = SlayerBotState.BANKING;
            return;
        }

        // Handle cannon refilling
        if (cannonPlaced && Rs2Inventory.hasItem("cannonball")) {
            Rs2Cannon.refill();
        }

        // Priority: Check if an NPC is already attacking us and focus on it
        var attackingNpc = Rs2Npc.getNpcs()
            .filter(npc -> npc != null
                && npc.getRuneliteNpc() != null
                && npc.getRuneliteNpc().getInteracting() == Microbot.getClient().getLocalPlayer())
            .findFirst()
            .orElse(null);
        
        if (attackingNpc != null && !Rs2Combat.inCombat()) {
            log.error("NPC {} is attacking us - focusing on this target", attackingNpc.getName());
            // Check if this NPC is a valid slayer target
            List<String> monsterNames = Rs2Slayer.getSlayerMonsters();
            if (monsterNames != null && monsterNames.stream().anyMatch(name -> 
                attackingNpc.getName() != null && attackingNpc.getName().toLowerCase().contains(name.toLowerCase()))) {
                
                // Attack the NPC that's attacking us
                if (Rs2Npc.interact(attackingNpc, "attack")) {
                    lastCombatTime = Instant.now();
                    lastCombatAction = Instant.now();
                    log.error("Successfully focused on attacking NPC: {}", attackingNpc.getName());
                    return; // Exit early, we're handling this attacking NPC
                }
            }
        }

        // Check for stuck combat situations
        if (Rs2Combat.inCombat()) {
            CombatStyle combatStyle = getCurrentCombatStyle();
            int timeoutSeconds = getTimeoutForCombatStyle(combatStyle);
            
            // Check if we're stuck in combat (no combat action for too long)
            if (lastCombatAction != null && 
                Instant.now().isAfter(lastCombatAction.plusSeconds(timeoutSeconds))) {
                log.error("Stuck in {} combat - resetting combat state", combatStyle);
                // Reset combat state by stepping away and back
                Rs2Walker.walkTo(Rs2Player.getWorldLocation().dx(1));
                lastCombatTime = null;
                lastCombatAction = null;
                return;
            }
            
            // Update combat action timestamp if we're actively attacking
            if (isActivelyAttacking()) {
                lastCombatAction = Instant.now();
            } else {
                // For ranged/magic, if we can't attack our current target, look for a new one immediately
                if (combatStyle != CombatStyle.MELEE && lastCombatAction != null &&
                    Instant.now().isAfter(lastCombatAction.plusSeconds(3))) {
                    log.error("Current target not attackable with {}, looking for new target", combatStyle);
                    lastCombatTime = null;
                    lastCombatAction = null;
                    return;
                }
            }
        } else {
            // Find and attack monsters when not in combat (prefer reachable ones)
            List<String> monsterNames = Rs2Slayer.getSlayerMonsters();
            log.error("Slayer monsters for current task: {}", monsterNames);
            if (monsterNames != null && !monsterNames.isEmpty()) {
                for (String monsterName : monsterNames) {
                    log.error("Trying to attack monster: {}", monsterName);
                    if (attackReachableNpc(monsterName)) {
                        lastCombatTime = Instant.now();
                        lastCombatAction = Instant.now();
                        log.error("Successfully attacked {}", monsterName);
                        checkForFailedAttackMessages(); // Check if attack actually succeeded
                        break;
                    } else {
                        log.error("No attackable {} found", monsterName);
                    }
                }
            } else {
                log.error("No slayer monsters found for current task");
            }
        }

        // Handle special kill requirements (gargoyles, etc.)
        var interacting = Microbot.getClient().getLocalPlayer().getInteracting();
        Rs2NpcModel targetNpc = null;
        if (interacting instanceof NPC) {
            targetNpc = new Rs2NpcModel((NPC) interacting);
        }
        if (currentTaskEnum != null && currentTaskEnum.isRequiresSpecialKill() && targetNpc != null && targetNpc.getHealthRatio() < 3) {
            handleSpecialKillRequirement();
        }

        // Check for loot after combat
        if (lastCombatTime != null && 
            Instant.now().isAfter(lastCombatTime.plusSeconds(3)) && 
            !Rs2Combat.inCombat()) {
            
            // Only go to looting state if there's actually something to loot
            if (shouldLoot()) {
            botState = SlayerBotState.LOOTING;
            } else {
                // Reset combat timer to continue looking for monsters
                lastCombatTime = null;
            }
        }
    }

    private void handleLooting() {
        Microbot.status = "Looting items...";
        
        if (config.lootItems()) {
            // Loot valuable items (with reachability check)
            if (Rs2GroundItem.isItemBasedOnValueOnGround(config.minimumItemValue(), 10)) {
                if (lootReachableItemBasedOnValue(config.minimumItemValue(), 10)) {
                Global.sleep(600, 1200);
                }
            }

            // Loot additional specified items (with reachability check)
            if (!config.additionalLootItems().isEmpty()) {
                String[] additionalItems = config.additionalLootItems().split(",");
                for (String item : additionalItems) {
                    if (Rs2GroundItem.exists(item.trim(), 10)) {
                        if (lootReachableItem(item.trim(), 10)) {
                        Global.sleep(600, 1200);
                        }
                    }
                }
            }

            // Bury bones if configured
            if (config.buryBones() && Rs2Inventory.hasItem("bones")) {
                Rs2Inventory.interact("bones", "Bury");
                Global.sleep(300, 600);
            }

            // High alchemy valuable items
            if (config.highAlchemy() && Microbot.getClient().getRealSkillLevel(Skill.MAGIC) >= 55) {
                if (hasItemsToAlch()) {
                performHighAlchemy();
                }
            }
        }

        // Return to combat
        botState = SlayerBotState.COMBAT;
    }

    private void handleSpecialKill() {
        Microbot.status = "Performing special kill...";
        
        if (currentTaskEnum == SlayerTaskEnum.GARGOYLES) {
            // Handle gargoyle finishing - look for low health gargoyles
            if (Rs2Npc.getNpcsForPlayer().anyMatch(npc -> 
                npc.getName() != null && npc.getName().toLowerCase().contains("gargoyle") && 
                npc.getHealthRatio() <= 10)) {
                if (Rs2Inventory.hasItem("rock hammer")) {
                    Rs2Inventory.interact("rock hammer", "Wield");
                    Global.sleep(600, 1000);
                    Rs2Npc.interact("gargoyle", "Smash");
                }
            }
        } else if (currentTaskEnum == SlayerTaskEnum.LIZARDS) {
            // Handle lizard cooling - look for low health lizards
            if (Rs2Npc.getNpcsForPlayer().anyMatch(npc -> 
                npc.getName() != null && npc.getName().toLowerCase().contains("lizard") && 
                npc.getHealthRatio() <= 5)) {
                if (Rs2Inventory.hasItem("ice cooler")) {
                    Rs2Inventory.interact("ice cooler", "Use");
                    Global.sleepUntil(() -> Microbot.getClient().isWidgetSelected(), 2000);
                    Rs2Npc.interact("lizard", "Use");
                }
            }
        }
        // Add more special kill handlers as needed
        
        botState = SlayerBotState.COMBAT;
    }

    private void handleRestoringStats() {
        Microbot.status = "Restoring stats...";
        
        // Eat food if health is low
        if (Rs2Player.getBoostedSkillLevel(Skill.HITPOINTS) <= config.eatAtHp()) {
            if (!config.foodType().isEmpty() && Rs2Inventory.hasItem(config.foodType())) {
                Rs2Inventory.interact(config.foodType(), "Eat");
                Global.sleep(600, 1200);
            }
        }

        // Drink prayer potion if prayer is low
        if (Rs2Player.getBoostedSkillLevel(Skill.PRAYER) <= config.drinkPrayerAt()) {
            Rs2Player.drinkPrayerPotionAt(config.drinkPrayerAt());
            Global.sleep(600, 1200);
        }

        // Drink combat potions if configured
        if (config.useCombatPotions()) {
            Rs2Player.drinkCombatPotionAt(Skill.ATTACK);
            Rs2Player.drinkCombatPotionAt(Skill.STRENGTH);
            Rs2Player.drinkCombatPotionAt(Skill.DEFENCE);
        }

        botState = SlayerBotState.COMBAT;
    }

    private void handleTaskComplete() {
        Microbot.status = "Task completed!";
        
        tasksCompleted++;
        currentTask = "";
        taskCount = 0;
        currentTaskLocation = null;
        currentTaskEnum = null;
        cannonPlaced = false;
        cannonLocation = null;
        lastCombatTime = null;
        lastCombatAction = null;
        
        log.error("Slayer task completed! Total tasks completed: {}", tasksCompleted);
        
        // Go back to slayer master for new task
        botState = SlayerBotState.GETTING_TASK;
    }

    private void handleCombatStats() {
        // Eat food if health is low
        if (Rs2Player.getBoostedSkillLevel(Skill.HITPOINTS) <= config.eatAtHp()) {
            if (!config.foodType().isEmpty() && Rs2Inventory.hasItem(config.foodType())) {
                Rs2Inventory.interact(config.foodType(), "Eat");
                Global.sleep(300, 600);
            }
        }

        // Drink prayer potion if prayer is low
        if (Rs2Player.getBoostedSkillLevel(Skill.PRAYER) <= config.drinkPrayerAt()) {
            Rs2Player.drinkPrayerPotionAt(config.drinkPrayerAt());
        }

        // Use special attack if configured and available
        if (config.useSpecialAttack() && 
            Microbot.getClient().getVarpValue(300) >= config.specialAttackAt()) {
            Microbot.getSpecialAttackConfigs().setSpecialAttack(true);
        }
    }

    private void handleSpecialKillRequirement() {
        if (currentTaskEnum == SlayerTaskEnum.GARGOYLES) {
            if (Rs2Npc.getNpcsForPlayer().anyMatch(npc -> 
                npc.getName() != null && npc.getName().toLowerCase().contains("gargoyle") && 
                npc.getHealthRatio() <= 10)) {
                botState = SlayerBotState.SPECIAL_KILL;
            }
        } else if (currentTaskEnum == SlayerTaskEnum.LIZARDS) {
            if (Rs2Npc.getNpcsForPlayer().anyMatch(npc -> 
                npc.getName() != null && npc.getName().toLowerCase().contains("lizard") && 
                npc.getHealthRatio() <= 5)) {
                botState = SlayerBotState.SPECIAL_KILL;
            }
        }
        // Add more special kill requirements as needed
    }

    private boolean hasItemsToAlch() {
        // Check if there are any items in inventory that meet alchemy criteria
        return Rs2Inventory.all().stream()
            .anyMatch(item -> {
                int geValue = Microbot.getItemManager().getItemPrice(item.getId());
                int haValue = item.getHaPrice();
                return geValue >= config.minimumItemValue() && 
                       (haValue - geValue) >= config.alchemyProfitThreshold();
            });
    }

    private boolean shouldLoot() {
        if (!config.lootItems()) {
            return false;
        }
        
        // Check if there are reachable valuable items on ground
        if (hasReachableValuableItems(config.minimumItemValue(), 10)) {
            return true;
        }
        
        // Check if there are reachable additional specified items on ground
        if (!config.additionalLootItems().isEmpty()) {
            String[] additionalItems = config.additionalLootItems().split(",");
            for (String item : additionalItems) {
                if (hasReachableItem(item.trim(), 10)) {
                    return true;
                }
            }
        }
        
        // Check if we need to bury bones
        if (config.buryBones() && Rs2Inventory.hasItem("bones")) {
            return true;
        }
        
        // Check if we have items to alch
        if (config.highAlchemy() && Microbot.getClient().getRealSkillLevel(Skill.MAGIC) >= 55 && hasItemsToAlch()) {
            return true;
        }
        
        return false;
    }

    private boolean hasReachableValuableItems(int minimumValue, int radius) {
        // Check if there are any valuable items that are actually reachable
        RS2Item[] allItems = Rs2GroundItem.getAll(radius);
        if (allItems == null || allItems.length == 0) {
            return false;
        }
        
        for (RS2Item item : allItems) {
            if (item != null) {
                // Get item value from ItemComposition
                int itemValue = item.getItem().getHaPrice() * item.getTileItem().getQuantity();
                if (itemValue >= minimumValue) {
                    WorldPoint itemLocation = item.getTile().getWorldLocation();
                    if (Rs2Walker.canReach(itemLocation)) {
                        return true; // Found at least one reachable valuable item
                    }
                }
            }
        }
        return false;
    }

    private boolean hasReachableItem(String itemName, int radius) {
        // Check if there are any specified items that are actually reachable
        RS2Item[] allItems = Rs2GroundItem.getAll(radius);
        if (allItems == null || allItems.length == 0) {
            return false;
        }
        
        for (RS2Item item : allItems) {
            if (item != null) {
                String currentItemName = item.getItem().getName();
                if (currentItemName.toLowerCase().contains(itemName.toLowerCase())) {
                    WorldPoint itemLocation = item.getTile().getWorldLocation();
                    if (Rs2Walker.canReach(itemLocation)) {
                        return true; // Found at least one reachable specified item
                    }
                }
            }
        }
        return false;
    }

    private boolean attackReachableNpc(String npcName) {
        // Determine combat style to apply appropriate targeting logic
        CombatStyle combatStyle = getCurrentCombatStyle();
        
        // Debug: Show all attackable NPCs nearby
        List<net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel> allAttackableNpcs = Rs2Npc.getAttackableNpcs()
            .collect(java.util.stream.Collectors.toList());
        log.error("All attackable NPCs nearby: {}", allAttackableNpcs.stream()
            .map(npc -> npc.getName())
            .collect(java.util.stream.Collectors.toList()));
        
        // Use Rs2Npc.getAttackableNpcs which already handles combat level, death state, and interaction checks
        // For melee, also use reachability filtering to eliminate clearly unreachable NPCs
        boolean useReachabilityFilter = (combatStyle == CombatStyle.MELEE);
        List<net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel> availableNpcs;
        
        if (useReachabilityFilter) {
            availableNpcs = Rs2Npc.getAttackableNpcs(true) // Use reachability filter for melee
                .filter(npc -> npc.getName() != null && npc.getName().equalsIgnoreCase(npcName))
                .collect(java.util.stream.Collectors.toList());
        } else {
            availableNpcs = Rs2Npc.getAttackableNpcs(npcName) // Standard filtering for ranged/magic
                .collect(java.util.stream.Collectors.toList());
        }
        
        log.error("Found {} attackable {} NPCs, checking {} combat style compatibility", availableNpcs.size(), npcName, combatStyle);
        
        // Filter out blacklisted NPCs and log rejections
        List<net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel> nonBlacklistedNpcs = availableNpcs.stream()
            .filter(npc -> {
                if (isNpcBlacklisted(npc)) {
                    log.error("Skipping blacklisted {} at {} (failed {} times)", 
                        npc.getName(), npc.getWorldLocation(), 
                        npcBlacklist.get(getNpcKey(npc)).failedAttempts);
                    return false;
                }
                return true;
            })
            .collect(java.util.stream.Collectors.toList());
            
        log.error("After blacklist filtering: {} NPCs remain out of {}", nonBlacklistedNpcs.size(), availableNpcs.size());
        
        return nonBlacklistedNpcs.stream()
            .filter(npc -> isNpcAttackable(npc, combatStyle)) // Apply combat-style-specific checks
            .sorted((npc1, npc2) -> {
                // Sort by distance to prefer closer NPCs
                int dist1 = Rs2Player.getWorldLocation().distanceTo(npc1.getWorldLocation());
                int dist2 = Rs2Player.getWorldLocation().distanceTo(npc2.getWorldLocation());
                return Integer.compare(dist1, dist2);
            })
            .findFirst()
            .map(npc -> {
                log.error("Attempting {} attack on {} at {} (distance: {})", 
                        combatStyle, npc.getName(), npc.getWorldLocation(), 
                        Rs2Player.getWorldLocation().distanceTo(npc.getWorldLocation()));
                
                // Store the attempted NPC for potential blacklisting
                lastAttemptedNpc = npc;
                
                boolean attackResult = Rs2Npc.interact(npc, "Attack");
                if (attackResult) {
                    log.error("Successfully initiated attack on {}", npc.getName());
                } else {
                    log.error("Attack interaction failed for {}, blacklisting NPC", npc.getName());
                    recordFailedAttack(npc);
                    lastAttemptedNpc = null; // Clear since we already recorded the failure
                }
                return attackResult;
            })
            .orElse(false);
    }

    private enum CombatStyle {
        MELEE,
        RANGED,
        MAGIC
    }

    private CombatStyle getCurrentCombatStyle() {
        if (!Rs2Equipment.isWearing(EquipmentInventorySlot.WEAPON)) {
            return CombatStyle.MELEE; // Default to melee if no weapon
        }
        
        String weaponName = Rs2Equipment.get(EquipmentInventorySlot.WEAPON).getName().toLowerCase();
        
        if (weaponName.contains("bow") || weaponName.contains("crossbow") || 
            weaponName.contains("dart") || weaponName.contains("javelin") ||
            weaponName.contains("knife") || weaponName.contains("thrownaxe")) {
            return CombatStyle.RANGED;
        }
        
        if (weaponName.contains("staff") || weaponName.contains("wand") || 
            weaponName.contains("tome") || weaponName.contains("sceptre") ||
            weaponName.contains("battlestaff") || weaponName.contains("orb") ||
            weaponName.contains("trident") || weaponName.contains("sang")) {
            return CombatStyle.MAGIC;
        }
        
        return CombatStyle.MELEE;
    }

    private boolean isNpcAttackable(net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel npc, CombatStyle combatStyle) {
        WorldPoint npcLocation = npc.getWorldLocation();
        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        
        switch (combatStyle) {
            case MELEE:
                // For melee, NPC must be reachable (within 1-2 tiles and pathable)
                boolean canReach = Rs2Walker.canReach(npcLocation);
                return canReach;
                
            case RANGED:
            case MAGIC:
                // For ranged/magic, be very aggressive - allow attacks within reasonable range
                int distance = playerLocation.distanceTo(npcLocation);
                log.error("Checking {} attackability: distance = {}", combatStyle, distance);
                if (distance <= 15) {
                    log.error("NPC within range for {}, allowing attack", combatStyle);
                    return true; // Allow direct attacks within reasonable range
                }
                log.error("NPC too far for {} (distance: {})", combatStyle, distance);
                return false;
                
            default:
                return Rs2Walker.canReach(npcLocation);
        }
    }

    private boolean canFindAttackablePosition(WorldPoint npcLocation, int maxRange) {
        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        
        // First check current position
        int currentDistance = playerLocation.distanceTo(npcLocation);
        if (currentDistance <= maxRange && hasLineOfSight(playerLocation, npcLocation)) {
            return true;
        }
        
        // Search for better positions within a reasonable search radius
        int searchRadius = Math.min(15, maxRange + 5); // Don't search too far
        
        for (int dx = -searchRadius; dx <= searchRadius; dx++) {
            for (int dy = -searchRadius; dy <= searchRadius; dy++) {
                WorldPoint candidatePosition = new WorldPoint(
                    playerLocation.getX() + dx, 
                    playerLocation.getY() + dy, 
                    playerLocation.getPlane()
                );
                
                // Skip if too far from our current position (we don't want to run across the map)
                if (playerLocation.distanceTo(candidatePosition) > 8) {
                    continue;
                }
                
                // Check if we can reach this candidate position
                if (!Rs2Walker.canReach(candidatePosition)) {
                    continue;
                }
                
                // Check if from this position we'd be in range and have line of sight
                int distanceToNpc = candidatePosition.distanceTo(npcLocation);
                if (distanceToNpc <= maxRange && hasLineOfSight(candidatePosition, npcLocation)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    private WorldPoint findBestAttackPosition(WorldPoint npcLocation, int maxRange) {
        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        
        // First check if current position is good
        int currentDistance = playerLocation.distanceTo(npcLocation);
        if (currentDistance <= maxRange && hasLineOfSight(playerLocation, npcLocation)) {
            return playerLocation; // Current position is fine
        }
        
        WorldPoint bestPosition = null;
        int bestDistance = Integer.MAX_VALUE;
        
        // Search for optimal positions within a reasonable search radius
        int searchRadius = Math.min(15, maxRange + 5);
        
        for (int dx = -searchRadius; dx <= searchRadius; dx++) {
            for (int dy = -searchRadius; dy <= searchRadius; dy++) {
                WorldPoint candidatePosition = new WorldPoint(
                    playerLocation.getX() + dx, 
                    playerLocation.getY() + dy, 
                    playerLocation.getPlane()
                );
                
                // Skip if too far from our current position
                if (playerLocation.distanceTo(candidatePosition) > 8) {
                    continue;
                }
                
                // Check if we can reach this candidate position
                if (!Rs2Walker.canReach(candidatePosition)) {
                    continue;
                }
                
                // Check if from this position we'd be in range and have line of sight
                int distanceToNpc = candidatePosition.distanceTo(npcLocation);
                if (distanceToNpc <= maxRange && hasLineOfSight(candidatePosition, npcLocation)) {
                    // Prefer positions closer to our current location (less movement required)
                    int walkDistance = playerLocation.distanceTo(candidatePosition);
                    if (walkDistance < bestDistance) {
                        bestDistance = walkDistance;
                        bestPosition = candidatePosition;
                    }
                }
            }
        }
        
        return bestPosition;
    }

    private boolean hasLineOfSight(WorldPoint from, WorldPoint to) {
        // Simple line of sight check - more sophisticated than pathfinding
        // This checks if there's a clear "shot" between two points
        
        int dx = Math.abs(to.getX() - from.getX());
        int dy = Math.abs(to.getY() - from.getY());
        
        // If very close, assume line of sight exists
        if (dx <= 1 && dy <= 1) {
            return true;
        }
        
        // For now, use a simple heuristic: if the NPC is within reasonable range
        // and not too far diagonally, assume we can shoot over low obstacles
        int distance = from.distanceTo(to);
        
        // Allow shots over fences/low obstacles (reasonable range)
        if (distance <= 8) {
            return true;
        }
        
        // For longer distances, be more conservative
        return distance <= 10 && (dx <= 6 || dy <= 6);
    }

    private int getTimeoutForCombatStyle(CombatStyle combatStyle) {
        switch (combatStyle) {
            case MELEE:
                return 15; // Longer timeout for melee since NPCs need to path to us
            case RANGED:
            case MAGIC:
                return 8; // Shorter timeout for ranged/magic since they're instant
            default:
                return COMBAT_TIMEOUT_SECONDS;
        }
    }

    private void performHighAlchemy() {
        // Check inventory for items that meet alchemy criteria
        Rs2Inventory.all().stream()
            .filter(item -> {
                int geValue = Microbot.getItemManager().getItemPrice(item.getId());
                int haValue = item.getHaPrice();
                return geValue >= config.minimumItemValue() && 
                       (haValue - geValue) >= config.alchemyProfitThreshold();
            })
            .findFirst()
            .ifPresent(item -> {
                Rs2Magic.alch(item.getName());
                Global.sleep(1200, 1800);
            });
    }

    private boolean isActivelyAttacking() {
        // Check if we're actively in combat and can still attack our target
        if (!Rs2Combat.inCombat()) {
            return false;
        }
        
        CombatStyle combatStyle = getCurrentCombatStyle();
        
        // Check if we have a valid target that we can still attack based on combat style
        return Rs2Npc.getNpcsForPlayer()
            .filter(npc -> npc.isInteracting() && npc.getInteracting() == Microbot.getClient().getLocalPlayer())
            .anyMatch(npc -> isNpcAttackable(npc, combatStyle));
    }

    private boolean lootReachableItemBasedOnValue(int minimumValue, int radius) {
        // Find valuable items and check if they're reachable before attempting to loot
        RS2Item[] allItems = Rs2GroundItem.getAll(radius);
        if (allItems == null || allItems.length == 0) {
            return false;
        }
        
        for (RS2Item item : allItems) {
            if (item != null) {
                // Get item value from ItemComposition
                int itemValue = item.getItem().getHaPrice() * item.getTileItem().getQuantity();
                if (itemValue >= minimumValue) {
                    WorldPoint itemLocation = item.getTile().getWorldLocation();
                    
                    // Check if the item location is reachable
                    if (Rs2Walker.canReach(itemLocation)) {
                        // Item is reachable, try to loot it
                        String itemName = item.getItem().getName();
                        if (Rs2GroundItem.loot(itemName, radius)) {
                            return true;
                        }
                    } else {
                        log.error("Skipping valuable item '{}' ({}gp) - not reachable", 
                                item.getItem().getName(), itemValue);
                    }
                }
            }
        }
        return false;
    }

    private boolean lootReachableItem(String itemName, int radius) {
        // Find specific items and check if they're reachable before attempting to loot
        RS2Item[] allItems = Rs2GroundItem.getAll(radius);
        if (allItems == null || allItems.length == 0) {
            return false;
        }
        
        for (RS2Item item : allItems) {
            if (item != null) {
                String currentItemName = item.getItem().getName();
                if (currentItemName.toLowerCase().contains(itemName.toLowerCase())) {
                    WorldPoint itemLocation = item.getTile().getWorldLocation();
                    
                    // Check if the item location is reachable
                    if (Rs2Walker.canReach(itemLocation)) {
                        // Item is reachable, try to loot it
                        if (Rs2GroundItem.loot(currentItemName, radius)) {
                            return true;
                        }
                    } else {
                        log.error("Skipping specified item '{}' - not reachable", currentItemName);
                    }
                }
            }
        }
        return false;
    }

    private boolean needsSupplies() {
        // Get the current inventory setup to understand what supplies we actually need
        InventorySetup currentSetup = null;
        if (currentTaskEnum != null) {
            currentSetup = getInventorySetup(currentTaskEnum);
        }
        
        // Check for special slayer items first
        if (needsSpecialSlayerItems()) {
            log.error("Missing special slayer items - returning to bank");
            return true;
        }
        log.error("Not missing special slayer items");
        
        // Check for desert survival items
        if (needsDesertSurvivalItems()) {
            log.error("Missing desert survival items - returning to bank");
            return true;
        }
        log.error("Not missing desert survival items");
        
        // Check for waterskin refill during desert tasks
        if (needsWaterskinRefill()) {
            log.error("Need to refill waterskins - returning to bank");
            return true;
        }
        log.error("Not need to refill waterskins");
        
        // Check if inventory is getting full and we might not have space for important items
        if (Rs2Inventory.isFull() && config.lootItems()) {
            log.info("Inventory full - returning to bank");
            return true;
        }

        // Check ammunition (arrows/bolts) if threshold is configured AND we use ranged in our setup
        if (config.minimumAmmoCount() > 0 && usesAmmunitionInSetup(currentSetup)) {
            int totalAmmo = countAmmunition();
            if (totalAmmo < config.minimumAmmoCount()) {
                log.error("Low ammunition: {} remaining - returning to bank", totalAmmo);
                return true;
            }
        }

        // Check magic runes if threshold is configured AND we use magic in our setup
        if (config.minimumAmmoCount() > 0 && usesMagicInSetup(currentSetup)) {
            if (!hasBasicMagicRequirements()) {
                log.error("Insufficient magic runes - returning to bank");
                return true;
            }
        }

        // Check cannonballs for cannon tasks if threshold is configured AND we use cannon
        boolean usesCannon = currentTaskEnum != null && shouldUseCannon(currentTaskEnum);
        if (config.minimumCannonballCount() > 0 && usesCannon) {
            int cannonballs = Rs2Inventory.count("cannonball");
            if (cannonballs < config.minimumCannonballCount()) {
                log.error("Low cannonballs: {} remaining - returning to bank", cannonballs);
                return true;
            }
        }

        // Check food if threshold is configured AND we use food in our setup
        if (config.minimumFoodCount() > 0 && usesFoodInSetup(currentSetup)) {
            int totalFood = countFood();
            if (totalFood < config.minimumFoodCount()) {
                log.error("Low food: {} remaining - returning to bank", totalFood);
                return true;
            }
        }

        // Check prayer potions if threshold is configured AND we use prayer potions in our setup
        if (config.minimumPrayerPotionCount() > 0 && usesPrayerPotionsInSetup(currentSetup)) {
            int totalPrayerPots = countPrayerPotions();
            if (totalPrayerPots < config.minimumPrayerPotionCount()) {
                log.error("Low prayer potions: {} remaining - returning to bank", totalPrayerPots);
                return true;
            }
        }

        return false;
    }

    private int countAmmunition() {
        // Count common arrows and bolts
        String[] ammunition = {
            // Arrows
            "bronze arrow", "iron arrow", "steel arrow", "mithril arrow", "adamant arrow", "rune arrow", "dragon arrow",
            "bronze arrow(p)", "iron arrow(p)", "steel arrow(p)", "mithril arrow(p)", "adamant arrow(p)", "rune arrow(p)",
            "bronze arrow(p+)", "iron arrow(p+)", "steel arrow(p+)", "mithril arrow(p+)", "adamant arrow(p+)", "rune arrow(p+)",
            "bronze arrow(p++)", "iron arrow(p++)", "steel arrow(p++)", "mithril arrow(p++)", "adamant arrow(p++)", "rune arrow(p++)",
            // Bolts
            "bronze bolts", "iron bolts", "steel bolts", "mithril bolts", "adamant bolts", "runite bolts", "dragon bolts",
            "bronze bolts(p)", "iron bolts(p)", "steel bolts(p)", "mithril bolts(p)", "adamant bolts(p)", "runite bolts(p)", "dragon bolts(p)",
            "bronze bolts(p+)", "iron bolts(p+)", "steel bolts(p+)", "mithril bolts(p+)", "adamant bolts(p+)", "runite bolts(p+)", "dragon bolts(p+)",
            "bronze bolts(p++)", "iron bolts(p++)", "steel bolts(p++)", "mithril bolts(p++)", "adamant bolts(p++)", "runite bolts(p++)", "dragon bolts(p++)",
            // Special bolts
            "broad bolts", "bone bolts", "pearl bolts", "silver bolts"
        };
        
        int total = 0;
        for (String ammo : ammunition) {
            total += Rs2Inventory.count(ammo);
        }
        return total;
    }

    private int countFood() {
        // Count common food items
        String[] commonFood = {
            "lobster", "swordfish", "monkfish", "shark", "karambwan", "manta ray", "anglerfish",
            "sea turtle", "tuna potato", "dark crab", "cooked karambwan", "purple sweets"
        };
        
        int total = 0;
        for (String food : commonFood) {
            total += Rs2Inventory.count(food);
        }
        return total;
    }

    private int countPrayerPotions() {
        // Count common prayer restoration potions
        String[] prayerPots = {
            "prayer potion(1)", "prayer potion(2)", "prayer potion(3)", "prayer potion(4)",
            "super restore(1)", "super restore(2)", "super restore(3)", "super restore(4)",
            "sanfew serum(1)", "sanfew serum(2)", "sanfew serum(3)", "sanfew serum(4)"
        };
        
        int total = 0;
        for (String pot : prayerPots) {
            total += Rs2Inventory.count(pot);
        }
        return total;
    }

    private boolean hasBasicMagicRequirements() {
        // Check for basic combat runes - at minimum we need some elemental runes
        // This is a lenient check - just ensuring we have SOME runes for magic combat
        
        // Check for elemental runes (for basic combat spells)
        boolean hasElementalRunes = Rs2Inventory.hasItem("air rune") || 
                                  Rs2Inventory.hasItem("water rune") ||
                                  Rs2Inventory.hasItem("earth rune") || 
                                  Rs2Inventory.hasItem("fire rune") ||
                                  Rs2Inventory.hasItem("mind rune") ||
                                  Rs2Inventory.hasItem("chaos rune") ||
                                  Rs2Inventory.hasItem("death rune") ||
                                  Rs2Inventory.hasItem("blood rune");
        
        // Check for combination runes that provide multiple elements
        boolean hasCombinationRunes = Rs2Inventory.hasItem("lava rune") ||
                                    Rs2Inventory.hasItem("steam rune") ||
                                    Rs2Inventory.hasItem("dust rune") ||
                                    Rs2Inventory.hasItem("smoke rune") ||
                                    Rs2Inventory.hasItem("mud rune") ||
                                    Rs2Inventory.hasItem("mist rune");
        
        // Check for special magic weapons that don't require runes
        boolean hasBuiltInSpells = false;
        if (Rs2Equipment.isWearing(EquipmentInventorySlot.WEAPON)) {
            String weaponName = Rs2Equipment.get(EquipmentInventorySlot.WEAPON).getName().toLowerCase();
            hasBuiltInSpells = weaponName.contains("trident") || 
                              weaponName.contains("sang") ||
                              weaponName.contains("crystal staff") ||
                              weaponName.contains("corrupted staff");
        }
        
        return hasElementalRunes || hasCombinationRunes || hasBuiltInSpells;
    }

    private SlayerBotState determineResumeState() {
        // Priority order for state detection:
        // 1. If we're already at the task location with supplies -> COMBAT
        // 2. If we have task-appropriate gear but need supplies -> BANKING
        // 3. If we don't have appropriate gear -> BANKING
        // 4. If we're far from task location but have gear/supplies -> TRAVELING_TO_LOCATION
        
        try {
            // Check if we have slayer monsters for this task
            List<String> slayerMonsters = Rs2Slayer.getSlayerMonsters();
            
            if (slayerMonsters == null || slayerMonsters.isEmpty()) {
                log.warn("No slayer monsters found for task: {}", currentTask);
                return SlayerBotState.BANKING;
            }
            
            MonsterLocation taskLocation = Rs2Slayer.getSlayerTaskLocation();
            
            if (taskLocation != null) {
                WorldPoint taskWorldPoint = taskLocation.getClosestToCenter();
                
                if (taskWorldPoint != null) {
                    int distanceToTask = Rs2Player.getWorldLocation().distanceTo(taskWorldPoint);
                    
                    // Check if we're already at or near the task location (within 15 tiles)
                    if (distanceToTask <= 15) {
                        // Check if we have adequate supplies
                        if (!needsSupplies()) {
                            log.error("At task location with supplies - resuming combat");
                            return SlayerBotState.COMBAT;
                        } else {
                            log.error("At task location but low on supplies - banking");
                            return SlayerBotState.BANKING;
                        }
                    } else {
                        // Check if we have the right gear for this task
                        if (hasTaskAppropriateGear()) {
                            // Check if we have adequate supplies
                            if (!needsSupplies()) {
                                log.error("Have gear and supplies - traveling to task location");
                                return SlayerBotState.TRAVELING_TO_LOCATION;
                            } else {
                                log.error("Have gear but low on supplies - banking");
                                return SlayerBotState.BANKING;
                            }
                        } else {
                            log.error("Missing appropriate gear - banking");
                            return SlayerBotState.BANKING;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error determining task location, defaulting to banking", e);
        }
        
        // Default fallback - go to banking to ensure we have the right setup
        return SlayerBotState.BANKING;
    }

    private boolean hasTaskAppropriateGear() {
        // Basic gear validation - check if we have reasonable gear to continue
        
        // Check if we have a weapon equipped
        if (!Rs2Equipment.isWearing(EquipmentInventorySlot.WEAPON)) {
            return false;
        }
        
        // Check for special task requirements (these are critical)
        if (currentTaskEnum != null) {
            switch (currentTaskEnum) {
                case GARGOYLES:
                    if (!Rs2Inventory.hasItem("rock hammer") && !Rs2Equipment.isWearing("rock hammer")) {
                        return false;
                    }
                    break;
                case LIZARDS:
                    if (!Rs2Inventory.hasItem("ice cooler")) {
                        return false;
                    }
                    break;
                case DUST_DEVILS:
                case ABERRANT_SPECTRES:
                    // Check for face mask or slayer helmet
                    if (!Rs2Equipment.isWearing("face mask") && 
                        !Rs2Equipment.isWearing("slayer helmet") &&
                        !Rs2Equipment.isWearing("slayer helmet (i)")) {
                        return false;
                    }
                    break;
            }
        }
        
        // For ranged/magic weapons, check if we have appropriate ammunition/runes
        CombatStyle combatStyle = getCurrentCombatStyle();
        if (combatStyle == CombatStyle.RANGED) {
                int totalAmmo = countAmmunition();
            // Only require ammunition if we have ZERO - having some is enough for gear check
                if (totalAmmo <= 0) {
                    return false;
                }
        } else if (combatStyle == CombatStyle.MAGIC) {
            // For magic, check if we have basic runes for combat spells
            if (!hasBasicMagicRequirements()) {
                return false;
            }
        }
        
        // If we made it here, we have basic appropriate gear
        return true;
    }

    private SlayerTaskEnum.TaskAction getTaskAction(SlayerTaskEnum taskEnum) {
        // This method should map the config values to the task actions
        // For now, returning KILL as default, but this should be expanded
        // to read the specific config values for each task
        switch (taskEnum) {
            case ABERRANT_SPECTRES:
                return config.aberrantSpectresAction();
            case ABYSSAL_DEMONS:
                return config.abyssalDemonsAction();
            case ANKOU:
                return config.ankouAction();
            case BLACK_DEMONS:
                return config.blackDemonsAction();
            case BLOODVELD:
                return config.bloodveldAction();
            case GARGOYLES:
                return config.gargoylesAction();
            case DUST_DEVILS:
                return config.dustDevilsAction();
            case NECHRYAEL:
                return config.nechryaelAction();
            default:
                return SlayerTaskEnum.TaskAction.KILL;
        }
    }

    private InventorySetup getInventorySetup(SlayerTaskEnum taskEnum) {
        log.error("Getting inventory setup for task enum: {}", taskEnum);
        switch (taskEnum) {
            case ABERRANT_SPECTRES:
                return config.aberrantSpectresSetup();
            case ABYSSAL_DEMONS:
                return config.abyssalDemonsSetup();
            case ADAMANT_DRAGONS:
                return config.adamantDragonsSetup();
            case ANKOU:
                return config.ankouSetup();
            case AVIANSIES:
                return config.aviansiesSetup();
            case BANDITS:
                return config.banditsSetup();
            case BANSHEES:
                return config.bansheesSetup();
            case BASILISKS:
                return config.basilisksSetup();
            case BLACK_DEMONS:
                return config.blackDemonsSetup();
            case BLACK_DRAGONS:
                return config.blackDragonsSetup();
            case BLOODVELD:
                return config.bloodveldSetup();
            case BLUE_DRAGONS:
                return config.blueDragonsSetup();
            case BRINE_RATS:
                return config.brineRatsSetup();
            case CAVE_BUGS:
                return config.caveBugsSetup();
            case CAVE_CRAWLERS:
                return config.caveCrawlersSetup();
            case CAVE_HORRORS:
                return config.caveHorrorsSetup();
            case CAVE_KRAKEN:
                return config.caveKrakenSetup();
            case CAVE_SLIMES:
                return config.caveSlimesSetup();
            case COCKATRICE:
                return config.cockatriceSetup();
            case CRAWLING_HANDS:
                return config.crawlingHandsSetup();
            case CROCODILES:
                String setupName = config.crocodilesSetup();
                log.error("Direct config call crocodilesSetup() returned: '{}'", setupName);
                return findInventorySetupByName(setupName);
            case DAGANNOTH:
                return config.dagannothSetup();
            case DARK_BEASTS:
                return config.darkBeastsSetup();
            case DUST_DEVILS:
                return config.dustDevilsSetup();
            case EARTH_WARRIORS:
                return config.earthWarriorsSetup();
            case FIRE_GIANTS:
                return config.fireGiantsSetup();
            case FOSSIL_ISLAND_WYVERNS:
                return config.fossilIslandWyvernsSetup();
            case GARGOYLES:
                return config.gargoylesSetup();
            case GREATER_DEMONS:
                return config.greaterDemonsSetup();
            case GREEN_DRAGONS:
                return config.greenDragonsSetup();
            case HELLHOUNDS:
                return config.hellhoundsSetup();
            case HILL_GIANTS:
                return config.hillGiantsSetup();
            case HOBGOBLINS:
                return config.hobgoblinsSetup();
            case ICEFIENDS:
                return config.icefiendsSetup();
            case ICE_GIANTS:
                return config.iceGiantsSetup();
            case ICE_WARRIORS:
                return config.iceWarriorsSetup();
            case INFERNAL_MAGES:
                return config.infernalMagesSetup();
            case JELLIES:
                return config.jelliesSetup();
            case JUNGLE_HORROR:
                return config.jungleHorrorsSetup();
            case KALPHITE:
                return config.kalphiteSetup();
            case KURASK:
                return config.kuraskSetup();
            case LESSER_DEMONS:
                return config.lesserDemonsSetup();
            case LIZARDMEN:
                return config.lizardmenSetup();
            case LIZARDS:
                return config.lizardsSetup();
            case MAGIC_AXES:
                return config.magicAxesSetup();
            case METAL_DRAGONS:
                return config.metalDragonsSetup();
            case MINOTAURS:
                return config.minotaursSetup();
            case MOGRES:
                return config.mogresSetup();
            case MOSS_GIANTS:
                return config.mossGiantsSetup();
            case MUTATED_ZYGOMITES:
                return config.mutatedZygomitesSetup();
            case NECHRYAEL:
                return config.nechryaelSetup();
            case OGRES:
                return config.ogresSetup();
            case PYREFIENDS:
                return config.pyrefiendsSetup();
            case RED_DRAGONS:
                return config.redDragonsSetup();
            case ROCKSLUGS:
                return config.rockslugsSetup();
            case SKELETAL_WYVERNS:
                return config.skeletalWyvernsSetup();
            case SMOKE_DEVILS:
                return config.smokeDevilsSetup();
            case SPIRITUAL_CREATURES:
                return config.spiritualCreaturesSetup();
            case SUQAHS:
                return config.suqahsSetup();
            case TERROR_DOGS:
                return config.terrorDogsSetup();
            case TROLLS:
                return config.trollsSetup();
            case TUROTH:
                return config.turothSetup();
            case TZHAAR:
                return config.tzhaarSetup();
            case VAMPYRES:
                return config.vampyresSetup();
            case WALL_BEASTS:
                return config.wallBeastsSetup();
            case WATERFIENDS:
                return config.waterfiendsSetup();
            case WEREWOLVES:
                return config.werewolvesSetup();
            case WYRMS:
                return config.wyrmsSetup();
            default:
                return null;
        }
    }

    private boolean shouldUseCannon(SlayerTaskEnum taskEnum) {
        if (taskEnum == null || !taskEnum.isCannonCompatible()) {
            return false;
        }

        switch (taskEnum) {
            case ABERRANT_SPECTRES:
                return config.aberrantSpectresCannon();
            case ANKOU:
                return config.ankouCannon();
            case AVIANSIES:
                return config.aviansiesCannon();
            case BANDITS:
                return config.banditsCannon();
            case BANSHEES:
                return config.bansheesCannon();
            case BLACK_DEMONS:
                return config.blackDemonsCannon();
            case BLOODVELD:
                return config.bloodveldCannon();
            case BLUE_DRAGONS:
                return config.blueDragonsCannon();
            case CRAWLING_HANDS:
                return config.crawlingHandsCannon();
            case DAGANNOTH:
                return config.dagannothCannon();
            case DARK_BEASTS:
                return config.darkBeastsCannon();
            case DUST_DEVILS:
                return config.dustDevilsCannon();
            case EARTH_WARRIORS:
                return config.earthWarriorsCannon();
            case FIRE_GIANTS:
                return config.fireGiantsCannon();
            case GREATER_DEMONS:
                return config.greaterDemonsCannon();
            case GREEN_DRAGONS:
                return config.greenDragonsCannon();
            case HELLHOUNDS:
                return config.hellhoundsCannon();
            case HILL_GIANTS:
                return config.hillGiantsCannon();
            case HOBGOBLINS:
                return config.hobgoblinsCannon();
            case KALPHITE:
                return config.kalphiteCannon();
            case LESSER_DEMONS:
                return config.lesserDemonsCannon();
            case LIZARDMEN:
                return config.lizardmenCannon();
            case MOSS_GIANTS:
                return config.mossGiantsCannon();
            case NECHRYAEL:
                return config.nechryaelCannon();
            case OGRES:
                return config.ogresCannon();
            case TROLLS:
                return config.trollsCannon();
            default:
                return false;
        }
    }

    private InventorySetup findInventorySetupByName(String setupName) {
        if (setupName == null || setupName.trim().isEmpty()) {
            return null;
        }
        
        if (MInventorySetupsPlugin.getInventorySetups() == null) {
            return null;
        }

        return MInventorySetupsPlugin.getInventorySetups().stream()
            .filter(setup -> setup.getName().equalsIgnoreCase(setupName.trim()))
            .findFirst()
            .orElse(null);
    }

    private WorldPoint findCannonSpot(WorldPoint center) {
        // Simple implementation - find a walkable tile near the center
        // This should be improved to find optimal cannon placement
        for (int x = -3; x <= 3; x++) {
            for (int y = -3; y <= 3; y++) {
                WorldPoint candidate = new WorldPoint(
                    center.getX() + x, 
                    center.getY() + y, 
                    center.getPlane()
                );
                // TODO: Add proper tile walkability and cannon placement checks
                if (Rs2Walker.canReach(candidate)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private boolean usesAmmunitionInSetup(InventorySetup setup) {
        if (setup == null) {
            // If no setup, check if we're using ranged weapon
            if (Rs2Equipment.isWearing(EquipmentInventorySlot.WEAPON)) {
                String weaponName = Rs2Equipment.get(EquipmentInventorySlot.WEAPON).getName().toLowerCase();
                return weaponName.contains("bow") || weaponName.contains("crossbow");
            }
            return false;
        }
        
        // Check if the setup contains arrows, bolts, or other ammunition
        return setup.getInventory().stream().anyMatch(item -> {
            if (item == null || item.getName() == null) return false;
            String name = item.getName().toLowerCase();
            return name.contains("arrow") || name.contains("bolt") || name.contains("dart") || 
                   name.contains("javelin") || name.contains("throwing");
        });
    }

    private boolean usesFoodInSetup(InventorySetup setup) {
        if (setup == null) {
            return false; // If no setup configured, don't require food
        }
        
        // Check if the setup contains common food items
        return setup.getInventory().stream().anyMatch(item -> {
            if (item == null || item.getName() == null) return false;
            String name = item.getName().toLowerCase();
            return name.contains("shark") || name.contains("lobster") || name.contains("swordfish") ||
                   name.contains("monkfish") || name.contains("karambwan") || name.contains("manta ray") ||
                   name.contains("anglerfish") || name.contains("tuna") || name.contains("salmon") ||
                   name.contains("sea turtle") || name.contains("dark crab") || name.contains("purple sweets");
        });
    }

    private boolean usesPrayerPotionsInSetup(InventorySetup setup) {
        if (setup == null) {
            return false; // If no setup configured, don't require prayer potions
        }
        
        // Check if the setup contains prayer restoration items
        return setup.getInventory().stream().anyMatch(item -> {
            if (item == null || item.getName() == null) return false;
            String name = item.getName().toLowerCase();
            return name.contains("prayer potion") || name.contains("super restore") || 
                   name.contains("sanfew serum") || name.contains("prayer mix");
        });
    }

    private boolean usesMagicInSetup(InventorySetup setup) {
        if (setup == null) {
            // If no setup, check if we're using magic weapon
            return getCurrentCombatStyle() == CombatStyle.MAGIC;
        }
        
        // Check if the setup contains magic runes or magic weapons
        return setup.getInventory().stream().anyMatch(item -> {
            if (item == null || item.getName() == null) return false;
            String name = item.getName().toLowerCase();
            return name.contains("rune") || name.contains("staff") || name.contains("wand") ||
                   name.contains("tome") || name.contains("trident") || name.contains("sceptre");
        });
    }

    public void onChatMessage(ChatMessage event) {
        if (event.getType() != ChatMessageType.GAMEMESSAGE) {
            return;
        }

        String message = event.getMessage();
        
        // Handle task completion messages
        if (message.contains("You have completed your task")) {
            botState = SlayerBotState.TASK_COMPLETE;
        }
        
        // Handle level up messages
        if (message.contains("Your Slayer level is now")) {
            log.error("Slayer level up!");
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
        Rs2Antiban.resetAntibanSettings();
        botState = SlayerBotState.STOPPED;
    }
}
