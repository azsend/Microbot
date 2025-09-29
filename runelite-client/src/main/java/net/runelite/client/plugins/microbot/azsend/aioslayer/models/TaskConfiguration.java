package net.runelite.client.plugins.microbot.azsend.aioslayer.models;

import lombok.Builder;
import lombok.Data;
import net.runelite.api.ItemID;
import net.runelite.client.plugins.microbot.util.skills.slayer.enums.ProtectiveEquipment;
import net.runelite.client.plugins.microbot.util.skills.slayer.enums.SlayerTaskMonster;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class TaskConfiguration {
    
    // Basic task info
    private String taskName;
    private SlayerTaskMonster monsterInfo;
    
    // Combat configuration
    private boolean cannonCompatible;
    private boolean requiresSpecialKill;
    private boolean requiresRangedOnly;
    private boolean requiresMeleeOnly;
    private boolean requiresMagicOnly;
    private boolean requiresPrayerPotions;
    private boolean requiresBankingBefore;
    
    // Equipment requirements
    private List<Integer> requiredEquipment;
    private List<Integer> protectiveEquipment;
    private Integer protectiveEquipmentId; // Single protective equipment item
    private Integer preferredHelmet; // Slayer helmet > specific protection
    private List<Integer> finishingBlowItems;
    private Integer finishingBlowThreshold;
    
    // Inventory overrides
    private List<RequiredItem> extraInventoryItems;
    private List<RequiredItem> replacementItems; // Items to replace in standard setup
    private Integer minFreeSpaces; // For cannon pickup, loot, etc.
    
    // Location and travel
    private boolean isWilderness;
    private boolean isMultiCombat;
    private boolean requiresDesertGear;
    private List<Integer> travelItems;
    
    // Special requirements
    private boolean requiresLightSource;
    private boolean requiresAntiPoison;
    private boolean requiresAntifire;
    private boolean requiresSuperAntifire;
    private boolean requiresStaminaPotions;
    
    // Food and consumables
    private String preferredFood;
    private Integer foodAmount;
    private List<String> alternativeFood;
    
    // Banking and shopping
    private List<Integer> shopItems; // Items to buy from slayer master if missing
    private boolean requiresSpecialBanking;
    
    @Data
    @Builder
    public static class RequiredItem {
        private Integer itemId;
        private String itemName;
        private Integer quantity;
        private boolean consumable;
        private boolean stackable;
        private boolean equipable;
        private Integer slot; // Equipment slot if equipable
    }
    
    /**
     * Creates a default configuration for any task
     */
    public static TaskConfiguration createDefault(String taskName) {
        return TaskConfiguration.builder()
                .taskName(taskName)
                .cannonCompatible(false)
                .requiresSpecialKill(false)
                .requiresRangedOnly(false)
                .requiresMeleeOnly(false)
                .requiresMagicOnly(false)
                .isWilderness(false)
                .isMultiCombat(false)
                .requiresDesertGear(false)
                .requiresLightSource(false)
                .requiresAntiPoison(false)
                .requiresAntifire(false)
                .requiresSuperAntifire(false)
                .requiresPrayerPotions(false)
                .requiresStaminaPotions(false)
                .requiresBankingBefore(false)
                .requiresSpecialBanking(false)
                .minFreeSpaces(4) // Default for cannon pickup
                .build();
    }
    
    /**
     * Gets the protective equipment item ID for this task
     */
    public Integer getProtectiveEquipmentId() {
        if (monsterInfo == null) return null;
        
        String protectiveItem = ProtectiveEquipment.getItemNameByCreature(taskName);
        if (protectiveItem == null) return null;
        
        // Map protective equipment names to item IDs
        switch (protectiveItem.toLowerCase()) {
            case "slayer helmet":
                return 11864; // SLAYER_HELMET
            case "slayer helmet (i)":
                return 11865; // SLAYER_HELMET_I
            case "facemask":
                return 4164; // FACEMASK
            case "earmuffs":
                return 4166; // EARMUFFS
            case "nose peg":
                return 4168; // NOSE_PEG
            case "mirror shield":
                return 4156; // MIRROR_SHIELD
            case "witchwood icon":
                return 9736; // WITCHWOOD_ICON
            case "insulated boots":
                return 6145; // INSULATED_BOOTS
            case "slayer gloves":
                return 6020; // SLAYER_GLOVES
            case "spiny helmet":
                return 4551; // SPINY_HELMET
            case "reinforced goggles":
                return 4164; // REINFORCED_GOGGLES
            default:
                return null;
        }
    }
    
    /**
     * Checks if this task requires a specific item to be worn/carried
     */
    public boolean requiresItem(int itemId) {
        if (requiredEquipment != null && requiredEquipment.contains(itemId)) return true;
        if (protectiveEquipment != null && protectiveEquipment.contains(itemId)) return true;
        if (finishingBlowItems != null && finishingBlowItems.contains(itemId)) return true;
        if (travelItems != null && travelItems.contains(itemId)) return true;
        
        return extraInventoryItems != null && 
               extraInventoryItems.stream().anyMatch(item -> item.getItemId().equals(itemId));
    }
}
