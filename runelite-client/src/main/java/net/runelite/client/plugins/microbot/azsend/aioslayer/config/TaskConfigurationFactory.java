package net.runelite.client.plugins.microbot.azsend.aioslayer.config;

import net.runelite.api.ItemID;
import net.runelite.client.plugins.microbot.azsend.aioslayer.models.TaskConfiguration;
import net.runelite.client.plugins.microbot.util.skills.slayer.enums.SlayerTaskMonster;

import java.util.Arrays;
import java.util.List;

/**
 * Factory class for creating task-specific configurations
 */
public class TaskConfigurationFactory {
    
    /**
     * Creates a configuration for the specified task
     */
    public static TaskConfiguration createConfiguration(String taskName) {
        if (taskName == null) {
            return TaskConfiguration.createDefault("Unknown");
        }
        
        String normalizedName = taskName.toLowerCase().trim();
        
        // Handle specific task configurations
        switch (normalizedName) {
            case "aberrant spectres":
            case "aberrant spectre":
                return createAberrantSpectreConfig();
            case "banshees":
            case "banshee":
                return createBansheeConfig();
            case "dust devils":
            case "dust devil":
                return createDustDevilConfig();
            case "gargoyles":
            case "gargoyle":
                return createGargoylesConfig();
            case "lizards":
            case "lizard":
                return createLizardConfig();
            case "rockslugs":
            case "rockslug":
                return createRockslugConfig();
            case "mutated zygomites":
            case "zygomites":
            case "zygomite":
                return createZygomiteConfig();
            case "cave horrors":
            case "cave horror":
                return createCaveHorrorConfig();
            case "wall beasts":
            case "wall beast":
                return createWallBeastConfig();
            case "fever spiders":
            case "fever spider":
                return createFeverSpiderConfig();
            case "basilisks":
            case "basilisk":
                return createBasiliskConfig();
            case "cockatrice":
                return createCockatriceConfig();
            case "killerwatts":
            case "killerwatt":
                return createKillerwattConfig();
            case "sourhogs":
            case "sourhog":
                return createSourhogConfig();
            case "black dragons":
            case "black dragon":
            case "blue dragons":
            case "blue dragon":
            case "green dragons":
            case "green dragon":
            case "red dragons":
            case "red dragon":
                return createDragonConfig(normalizedName);
            case "skeletal wyverns":
            case "skeletal wyvern":
            case "fossil island wyverns":
                return createWyvernConfig();
            case "rune dragons":
            case "rune dragon":
                return createRuneDragonConfig();
            case "smoke devils":
            case "smoke devil":
                return createSmokeDevilConfig();
            case "crocodiles":
            case "crocodile":
                return createCrocodileConfig();
            case "kalphite":
                return createKalphiteConfig();
            case "wolves":
            case "wolf":
                return createWolvesConfig();
            default:
                return createGenericConfig(taskName);
        }
    }
    
    private static TaskConfiguration createAberrantSpectreConfig() {
        return TaskConfiguration.builder()
                .taskName("Aberrant spectres")
                .monsterInfo(SlayerTaskMonster.ABERRANT_SPECTRE)
                .cannonCompatible(true)
                .requiresSpecialKill(false)
                .protectiveEquipment(Arrays.asList(ItemID.SLAYER_HELMET, ItemID.NOSE_PEG))
                .preferredHelmet(ItemID.SLAYER_HELMET)
                .requiresPrayerPotions(true)
                .build();
    }
    
    private static TaskConfiguration createBansheeConfig() {
        return TaskConfiguration.builder()
                .taskName("Banshees")
                .monsterInfo(SlayerTaskMonster.BANSHEE)
                .cannonCompatible(true)
                .requiresSpecialKill(false)
                .protectiveEquipment(Arrays.asList(ItemID.SLAYER_HELMET, ItemID.EARMUFFS))
                .preferredHelmet(ItemID.SLAYER_HELMET)
                .build();
    }
    
    private static TaskConfiguration createDustDevilConfig() {
        return TaskConfiguration.builder()
                .taskName("Dust devils")
                .monsterInfo(SlayerTaskMonster.DUST_DEVIL)
                .cannonCompatible(true)
                .requiresSpecialKill(false)
                .protectiveEquipment(Arrays.asList(ItemID.SLAYER_HELMET, ItemID.FACEMASK))
                .preferredHelmet(ItemID.SLAYER_HELMET)
                .build();
    }
    
    private static TaskConfiguration createGargoylesConfig() {
        return TaskConfiguration.builder()
                .taskName("Gargoyles")
                .monsterInfo(SlayerTaskMonster.GARGOYLE)
                .cannonCompatible(false)
                .requiresSpecialKill(true)
                .finishingBlowItems(Arrays.asList(ItemID.ROCK_HAMMER, ItemID.GRANITE_HAMMER))
                .finishingBlowThreshold(10) // Use hammer when HP <= 10
                .build();
    }
    
    private static TaskConfiguration createLizardConfig() {
        return TaskConfiguration.builder()
                .taskName("Lizards")
                .monsterInfo(SlayerTaskMonster.getMonsterByName("Lizard"))
                .cannonCompatible(false)
                .requiresSpecialKill(true)
                .requiresDesertGear(true)
                .finishingBlowItems(Arrays.asList(ItemID.ICE_COOLER))
                .finishingBlowThreshold(1) // Use ice cooler when HP <= 1
                .extraInventoryItems(Arrays.asList(
                    TaskConfiguration.RequiredItem.builder()
                        .itemId(ItemID.WATERSKIN4)
                        .itemName("Waterskin(4)")
                        .quantity(3)
                        .consumable(true)
                        .build()
                ))
                .build();
    }
    
    private static TaskConfiguration createRockslugConfig() {
        return TaskConfiguration.builder()
                .taskName("Rockslugs")
                .monsterInfo(SlayerTaskMonster.getMonsterByName("Rockslug"))
                .cannonCompatible(false)
                .requiresSpecialKill(true)
                .finishingBlowItems(Arrays.asList(ItemID.BAG_OF_SALT))
                .finishingBlowThreshold(5) // Use salt when HP <= 5
                .build();
    }
    
    private static TaskConfiguration createZygomiteConfig() {
        return TaskConfiguration.builder()
                .taskName("Mutated zygomites")
                .monsterInfo(SlayerTaskMonster.ZYGOMITE)
                .cannonCompatible(false)
                .requiresSpecialKill(true)
                .finishingBlowItems(Arrays.asList(ItemID.FUNGICIDE_SPRAY_10, ItemID.FUNGICIDE))
                .finishingBlowThreshold(10) // Use fungicide when HP <= 10
                .build();
    }
    
    private static TaskConfiguration createCaveHorrorConfig() {
        return TaskConfiguration.builder()
                .taskName("Cave horrors")
                .monsterInfo(SlayerTaskMonster.CAVE_HORROR)
                .cannonCompatible(false)
                .requiresSpecialKill(false)
                .requiresLightSource(true)
                .protectiveEquipment(Arrays.asList(ItemID.WITCHWOOD_ICON))
                .extraInventoryItems(Arrays.asList(
                    TaskConfiguration.RequiredItem.builder()
                        .itemId(ItemID.BULLSEYE_LANTERN)
                        .itemName("Bullseye lantern")
                        .quantity(1)
                        .equipable(true)
                        .build()
                ))
                .build();
    }
    
    private static TaskConfiguration createWallBeastConfig() {
        return TaskConfiguration.builder()
                .taskName("Wall beasts")
                .monsterInfo(SlayerTaskMonster.WALL_BEAST)
                .cannonCompatible(false)
                .requiresSpecialKill(false)
                .protectiveEquipment(Arrays.asList(ItemID.SLAYER_HELMET, ItemID.SPINY_HELMET))
                .preferredHelmet(ItemID.SLAYER_HELMET)
                .build();
    }
    
    private static TaskConfiguration createFeverSpiderConfig() {
        return TaskConfiguration.builder()
                .taskName("Fever spiders")
                .monsterInfo(SlayerTaskMonster.FEVER_SPIDER)
                .cannonCompatible(false)
                .requiresSpecialKill(false)
                .protectiveEquipment(Arrays.asList(ItemID.SLAYER_GLOVES))
                .build();
    }
    
    private static TaskConfiguration createBasiliskConfig() {
        return TaskConfiguration.builder()
                .taskName("Basilisks")
                .monsterInfo(SlayerTaskMonster.BASILISK)
                .cannonCompatible(true)
                .requiresSpecialKill(false)
                .protectiveEquipment(Arrays.asList(ItemID.MIRROR_SHIELD))
                .build();
    }
    
    private static TaskConfiguration createCockatriceConfig() {
        return TaskConfiguration.builder()
                .taskName("Cockatrice")
                .monsterInfo(SlayerTaskMonster.COCKATRICE)
                .cannonCompatible(false)
                .requiresSpecialKill(false)
                .protectiveEquipment(Arrays.asList(ItemID.MIRROR_SHIELD))
                .build();
    }
    
    private static TaskConfiguration createKillerwattConfig() {
        return TaskConfiguration.builder()
                .taskName("Killerwatts")
                .monsterInfo(SlayerTaskMonster.KILLERWATT)
                .cannonCompatible(false)
                .requiresSpecialKill(false)
                .protectiveEquipment(Arrays.asList(ItemID.INSULATED_BOOTS))
                .build();
    }
    
    private static TaskConfiguration createSourhogConfig() {
        return TaskConfiguration.builder()
                .taskName("Sourhogs")
                .monsterInfo(SlayerTaskMonster.SOURHOG)
                .cannonCompatible(false)
                .requiresSpecialKill(false)
                .protectiveEquipment(Arrays.asList(ItemID.SLAYER_HELMET, ItemID.REINFORCED_GOGGLES))
                .preferredHelmet(ItemID.SLAYER_HELMET)
                .build();
    }
    
    private static TaskConfiguration createDragonConfig(String dragonType) {
        TaskConfiguration.TaskConfigurationBuilder builder = TaskConfiguration.builder()
                .taskName(dragonType)
                .cannonCompatible(false)
                .requiresSpecialKill(false)
                .requiresAntifire(true)
                .extraInventoryItems(Arrays.asList(
                    TaskConfiguration.RequiredItem.builder()
                        .itemId(ItemID.ANTIFIRE_POTION4)
                        .itemName("Antifire potion(4)")
                        .quantity(2)
                        .consumable(true)
                        .build()
                ));
        
        // Add specific dragon configurations
        switch (dragonType) {
            case "black dragons":
            case "black dragon":
                builder.monsterInfo(SlayerTaskMonster.BLACK_DRAGON);
                break;
            case "blue dragons":  
            case "blue dragon":
                builder.monsterInfo(SlayerTaskMonster.BLUE_DRAGON);
                break;
            case "green dragons":
            case "green dragon":
                builder.monsterInfo(SlayerTaskMonster.GREEN_DRAGON);
                builder.isWilderness(true);
                break;
            case "red dragons":
            case "red dragon":
                builder.monsterInfo(SlayerTaskMonster.RED_DRAGON);
                break;
        }
        
        return builder.build();
    }
    
    private static TaskConfiguration createWyvernConfig() {
        return TaskConfiguration.builder()
                .taskName("Skeletal wyverns")
                .monsterInfo(SlayerTaskMonster.SKELETAL_WYVERN)
                .cannonCompatible(false)
                .requiresSpecialKill(false)
                .protectiveEquipment(Arrays.asList(ItemID.ELEMENTAL_SHIELD, ItemID.MIND_SHIELD, ItemID.DRAGONFIRE_SHIELD))
                .build();
    }
    
    private static TaskConfiguration createRuneDragonConfig() {
        return TaskConfiguration.builder()
                .taskName("Rune dragons")
                .monsterInfo(SlayerTaskMonster.RUNE_DRAGON)
                .cannonCompatible(false)
                .requiresSpecialKill(false)
                .requiresSuperAntifire(true)
                .protectiveEquipment(Arrays.asList(ItemID.INSULATED_BOOTS))
                .extraInventoryItems(Arrays.asList(
                    TaskConfiguration.RequiredItem.builder()
                        .itemId(ItemID.SUPER_ANTIFIRE_POTION4)
                        .itemName("Super antifire potion(4)")
                        .quantity(2)
                        .consumable(true)
                        .build()
                ))
                .build();
    }
    
    private static TaskConfiguration createSmokeDevilConfig() {
        return TaskConfiguration.builder()
                .taskName("Smoke devils")
                .monsterInfo(SlayerTaskMonster.SMOKE_DEVIL)
                .cannonCompatible(true)
                .requiresSpecialKill(false)
                .protectiveEquipment(Arrays.asList(ItemID.SLAYER_HELMET, ItemID.FACEMASK))
                .preferredHelmet(ItemID.SLAYER_HELMET)
                .build();
    }
    
    private static TaskConfiguration createCrocodileConfig() {
        return TaskConfiguration.builder()
                .taskName("Crocodiles")
                .cannonCompatible(false)
                .requiresSpecialKill(false)
                .requiresDesertGear(true)
                .extraInventoryItems(Arrays.asList(
                    TaskConfiguration.RequiredItem.builder()
                        .itemId(ItemID.WATERSKIN4)
                        .itemName("Waterskin(4)")
                        .quantity(3)
                        .consumable(true)
                        .build()
                ))
                .build();
    }
    
    private static TaskConfiguration createKalphiteConfig() {
        return TaskConfiguration.builder()
                .taskName("Kalphite")
                .monsterInfo(SlayerTaskMonster.KALPHITE)
                .cannonCompatible(true)
                .requiresSpecialKill(false)
                .requiresDesertGear(true)
                .extraInventoryItems(Arrays.asList(
                    TaskConfiguration.RequiredItem.builder()
                        .itemId(ItemID.WATERSKIN4)
                        .itemName("Waterskin(4)")
                        .quantity(3)
                        .consumable(true)
                        .build()
                ))
                .build();
    }
    
    private static TaskConfiguration createWolvesConfig() {
        return TaskConfiguration.builder()
                .taskName("Wolves")
                .monsterInfo(SlayerTaskMonster.getMonsterByName("Wolf"))
                .cannonCompatible(true)
                .requiresSpecialKill(false)
                .requiresDesertGear(false)
                .requiresPrayerPotions(false)
                .requiresBankingBefore(false)
                .isWilderness(false)
                .isMultiCombat(false)
                .build();
    }
    
    private static TaskConfiguration createGenericConfig(String taskName) {
        // Create a basic configuration for tasks not specifically handled
        TaskConfiguration config = TaskConfiguration.createDefault(taskName);
        
        // Try to find the monster info
        SlayerTaskMonster monster = SlayerTaskMonster.getMonsterByName(taskName);
        if (monster != null) {
            config.setMonsterInfo(monster);
            
            // Set some basic properties based on monster info
            String[] itemsRequired = monster.getItemsRequired();
            if (itemsRequired != null && itemsRequired.length > 0 && !itemsRequired[0].equals("None")) {
                // This monster requires special equipment - mark it as needing protection
                config.setRequiresBankingBefore(true);
            }
        }
        
        return config;
    }
}
