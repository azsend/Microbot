package net.runelite.client.plugins.microbot.azsend.aioslayer;

import net.runelite.client.config.*;
import net.runelite.client.plugins.microbot.azsend.aioslayer.enums.SlayerTaskEnum;
import net.runelite.client.plugins.microbot.inventorysetups.InventorySetup;
import net.runelite.client.plugins.microbot.util.skills.slayer.enums.SlayerMaster;

@ConfigGroup("AioSlayer")
public interface AioSlayerConfig extends Config {

    @ConfigSection(
            name = "General Settings",
            description = "General slayer bot settings",
            position = 0,
            closedByDefault = false
    )
    String generalSection = "General Settings";

    @ConfigSection(
            name = "Combat Settings",
            description = "Combat related settings",
            position = 1,
            closedByDefault = false
    )
    String combatSection = "Combat Settings";

    @ConfigSection(
            name = "Looting Settings",
            description = "Item looting and value settings",
            position = 2,
            closedByDefault = false
    )
    String lootingSection = "Looting Settings";

    // Combat Settings - Supply Thresholds
    @ConfigItem(
            name = "Minimum Arrow/Bolt Count",
            keyName = "minimumAmmoCount",
            description = "Minimum arrows/bolts before returning to bank (0 to disable)",
            position = 0,
            section = combatSection
    )
    default int minimumAmmoCount() {
        return 20;
    }

    @ConfigItem(
            name = "Minimum Cannonball Count",
            keyName = "minimumCannonballCount", 
            description = "Minimum cannonballs before returning to bank for cannon tasks (0 to disable)",
            position = 1,
            section = combatSection
    )
    default int minimumCannonballCount() {
        return 50;
    }

    @ConfigItem(
            name = "Minimum Food Count",
            keyName = "minimumFoodCount",
            description = "Minimum food items before returning to bank (0 to disable)",
            position = 2,
            section = combatSection
    )
    default int minimumFoodCount() {
        return 2;
    }

    @ConfigItem(
            name = "Minimum Prayer Potion Count",
            keyName = "minimumPrayerPotionCount",
            description = "Minimum prayer potions before returning to bank (0 to disable)",
            position = 3,
            section = combatSection
    )
    default int minimumPrayerPotionCount() {
        return 0;
    }

    // Task-specific sections
    @ConfigSection(
            name = "Aberrant Spectres",
            description = "Configuration for Aberrant Spectres tasks",
            position = 10,
            closedByDefault = true
    )
    String aberrantSpectresSection = "Aberrant Spectres";

    @ConfigSection(
            name = "Abyssal Demons",
            description = "Configuration for Abyssal Demons tasks",
            position = 11,
            closedByDefault = true
    )
    String abyssalDemonsSection = "Abyssal Demons";

    @ConfigSection(
            name = "Ankou",
            description = "Configuration for Ankou tasks",
            position = 12,
            closedByDefault = true
    )
    String ankouSection = "Ankou";

    @ConfigSection(
            name = "Basilisks",
            description = "Configuration for Basilisk tasks",
            position = 13,
            closedByDefault = true
    )
    String basilisksSection = "Basilisks";

    @ConfigSection(
            name = "Black Demons",
            description = "Configuration for Black Demons tasks",
            position = 14,
            closedByDefault = true
    )
    String blackDemonsSection = "Black Demons";

    @ConfigSection(
            name = "Bloodveld",
            description = "Configuration for Bloodveld tasks",
            position = 15,
            closedByDefault = true
    )
    String bloodveldSection = "Bloodveld";

    @ConfigSection(
            name = "Cave Horrors",
            description = "Configuration for Cave Horrors tasks",
            position = 16,
            closedByDefault = true
    )
    String caveHorrorsSection = "Cave Horrors";

    @ConfigSection(
            name = "Crawling Hands",
            description = "Configuration for Crawling Hands tasks",
            position = 17,
            closedByDefault = true
    )
    String crawlingHandsSection = "Crawling Hands";

    @ConfigSection(
            name = "Dagannoth",
            description = "Configuration for Dagannoth tasks",
            position = 18,
            closedByDefault = true
    )
    String dagannothSection = "Dagannoth";

    @ConfigSection(
            name = "Dark Beasts",
            description = "Configuration for Dark Beasts tasks",
            position = 19,
            closedByDefault = true
    )
    String darkBeastsSection = "Dark Beasts";

    @ConfigSection(
            name = "Dust Devils",
            description = "Configuration for Dust Devils tasks",
            position = 20,
            closedByDefault = true
    )
    String dustDevilsSection = "Dust Devils";

    @ConfigSection(
            name = "Fire Giants",
            description = "Configuration for Fire Giants tasks",
            position = 21,
            closedByDefault = true
    )
    String fireGiantsSection = "Fire Giants";

    @ConfigSection(
            name = "Gargoyles",
            description = "Configuration for Gargoyles tasks",
            position = 22,
            closedByDefault = true
    )
    String gargoylesSection = "Gargoyles";

    @ConfigSection(
            name = "Greater Demons",
            description = "Configuration for Greater Demons tasks",
            position = 23,
            closedByDefault = true
    )
    String greaterDemonsSection = "Greater Demons";

    @ConfigSection(
            name = "Hellhounds",
            description = "Configuration for Hellhounds tasks",
            position = 24,
            closedByDefault = true
    )
    String hellhoundsSection = "Hellhounds";

    @ConfigSection(
            name = "Hill Giants",
            description = "Configuration for Hill Giants tasks",
            position = 25,
            closedByDefault = true
    )
    String hillGiantsSection = "Hill Giants";

    @ConfigSection(
            name = "Infernal Mages",
            description = "Configuration for Infernal Mages tasks",
            position = 26,
            closedByDefault = true
    )
    String infernalMagesSection = "Infernal Mages";

    @ConfigSection(
            name = "Kalphite",
            description = "Configuration for Kalphite tasks",
            position = 27,
            closedByDefault = true
    )
    String kalphiteSection = "Kalphite";

    @ConfigSection(
            name = "Kurask",
            description = "Configuration for Kurask tasks",
            position = 28,
            closedByDefault = true
    )
    String kuraskSection = "Kurask";

    @ConfigSection(
            name = "Lesser Demons",
            description = "Configuration for Lesser Demons tasks",
            position = 29,
            closedByDefault = true
    )
    String lesserDemonsSection = "Lesser Demons";

    @ConfigSection(
            name = "Nechryael",
            description = "Configuration for Nechryael tasks",
            position = 30,
            closedByDefault = true
    )
    String nechryaelSection = "Nechryael";

    @ConfigSection(
            name = "Skeletal Wyverns",
            description = "Configuration for Skeletal Wyverns tasks",
            position = 31,
            closedByDefault = true
    )
    String skeletalWyvernsSection = "Skeletal Wyverns";

    @ConfigSection(
            name = "Smoke Devils",
            description = "Configuration for Smoke Devils tasks",
            position = 32,
            closedByDefault = true
    )
    String smokeDevilsSection = "Smoke Devils";

    @ConfigSection(
            name = "Spiritual Creatures",
            description = "Configuration for Spiritual Creatures tasks",
            position = 33,
            closedByDefault = true
    )
    String spiritualCreaturesSection = "Spiritual Creatures";

    @ConfigSection(
            name = "Suqahs",
            description = "Configuration for Suqahs tasks",
            position = 34,
            closedByDefault = true
    )
    String suqahsSection = "Suqahs";

    @ConfigSection(
            name = "Trolls",
            description = "Configuration for Trolls tasks",
            position = 35,
            closedByDefault = true
    )
    String trollsSection = "Trolls";

    @ConfigSection(
            name = "Turoth",
            description = "Configuration for Turoth tasks",
            position = 36,
            closedByDefault = true
    )
    String turothSection = "Turoth";

    @ConfigSection(
            name = "TzHaar",
            description = "Configuration for TzHaar tasks",
            position = 37,
            closedByDefault = true
    )
    String tzhaarSection = "TzHaar";

    @ConfigSection(
            name = "Waterfiends",
            description = "Configuration for Waterfiends tasks",
            position = 38,
            closedByDefault = true
    )
    String waterfiendsSection = "Waterfiends";

    @ConfigSection(
            name = "Wyrms",
            description = "Configuration for Wyrms tasks",
            position = 39,
            closedByDefault = true
    )
    String wyrmsSection = "Wyrms";

    @ConfigSection(
            name = "Adamant Dragons",
            description = "Configuration for Adamant Dragons tasks",
            position = 40,
            closedByDefault = true
    )
    String adamantDragonsSection = "Adamant Dragons";

    @ConfigSection(
            name = "Aviansies",
            description = "Configuration for Aviansies tasks",
            position = 41,
            closedByDefault = true
    )
    String aviansiesSection = "Aviansies";

    @ConfigSection(
            name = "Bandits",
            description = "Configuration for Bandits tasks",
            position = 42,
            closedByDefault = true
    )
    String banditsSection = "Bandits";

    @ConfigSection(
            name = "Banshees",
            description = "Configuration for Banshees tasks",
            position = 43,
            closedByDefault = true
    )
    String bansheesSection = "Banshees";

    @ConfigSection(
            name = "Black Dragons",
            description = "Configuration for Black Dragons tasks",
            position = 44,
            closedByDefault = true
    )
    String blackDragonsSection = "Black Dragons";

    @ConfigSection(
            name = "Blue Dragons",
            description = "Configuration for Blue Dragons tasks",
            position = 45,
            closedByDefault = true
    )
    String blueDragonsSection = "Blue Dragons";

    @ConfigSection(
            name = "Brine Rats",
            description = "Configuration for Brine Rats tasks",
            position = 46,
            closedByDefault = true
    )
    String brineRatsSection = "Brine Rats";

    @ConfigSection(
            name = "Cave Bugs",
            description = "Configuration for Cave Bugs tasks",
            position = 47,
            closedByDefault = true
    )
    String caveBugsSection = "Cave Bugs";

    @ConfigSection(
            name = "Cave Crawlers",
            description = "Configuration for Cave Crawlers tasks",
            position = 48,
            closedByDefault = true
    )
    String caveCrawlersSection = "Cave Crawlers";

    @ConfigSection(
            name = "Cave Kraken",
            description = "Configuration for Cave Kraken tasks",
            position = 49,
            closedByDefault = true
    )
    String caveKrakenSection = "Cave Kraken";

    @ConfigSection(
            name = "Cave Slimes",
            description = "Configuration for Cave Slimes tasks",
            position = 50,
            closedByDefault = true
    )
    String caveSlimesSection = "Cave Slimes";

    @ConfigSection(
            name = "Cockatrice",
            description = "Configuration for Cockatrice tasks",
            position = 51,
            closedByDefault = true
    )
    String cockatriceSection = "Cockatrice";

    @ConfigSection(
            name = "Crocodiles",
            description = "Configuration for Crocodiles tasks",
            position = 52,
            closedByDefault = true
    )
    String crocodilesSection = "Crocodiles";

    @ConfigSection(
            name = "Earth Warriors",
            description = "Configuration for Earth Warriors tasks",
            position = 53,
            closedByDefault = true
    )
    String earthWarriorsSection = "Earth Warriors";

    @ConfigSection(
            name = "Fossil Island Wyverns",
            description = "Configuration for Fossil Island Wyverns tasks",
            position = 54,
            closedByDefault = true
    )
    String fossilIslandWyvernsSection = "Fossil Island Wyverns";

    @ConfigSection(
            name = "Green Dragons",
            description = "Configuration for Green Dragons tasks",
            position = 55,
            closedByDefault = true
    )
    String greenDragonsSection = "Green Dragons";

    @ConfigSection(
            name = "Hobgoblins",
            description = "Configuration for Hobgoblins tasks",
            position = 56,
            closedByDefault = true
    )
    String hobgoblinsSection = "Hobgoblins";

    @ConfigSection(
            name = "Icefiends",
            description = "Configuration for Icefiends tasks",
            position = 57,
            closedByDefault = true
    )
    String icefiendsSection = "Icefiends";

    @ConfigSection(
            name = "Ice Giants",
            description = "Configuration for Ice Giants tasks",
            position = 58,
            closedByDefault = true
    )
    String iceGiantsSection = "Ice Giants";

    @ConfigSection(
            name = "Ice Warriors",
            description = "Configuration for Ice Warriors tasks",
            position = 59,
            closedByDefault = true
    )
    String iceWarriorsSection = "Ice Warriors";

    @ConfigSection(
            name = "Jellies",
            description = "Configuration for Jellies tasks",
            position = 60,
            closedByDefault = true
    )
    String jelliesSection = "Jellies";

    @ConfigSection(
            name = "Jungle Horrors",
            description = "Configuration for Jungle Horrors tasks",
            position = 61,
            closedByDefault = true
    )
    String jungleHorrorsSection = "Jungle Horrors";

    @ConfigSection(
            name = "Lizardmen",
            description = "Configuration for Lizardmen tasks",
            position = 62,
            closedByDefault = true
    )
    String lizardmenSection = "Lizardmen";

    @ConfigSection(
            name = "Lizards",
            description = "Configuration for Lizards tasks (requires Ice Cooler)",
            position = 63,
            closedByDefault = true
    )
    String lizardsSection = "Lizards";

    @ConfigSection(
            name = "Magic Axes",
            description = "Configuration for Magic Axes tasks",
            position = 64,
            closedByDefault = true
    )
    String magicAxesSection = "Magic Axes";

    @ConfigSection(
            name = "Metal Dragons",
            description = "Configuration for Metal Dragons tasks",
            position = 65,
            closedByDefault = true
    )
    String metalDragonsSection = "Metal Dragons";

    @ConfigSection(
            name = "Minotaurs",
            description = "Configuration for Minotaurs tasks",
            position = 66,
            closedByDefault = true
    )
    String minotaursSection = "Minotaurs";

    @ConfigSection(
            name = "Mogres",
            description = "Configuration for Mogres tasks",
            position = 67,
            closedByDefault = true
    )
    String mogresSection = "Mogres";

    @ConfigSection(
            name = "Moss Giants",
            description = "Configuration for Moss Giants tasks",
            position = 68,
            closedByDefault = true
    )
    String mossGiantsSection = "Moss Giants";

    @ConfigSection(
            name = "Mutated Zygomites",
            description = "Configuration for Mutated Zygomites tasks (requires Fungicide)",
            position = 69,
            closedByDefault = true
    )
    String mutatedZygomitesSection = "Mutated Zygomites";

    @ConfigSection(
            name = "Ogres",
            description = "Configuration for Ogres tasks",
            position = 70,
            closedByDefault = true
    )
    String ogresSection = "Ogres";

    @ConfigSection(
            name = "Pyrefiends",
            description = "Configuration for Pyrefiends tasks",
            position = 71,
            closedByDefault = true
    )
    String pyrefiendsSection = "Pyrefiends";

    @ConfigSection(
            name = "Red Dragons",
            description = "Configuration for Red Dragons tasks",
            position = 72,
            closedByDefault = true
    )
    String redDragonsSection = "Red Dragons";

    @ConfigSection(
            name = "Rockslugs",
            description = "Configuration for Rockslugs tasks (requires Bag of Salt)",
            position = 73,
            closedByDefault = true
    )
    String rockslugsSection = "Rockslugs";

    @ConfigSection(
            name = "Terror Dogs",
            description = "Configuration for Terror Dogs tasks",
            position = 74,
            closedByDefault = true
    )
    String terrorDogsSection = "Terror Dogs";

    @ConfigSection(
            name = "Vampyres",
            description = "Configuration for Vampyres tasks",
            position = 75,
            closedByDefault = true
    )
    String vampyresSection = "Vampyres";

    @ConfigSection(
            name = "Wall Beasts",
            description = "Configuration for Wall Beasts tasks",
            position = 76,
            closedByDefault = true
    )
    String wallBeastsSection = "Wall Beasts";

    @ConfigSection(
            name = "Werewolves",
            description = "Configuration for Werewolves tasks",
            position = 77,
            closedByDefault = true
    )
    String werewolvesSection = "Werewolves";

    // General Settings
    @ConfigItem(
            name = "Slayer Master",
            keyName = "slayerMaster",
            description = "Choose your preferred slayer master",
            position = 0,
            section = generalSection
    )
    default SlayerMaster slayerMaster() {
        return SlayerMaster.NIEVE;
    }

    @ConfigItem(
            name = "Enable Banking",
            keyName = "enableBanking",
            description = "Enable banking for supplies and loot",
            position = 1,
            section = generalSection
    )
    default boolean enableBanking() {
        return true;
    }

    @ConfigItem(
            name = "Break Handler",
            keyName = "enableBreakHandler",
            description = "Enable break handler for anti-detection",
            position = 2,
            section = generalSection
    )
    default boolean enableBreakHandler() {
        return true;
    }

    // Combat Settings
    @ConfigItem(
            name = "Food Type",
            keyName = "foodType",
            description = "Type of food to eat (leave empty for auto-detection)",
            position = 0,
            section = combatSection
    )
    default String foodType() {
        return null;
    }

    @Range(min = 1, max = 99)
    @ConfigItem(
            name = "Eat at HP",
            keyName = "eatAtHp",
            description = "Eat food when HP drops to this level",
            position = 1,
            section = combatSection
    )
    default int eatAtHp() {
        return 60;
    }

    @Range(min = 1, max = 99)
    @ConfigItem(
            name = "Drink Prayer at",
            keyName = "drinkPrayerAt",
            description = "Drink prayer potion when prayer drops to this level",
            position = 2,
            section = combatSection
    )
    default int drinkPrayerAt() {
        return 20;
    }

    @ConfigItem(
            name = "Use Combat Potions",
            keyName = "useCombatPotions",
            description = "Use combat potions for better performance",
            position = 3,
            section = combatSection
    )
    default boolean useCombatPotions() {
        return true;
    }

    @ConfigItem(
            name = "Use Special Attack",
            keyName = "useSpecialAttack",
            description = "Use special attack when available",
            position = 4,
            section = combatSection
    )
    default boolean useSpecialAttack() {
        return true;
    }

    @Range(min = 25, max = 100)
    @ConfigItem(
            name = "Special Attack at %",
            keyName = "specialAttackAt",
            description = "Use special attack when energy reaches this percentage",
            position = 5,
            section = combatSection
    )
    default int specialAttackAt() {
        return 50;
    }

    // Looting Settings
    @ConfigItem(
            name = "Loot Items",
            keyName = "lootItems",
            description = "Enable item looting",
            position = 0,
            section = lootingSection
    )
    default boolean lootItems() {
        return true;
    }

    @ConfigItem(
            name = "Minimum Item Value",
            keyName = "minimumItemValue",
            description = "Minimum value in GP to loot items",
            position = 1,
            section = lootingSection
    )
    default int minimumItemValue() {
        return 1000;
    }

    @ConfigItem(
            name = "High Alchemy",
            keyName = "highAlchemy",
            description = "Use high alchemy on valuable items",
            position = 2,
            section = lootingSection
    )
    default boolean highAlchemy() {
        return true;
    }

    @ConfigItem(
            name = "Alchemy Profit Threshold",
            keyName = "alchemyProfitThreshold",
            description = "Minimum profit to high alch an item (GE value - HA value)",
            position = 3,
            section = lootingSection
    )
    default int alchemyProfitThreshold() {
        return -50;
    }

    @ConfigItem(
            name = "Bury Bones",
            keyName = "buryBones",
            description = "Automatically bury bones for prayer XP",
            position = 4,
            section = lootingSection
    )
    default boolean buryBones() {
        return false;
    }

    @ConfigItem(
            name = "Additional Items to Loot",
            keyName = "additionalLootItems",
            description = "Comma-separated list of additional items to always loot",
            position = 5,
            section = lootingSection
    )
    default String additionalLootItems() {
        return null;
    }

    // Aberrant Spectres
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "aberrantSpectresSetup",
            description = "Inventory setup for Aberrant Spectres",
            position = 0,
            section = aberrantSpectresSection
    )
    default InventorySetup aberrantSpectresSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "aberrantSpectresAction",
            description = "Action for Aberrant Spectres task",
            position = 1,
            section = aberrantSpectresSection
    )
    default SlayerTaskEnum.TaskAction aberrantSpectresAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    @ConfigItem(
            name = "Use Cannon",
            keyName = "aberrantSpectresCannon",
            description = "Use cannon for Aberrant Spectres",
            position = 2,
            section = aberrantSpectresSection
    )
    default boolean aberrantSpectresCannon() {
        return false;
    }

    // Abyssal Demons
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "abyssalDemonsSetup",
            description = "Inventory setup for Abyssal Demons",
            position = 0,
            section = abyssalDemonsSection
    )
    default InventorySetup abyssalDemonsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "abyssalDemonsAction",
            description = "Action for Abyssal Demons task",
            position = 1,
            section = abyssalDemonsSection
    )
    default SlayerTaskEnum.TaskAction abyssalDemonsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }


    // Ankou
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "ankouSetup",
            description = "Inventory setup for Ankou",
            position = 0,
            section = ankouSection
    )
    default InventorySetup ankouSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "ankouAction",
            description = "Action for Ankou task",
            position = 1,
            section = ankouSection
    )
    default SlayerTaskEnum.TaskAction ankouAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    @ConfigItem(
            name = "Use Cannon",
            keyName = "ankouCannon",
            description = "Use cannon for Ankou",
            position = 2,
            section = ankouSection
    )
    default boolean ankouCannon() {
        return false;
    }

    // Basilisks
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "basilisksSetup",
            description = "Inventory setup for Basilisks",
            position = 0,
            section = basilisksSection
    )
    default InventorySetup basilisksSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "basilisksAction",
            description = "Action for Basilisks task",
            position = 1,
            section = basilisksSection
    )
    default SlayerTaskEnum.TaskAction basilisksAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }


    // Black Demons
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "blackDemonsSetup",
            description = "Inventory setup for Black Demons",
            position = 0,
            section = blackDemonsSection
    )
    default InventorySetup blackDemonsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "blackDemonsAction",
            description = "Action for Black Demons task",
            position = 1,
            section = blackDemonsSection
    )
    default SlayerTaskEnum.TaskAction blackDemonsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    @ConfigItem(
            name = "Use Cannon",
            keyName = "blackDemonsCannon",
            description = "Use cannon for Black Demons",
            position = 2,
            section = blackDemonsSection
    )
    default boolean blackDemonsCannon() {
        return true;
    }

    // Bloodveld
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "bloodveldSetup",
            description = "Inventory setup for Bloodveld",
            position = 0,
            section = bloodveldSection
    )
    default InventorySetup bloodveldSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "bloodveldAction",
            description = "Action for Bloodveld task",
            position = 1,
            section = bloodveldSection
    )
    default SlayerTaskEnum.TaskAction bloodveldAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    @ConfigItem(
            name = "Use Cannon",
            keyName = "bloodveldCannon",
            description = "Use cannon for Bloodveld",
            position = 2,
            section = bloodveldSection
    )
    default boolean bloodveldCannon() {
        return true;
    }

    // Cave Horrors
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "caveHorrorsSetup",
            description = "Inventory setup for Cave Horrors",
            position = 0,
            section = caveHorrorsSection
    )
    default InventorySetup caveHorrorsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "caveHorrorsAction",
            description = "Action for Cave Horrors task",
            position = 1,
            section = caveHorrorsSection
    )
    default SlayerTaskEnum.TaskAction caveHorrorsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }


    // Crawling Hands
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "crawlingHandsSetup",
            description = "Inventory setup for Crawling Hands",
            position = 0,
            section = crawlingHandsSection
    )
    default InventorySetup crawlingHandsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "crawlingHandsAction",
            description = "Action for Crawling Hands task",
            position = 1,
            section = crawlingHandsSection
    )
    default SlayerTaskEnum.TaskAction crawlingHandsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    @ConfigItem(
            name = "Use Cannon",
            keyName = "crawlingHandsCannon",
            description = "Use cannon for Crawling Hands",
            position = 2,
            section = crawlingHandsSection
    )
    default boolean crawlingHandsCannon() {
        return true;
    }

    // Dagannoth
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "dagannothSetup",
            description = "Inventory setup for Dagannoth",
            position = 0,
            section = dagannothSection
    )
    default InventorySetup dagannothSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "dagannothAction",
            description = "Action for Dagannoth task",
            position = 1,
            section = dagannothSection
    )
    default SlayerTaskEnum.TaskAction dagannothAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    @ConfigItem(
            name = "Use Cannon",
            keyName = "dagannothCannon",
            description = "Use cannon for Dagannoth",
            position = 2,
            section = dagannothSection
    )
    default boolean dagannothCannon() {
        return true;
    }

    // Dark Beasts
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "darkBeastsSetup",
            description = "Inventory setup for Dark Beasts",
            position = 0,
            section = darkBeastsSection
    )
    default InventorySetup darkBeastsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "darkBeastsAction",
            description = "Action for Dark Beasts task",
            position = 1,
            section = darkBeastsSection
    )
    default SlayerTaskEnum.TaskAction darkBeastsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    @ConfigItem(
            name = "Use Cannon",
            keyName = "darkBeastsCannon",
            description = "Use cannon for Dark Beasts",
            position = 2,
            section = darkBeastsSection
    )
    default boolean darkBeastsCannon() {
        return true;
    }

    // Dust Devils
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "dustDevilsSetup",
            description = "Inventory setup for Dust Devils",
            position = 0,
            section = dustDevilsSection
    )
    default InventorySetup dustDevilsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "dustDevilsAction",
            description = "Action for Dust Devils task",
            position = 1,
            section = dustDevilsSection
    )
    default SlayerTaskEnum.TaskAction dustDevilsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    @ConfigItem(
            name = "Use Cannon",
            keyName = "dustDevilsCannon",
            description = "Use cannon for Dust Devils",
            position = 2,
            section = dustDevilsSection
    )
    default boolean dustDevilsCannon() {
        return true;
    }

    // Fire Giants
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "fireGiantsSetup",
            description = "Inventory setup for Fire Giants",
            position = 0,
            section = fireGiantsSection
    )
    default InventorySetup fireGiantsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "fireGiantsAction",
            description = "Action for Fire Giants task",
            position = 1,
            section = fireGiantsSection
    )
    default SlayerTaskEnum.TaskAction fireGiantsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    @ConfigItem(
            name = "Use Cannon",
            keyName = "fireGiantsCannon",
            description = "Use cannon for Fire Giants",
            position = 2,
            section = fireGiantsSection
    )
    default boolean fireGiantsCannon() {
        return true;
    }

    // Gargoyles
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "gargoylesSetup",
            description = "Inventory setup for Gargoyles (requires Rock Hammer)",
            position = 0,
            section = gargoylesSection
    )
    default InventorySetup gargoylesSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "gargoylesAction",
            description = "Action for Gargoyles task",
            position = 1,
            section = gargoylesSection
    )
    default SlayerTaskEnum.TaskAction gargoylesAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    @ConfigItem(
            name = "Use Cannon",
            keyName = "gargoylesCannon",
            description = "Use cannon for Gargoyles",
            position = 2,
            section = gargoylesSection
    )
    default boolean gargoylesCannon() {
        return false;
    }

    // Greater Demons
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "greaterDemonsSetup",
            description = "Inventory setup for Greater Demons",
            position = 0,
            section = greaterDemonsSection
    )
    default InventorySetup greaterDemonsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "greaterDemonsAction",
            description = "Action for Greater Demons task",
            position = 1,
            section = greaterDemonsSection
    )
    default SlayerTaskEnum.TaskAction greaterDemonsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    @ConfigItem(
            name = "Use Cannon",
            keyName = "greaterDemonsCannon",
            description = "Use cannon for Greater Demons",
            position = 2,
            section = greaterDemonsSection
    )
    default boolean greaterDemonsCannon() {
        return true;
    }

    // Hellhounds
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "hellhoundsSetup",
            description = "Inventory setup for Hellhounds",
            position = 0,
            section = hellhoundsSection
    )
    default InventorySetup hellhoundsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "hellhoundsAction",
            description = "Action for Hellhounds task",
            position = 1,
            section = hellhoundsSection
    )
    default SlayerTaskEnum.TaskAction hellhoundsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    @ConfigItem(
            name = "Use Cannon",
            keyName = "hellhoundsCannon",
            description = "Use cannon for Hellhounds",
            position = 2,
            section = hellhoundsSection
    )
    default boolean hellhoundsCannon() {
        return true;
    }

    // Hill Giants
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "hillGiantsSetup",
            description = "Inventory setup for Hill Giants",
            position = 0,
            section = hillGiantsSection
    )
    default InventorySetup hillGiantsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "hillGiantsAction",
            description = "Action for Hill Giants task",
            position = 1,
            section = hillGiantsSection
    )
    default SlayerTaskEnum.TaskAction hillGiantsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    @ConfigItem(
            name = "Use Cannon",
            keyName = "hillGiantsCannon",
            description = "Use cannon for Hill Giants",
            position = 2,
            section = hillGiantsSection
    )
    default boolean hillGiantsCannon() {
        return true;
    }

    // Infernal Mages
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "infernalMagesSetup",
            description = "Inventory setup for Infernal Mages",
            position = 0,
            section = infernalMagesSection
    )
    default InventorySetup infernalMagesSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "infernalMagesAction",
            description = "Action for Infernal Mages task",
            position = 1,
            section = infernalMagesSection
    )
    default SlayerTaskEnum.TaskAction infernalMagesAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }


    // Kalphite
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "kalphiteSetup",
            description = "Inventory setup for Kalphite",
            position = 0,
            section = kalphiteSection
    )
    default InventorySetup kalphiteSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "kalphiteAction",
            description = "Action for Kalphite task",
            position = 1,
            section = kalphiteSection
    )
    default SlayerTaskEnum.TaskAction kalphiteAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    @ConfigItem(
            name = "Use Cannon",
            keyName = "kalphiteCannon",
            description = "Use cannon for Kalphite",
            position = 2,
            section = kalphiteSection
    )
    default boolean kalphiteCannon() {
        return true;
    }

    // Kurask
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "kuraskSetup",
            description = "Inventory setup for Kurask",
            position = 0,
            section = kuraskSection
    )
    default InventorySetup kuraskSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "kuraskAction",
            description = "Action for Kurask task",
            position = 1,
            section = kuraskSection
    )
    default SlayerTaskEnum.TaskAction kuraskAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }


    // Lesser Demons
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "lesserDemonsSetup",
            description = "Inventory setup for Lesser Demons",
            position = 0,
            section = lesserDemonsSection
    )
    default InventorySetup lesserDemonsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "lesserDemonsAction",
            description = "Action for Lesser Demons task",
            position = 1,
            section = lesserDemonsSection
    )
    default SlayerTaskEnum.TaskAction lesserDemonsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    @ConfigItem(
            name = "Use Cannon",
            keyName = "lesserDemonsCannon",
            description = "Use cannon for Lesser Demons",
            position = 2,
            section = lesserDemonsSection
    )
    default boolean lesserDemonsCannon() {
        return true;
    }

    // Nechryael
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "nechryaelSetup",
            description = "Inventory setup for Nechryael",
            position = 0,
            section = nechryaelSection
    )
    default InventorySetup nechryaelSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "nechryaelAction",
            description = "Action for Nechryael task",
            position = 1,
            section = nechryaelSection
    )
    default SlayerTaskEnum.TaskAction nechryaelAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    @ConfigItem(
            name = "Use Cannon",
            keyName = "nechryaelCannon",
            description = "Use cannon for Nechryael",
            position = 2,
            section = nechryaelSection
    )
    default boolean nechryaelCannon() {
        return true;
    }

    // Skeletal Wyverns
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "skeletalWyvernsSetup",
            description = "Inventory setup for Skeletal Wyverns",
            position = 0,
            section = skeletalWyvernsSection
    )
    default InventorySetup skeletalWyvernsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "skeletalWyvernsAction",
            description = "Action for Skeletal Wyverns task",
            position = 1,
            section = skeletalWyvernsSection
    )
    default SlayerTaskEnum.TaskAction skeletalWyvernsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }


    // Smoke Devils
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "smokeDevilsSetup",
            description = "Inventory setup for Smoke Devils",
            position = 0,
            section = smokeDevilsSection
    )
    default InventorySetup smokeDevilsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "smokeDevilsAction",
            description = "Action for Smoke Devils task",
            position = 1,
            section = smokeDevilsSection
    )
    default SlayerTaskEnum.TaskAction smokeDevilsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }


    // Spiritual Creatures
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "spiritualCreaturesSetup",
            description = "Inventory setup for Spiritual Creatures",
            position = 0,
            section = spiritualCreaturesSection
    )
    default InventorySetup spiritualCreaturesSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "spiritualCreaturesAction",
            description = "Action for Spiritual Creatures task",
            position = 1,
            section = spiritualCreaturesSection
    )
    default SlayerTaskEnum.TaskAction spiritualCreaturesAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }


    // Suqahs
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "suqahsSetup",
            description = "Inventory setup for Suqahs",
            position = 0,
            section = suqahsSection
    )
    default InventorySetup suqahsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "suqahsAction",
            description = "Action for Suqahs task",
            position = 1,
            section = suqahsSection
    )
    default SlayerTaskEnum.TaskAction suqahsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }


    // Trolls
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "trollsSetup",
            description = "Inventory setup for Trolls",
            position = 0,
            section = trollsSection
    )
    default InventorySetup trollsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "trollsAction",
            description = "Action for Trolls task",
            position = 1,
            section = trollsSection
    )
    default SlayerTaskEnum.TaskAction trollsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    @ConfigItem(
            name = "Use Cannon",
            keyName = "trollsCannon",
            description = "Use cannon for Trolls",
            position = 2,
            section = trollsSection
    )
    default boolean trollsCannon() {
        return true;
    }

    // Turoth
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "turothSetup",
            description = "Inventory setup for Turoth",
            position = 0,
            section = turothSection
    )
    default InventorySetup turothSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "turothAction",
            description = "Action for Turoth task",
            position = 1,
            section = turothSection
    )
    default SlayerTaskEnum.TaskAction turothAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }


    // TzHaar
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "tzhaarSetup",
            description = "Inventory setup for TzHaar",
            position = 0,
            section = tzhaarSection
    )
    default InventorySetup tzhaarSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "tzhaarAction",
            description = "Action for TzHaar task",
            position = 1,
            section = tzhaarSection
    )
    default SlayerTaskEnum.TaskAction tzhaarAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }


    // Waterfiends
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "waterfiendsSetup",
            description = "Inventory setup for Waterfiends",
            position = 0,
            section = waterfiendsSection
    )
    default InventorySetup waterfiendsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "waterfiendsAction",
            description = "Action for Waterfiends task",
            position = 1,
            section = waterfiendsSection
    )
    default SlayerTaskEnum.TaskAction waterfiendsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }


    // Wyrms
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "wyrmsSetup",
            description = "Inventory setup for Wyrms",
            position = 0,
            section = wyrmsSection
    )
    default InventorySetup wyrmsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "wyrmsAction",
            description = "Action for Wyrms task",
            position = 1,
            section = wyrmsSection
    )
    default SlayerTaskEnum.TaskAction wyrmsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    // Adamant Dragons
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "adamantDragonsSetup",
            description = "Inventory setup for Adamant Dragons",
            position = 0,
            section = adamantDragonsSection
    )
    default InventorySetup adamantDragonsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "adamantDragonsAction",
            description = "Action for Adamant Dragons task",
            position = 1,
            section = adamantDragonsSection
    )
    default SlayerTaskEnum.TaskAction adamantDragonsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    // Aviansies
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "aviansiesSetup",
            description = "Inventory setup for Aviansies",
            position = 0,
            section = aviansiesSection
    )
    default InventorySetup aviansiesSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "aviansiesAction",
            description = "Action for Aviansies task",
            position = 1,
            section = aviansiesSection
    )
    default SlayerTaskEnum.TaskAction aviansiesAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    @ConfigItem(
            name = "Use Cannon",
            keyName = "aviansiesCannon",
            description = "Use cannon for Aviansies",
            position = 2,
            section = aviansiesSection
    )
    default boolean aviansiesCannon() {
        return true;
    }

    // Bandits
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "banditsSetup",
            description = "Inventory setup for Bandits",
            position = 0,
            section = banditsSection
    )
    default InventorySetup banditsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "banditsAction",
            description = "Action for Bandits task",
            position = 1,
            section = banditsSection
    )
    default SlayerTaskEnum.TaskAction banditsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    @ConfigItem(
            name = "Use Cannon",
            keyName = "banditsCannon",
            description = "Use cannon for Bandits",
            position = 2,
            section = banditsSection
    )
    default boolean banditsCannon() {
        return true;
    }

    // Banshees
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "bansheesSetup",
            description = "Inventory setup for Banshees",
            position = 0,
            section = bansheesSection
    )
    default InventorySetup bansheesSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "bansheesAction",
            description = "Action for Banshees task",
            position = 1,
            section = bansheesSection
    )
    default SlayerTaskEnum.TaskAction bansheesAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    @ConfigItem(
            name = "Use Cannon",
            keyName = "bansheesCannon",
            description = "Use cannon for Banshees",
            position = 2,
            section = bansheesSection
    )
    default boolean bansheesCannon() {
        return true;
    }

    // Black Dragons
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "blackDragonsSetup",
            description = "Inventory setup for Black Dragons",
            position = 0,
            section = blackDragonsSection
    )
    default InventorySetup blackDragonsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "blackDragonsAction",
            description = "Action for Black Dragons task",
            position = 1,
            section = blackDragonsSection
    )
    default SlayerTaskEnum.TaskAction blackDragonsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    // Blue Dragons
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "blueDragonsSetup",
            description = "Inventory setup for Blue Dragons",
            position = 0,
            section = blueDragonsSection
    )
    default InventorySetup blueDragonsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "blueDragonsAction",
            description = "Action for Blue Dragons task",
            position = 1,
            section = blueDragonsSection
    )
    default SlayerTaskEnum.TaskAction blueDragonsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    @ConfigItem(
            name = "Use Cannon",
            keyName = "blueDragonsCannon",
            description = "Use cannon for Blue Dragons",
            position = 2,
            section = blueDragonsSection
    )
    default boolean blueDragonsCannon() {
        return true;
    }

    // Brine Rats
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "brineRatsSetup",
            description = "Inventory setup for Brine Rats",
            position = 0,
            section = brineRatsSection
    )
    default InventorySetup brineRatsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "brineRatsAction",
            description = "Action for Brine Rats task",
            position = 1,
            section = brineRatsSection
    )
    default SlayerTaskEnum.TaskAction brineRatsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    // Cave Bugs
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "caveBugsSetup",
            description = "Inventory setup for Cave Bugs",
            position = 0,
            section = caveBugsSection
    )
    default InventorySetup caveBugsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "caveBugsAction",
            description = "Action for Cave Bugs task",
            position = 1,
            section = caveBugsSection
    )
    default SlayerTaskEnum.TaskAction caveBugsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    // Cave Crawlers
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "caveCrawlersSetup",
            description = "Inventory setup for Cave Crawlers",
            position = 0,
            section = caveCrawlersSection
    )
    default InventorySetup caveCrawlersSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "caveCrawlersAction",
            description = "Action for Cave Crawlers task",
            position = 1,
            section = caveCrawlersSection
    )
    default SlayerTaskEnum.TaskAction caveCrawlersAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    // Cave Kraken
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "caveKrakenSetup",
            description = "Inventory setup for Cave Kraken",
            position = 0,
            section = caveKrakenSection
    )
    default InventorySetup caveKrakenSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "caveKrakenAction",
            description = "Action for Cave Kraken task",
            position = 1,
            section = caveKrakenSection
    )
    default SlayerTaskEnum.TaskAction caveKrakenAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    // Cave Slimes
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "caveSlimesSetup",
            description = "Inventory setup for Cave Slimes",
            position = 0,
            section = caveSlimesSection
    )
    default InventorySetup caveSlimesSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "caveSlimesAction",
            description = "Action for Cave Slimes task",
            position = 1,
            section = caveSlimesSection
    )
    default SlayerTaskEnum.TaskAction caveSlimesAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    // Cockatrice
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "cockatriceSetup",
            description = "Inventory setup for Cockatrice",
            position = 0,
            section = cockatriceSection
    )
    default InventorySetup cockatriceSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "cockatriceAction",
            description = "Action for Cockatrice task",
            position = 1,
            section = cockatriceSection
    )
    default SlayerTaskEnum.TaskAction cockatriceAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    // Crocodiles
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "crocodilesSetup",
            description = "Name of inventory setup for Crocodiles",
            position = 0,
            section = crocodilesSection
    )
    default String crocodilesSetup() {
        return "";
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "crocodilesAction",
            description = "Action for Crocodiles task",
            position = 1,
            section = crocodilesSection
    )
    default SlayerTaskEnum.TaskAction crocodilesAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    // Earth Warriors
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "earthWarriorsSetup",
            description = "Inventory setup for Earth Warriors",
            position = 0,
            section = earthWarriorsSection
    )
    default InventorySetup earthWarriorsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "earthWarriorsAction",
            description = "Action for Earth Warriors task",
            position = 1,
            section = earthWarriorsSection
    )
    default SlayerTaskEnum.TaskAction earthWarriorsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    @ConfigItem(
            name = "Use Cannon",
            keyName = "earthWarriorsCannon",
            description = "Use cannon for Earth Warriors",
            position = 2,
            section = earthWarriorsSection
    )
    default boolean earthWarriorsCannon() {
        return true;
    }

    // Fossil Island Wyverns
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "fossilIslandWyvernsSetup",
            description = "Inventory setup for Fossil Island Wyverns",
            position = 0,
            section = fossilIslandWyvernsSection
    )
    default InventorySetup fossilIslandWyvernsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "fossilIslandWyvernsAction",
            description = "Action for Fossil Island Wyverns task",
            position = 1,
            section = fossilIslandWyvernsSection
    )
    default SlayerTaskEnum.TaskAction fossilIslandWyvernsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    // Green Dragons
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "greenDragonsSetup",
            description = "Inventory setup for Green Dragons",
            position = 0,
            section = greenDragonsSection
    )
    default InventorySetup greenDragonsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "greenDragonsAction",
            description = "Action for Green Dragons task",
            position = 1,
            section = greenDragonsSection
    )
    default SlayerTaskEnum.TaskAction greenDragonsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    @ConfigItem(
            name = "Use Cannon",
            keyName = "greenDragonsCannon",
            description = "Use cannon for Green Dragons",
            position = 2,
            section = greenDragonsSection
    )
    default boolean greenDragonsCannon() {
        return true;
    }

    // Hobgoblins
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "hobgoblinsSetup",
            description = "Inventory setup for Hobgoblins",
            position = 0,
            section = hobgoblinsSection
    )
    default InventorySetup hobgoblinsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "hobgoblinsAction",
            description = "Action for Hobgoblins task",
            position = 1,
            section = hobgoblinsSection
    )
    default SlayerTaskEnum.TaskAction hobgoblinsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    @ConfigItem(
            name = "Use Cannon",
            keyName = "hobgoblinsCannon",
            description = "Use cannon for Hobgoblins",
            position = 2,
            section = hobgoblinsSection
    )
    default boolean hobgoblinsCannon() {
        return true;
    }

    // Icefiends
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "icefiendsSetup",
            description = "Inventory setup for Icefiends",
            position = 0,
            section = icefiendsSection
    )
    default InventorySetup icefiendsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "icefiendsAction",
            description = "Action for Icefiends task",
            position = 1,
            section = icefiendsSection
    )
    default SlayerTaskEnum.TaskAction icefiendsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    // Ice Giants
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "iceGiantsSetup",
            description = "Inventory setup for Ice Giants",
            position = 0,
            section = iceGiantsSection
    )
    default InventorySetup iceGiantsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "iceGiantsAction",
            description = "Action for Ice Giants task",
            position = 1,
            section = iceGiantsSection
    )
    default SlayerTaskEnum.TaskAction iceGiantsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    // Ice Warriors
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "iceWarriorsSetup",
            description = "Inventory setup for Ice Warriors",
            position = 0,
            section = iceWarriorsSection
    )
    default InventorySetup iceWarriorsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "iceWarriorsAction",
            description = "Action for Ice Warriors task",
            position = 1,
            section = iceWarriorsSection
    )
    default SlayerTaskEnum.TaskAction iceWarriorsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    // Jellies
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "jelliesSetup",
            description = "Inventory setup for Jellies",
            position = 0,
            section = jelliesSection
    )
    default InventorySetup jelliesSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "jelliesAction",
            description = "Action for Jellies task",
            position = 1,
            section = jelliesSection
    )
    default SlayerTaskEnum.TaskAction jelliesAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    // Jungle Horrors
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "jungleHorrorsSetup",
            description = "Inventory setup for Jungle Horrors",
            position = 0,
            section = jungleHorrorsSection
    )
    default InventorySetup jungleHorrorsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "jungleHorrorsAction",
            description = "Action for Jungle Horrors task",
            position = 1,
            section = jungleHorrorsSection
    )
    default SlayerTaskEnum.TaskAction jungleHorrorsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    // Lizardmen
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "lizardmenSetup",
            description = "Inventory setup for Lizardmen",
            position = 0,
            section = lizardmenSection
    )
    default InventorySetup lizardmenSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "lizardmenAction",
            description = "Action for Lizardmen task",
            position = 1,
            section = lizardmenSection
    )
    default SlayerTaskEnum.TaskAction lizardmenAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    @ConfigItem(
            name = "Use Cannon",
            keyName = "lizardmenCannon",
            description = "Use cannon for Lizardmen",
            position = 2,
            section = lizardmenSection
    )
    default boolean lizardmenCannon() {
        return true;
    }

    // Lizards
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "lizardsSetup",
            description = "Inventory setup for Lizards (requires Ice Cooler)",
            position = 0,
            section = lizardsSection
    )
    default InventorySetup lizardsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "lizardsAction",
            description = "Action for Lizards task",
            position = 1,
            section = lizardsSection
    )
    default SlayerTaskEnum.TaskAction lizardsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    // Magic Axes
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "magicAxesSetup",
            description = "Inventory setup for Magic Axes",
            position = 0,
            section = magicAxesSection
    )
    default InventorySetup magicAxesSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "magicAxesAction",
            description = "Action for Magic Axes task",
            position = 1,
            section = magicAxesSection
    )
    default SlayerTaskEnum.TaskAction magicAxesAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    // Metal Dragons
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "metalDragonsSetup",
            description = "Inventory setup for Metal Dragons",
            position = 0,
            section = metalDragonsSection
    )
    default InventorySetup metalDragonsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "metalDragonsAction",
            description = "Action for Metal Dragons task",
            position = 1,
            section = metalDragonsSection
    )
    default SlayerTaskEnum.TaskAction metalDragonsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    // Minotaurs
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "minotaursSetup",
            description = "Inventory setup for Minotaurs",
            position = 0,
            section = minotaursSection
    )
    default InventorySetup minotaursSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "minotaursAction",
            description = "Action for Minotaurs task",
            position = 1,
            section = minotaursSection
    )
    default SlayerTaskEnum.TaskAction minotaursAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    // Mogres
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "mogresSetup",
            description = "Inventory setup for Mogres",
            position = 0,
            section = mogresSection
    )
    default InventorySetup mogresSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "mogresAction",
            description = "Action for Mogres task",
            position = 1,
            section = mogresSection
    )
    default SlayerTaskEnum.TaskAction mogresAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    // Moss Giants
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "mossGiantsSetup",
            description = "Inventory setup for Moss Giants",
            position = 0,
            section = mossGiantsSection
    )
    default InventorySetup mossGiantsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "mossGiantsAction",
            description = "Action for Moss Giants task",
            position = 1,
            section = mossGiantsSection
    )
    default SlayerTaskEnum.TaskAction mossGiantsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    @ConfigItem(
            name = "Use Cannon",
            keyName = "mossGiantsCannon",
            description = "Use cannon for Moss Giants",
            position = 2,
            section = mossGiantsSection
    )
    default boolean mossGiantsCannon() {
        return true;
    }

    // Mutated Zygomites
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "mutatedZygomitesSetup",
            description = "Inventory setup for Mutated Zygomites (requires Fungicide)",
            position = 0,
            section = mutatedZygomitesSection
    )
    default InventorySetup mutatedZygomitesSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "mutatedZygomitesAction",
            description = "Action for Mutated Zygomites task",
            position = 1,
            section = mutatedZygomitesSection
    )
    default SlayerTaskEnum.TaskAction mutatedZygomitesAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    // Ogres
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "ogresSetup",
            description = "Inventory setup for Ogres",
            position = 0,
            section = ogresSection
    )
    default InventorySetup ogresSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "ogresAction",
            description = "Action for Ogres task",
            position = 1,
            section = ogresSection
    )
    default SlayerTaskEnum.TaskAction ogresAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    @ConfigItem(
            name = "Use Cannon",
            keyName = "ogresCannon",
            description = "Use cannon for Ogres",
            position = 2,
            section = ogresSection
    )
    default boolean ogresCannon() {
        return true;
    }

    // Pyrefiends
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "pyrefiendsSetup",
            description = "Inventory setup for Pyrefiends",
            position = 0,
            section = pyrefiendsSection
    )
    default InventorySetup pyrefiendsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "pyrefiendsAction",
            description = "Action for Pyrefiends task",
            position = 1,
            section = pyrefiendsSection
    )
    default SlayerTaskEnum.TaskAction pyrefiendsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    // Red Dragons
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "redDragonsSetup",
            description = "Inventory setup for Red Dragons",
            position = 0,
            section = redDragonsSection
    )
    default InventorySetup redDragonsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "redDragonsAction",
            description = "Action for Red Dragons task",
            position = 1,
            section = redDragonsSection
    )
    default SlayerTaskEnum.TaskAction redDragonsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    // Rockslugs
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "rockslugsSetup",
            description = "Inventory setup for Rockslugs (requires Bag of Salt)",
            position = 0,
            section = rockslugsSection
    )
    default InventorySetup rockslugsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "rockslugsAction",
            description = "Action for Rockslugs task",
            position = 1,
            section = rockslugsSection
    )
    default SlayerTaskEnum.TaskAction rockslugsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    // Terror Dogs
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "terrorDogsSetup",
            description = "Inventory setup for Terror Dogs",
            position = 0,
            section = terrorDogsSection
    )
    default InventorySetup terrorDogsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "terrorDogsAction",
            description = "Action for Terror Dogs task",
            position = 1,
            section = terrorDogsSection
    )
    default SlayerTaskEnum.TaskAction terrorDogsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    // Vampyres
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "vampyresSetup",
            description = "Inventory setup for Vampyres",
            position = 0,
            section = vampyresSection
    )
    default InventorySetup vampyresSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "vampyresAction",
            description = "Action for Vampyres task",
            position = 1,
            section = vampyresSection
    )
    default SlayerTaskEnum.TaskAction vampyresAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    // Wall Beasts
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "wallBeastsSetup",
            description = "Inventory setup for Wall Beasts",
            position = 0,
            section = wallBeastsSection
    )
    default InventorySetup wallBeastsSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "wallBeastsAction",
            description = "Action for Wall Beasts task",
            position = 1,
            section = wallBeastsSection
    )
    default SlayerTaskEnum.TaskAction wallBeastsAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

    // Werewolves
    @ConfigItem(
            name = "Inventory Setup",
            keyName = "werewolvesSetup",
            description = "Inventory setup for Werewolves",
            position = 0,
            section = werewolvesSection
    )
    default InventorySetup werewolvesSetup() {
        return null;
    }

    @ConfigItem(
            name = "Task Action",
            keyName = "werewolvesAction",
            description = "Action for Werewolves task",
            position = 1,
            section = werewolvesSection
    )
    default SlayerTaskEnum.TaskAction werewolvesAction() {
        return SlayerTaskEnum.TaskAction.KILL;
    }

}
