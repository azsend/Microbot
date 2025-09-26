package net.runelite.client.plugins.microbot.azsend.aioslayer.enums;

import lombok.Getter;

@Getter
public enum SlayerTaskEnum {
    ABERRANT_SPECTRES("Aberrant spectres", true, false, new int[]{}),
    ABYSSAL_DEMONS("Abyssal demons", false, false, new int[]{}),
    ADAMANT_DRAGONS("Adamant dragons", false, false, new int[]{}),
    ANKOU("Ankou", true, false, new int[]{}),
    AVIANSIES("Aviansies", true, false, new int[]{}),
    BANDITS("Bandits", true, false, new int[]{}),
    BANSHEES("Banshees", true, false, new int[]{}),
    BASILISKS("Basilisks", false, false, new int[]{}),
    BLACK_DEMONS("Black demons", true, false, new int[]{}),
    BLACK_DRAGONS("Black dragons", false, false, new int[]{}),
    BLOODVELD("Bloodveld", true, false, new int[]{}),
    BLUE_DRAGONS("Blue dragons", true, false, new int[]{}),
    BRINE_RATS("Brine rats", false, false, new int[]{}),
    CAVE_BUGS("Cave bugs", false, false, new int[]{}),
    CAVE_CRAWLERS("Cave crawlers", false, false, new int[]{}),
    CAVE_HORRORS("Cave horrors", false, false, new int[]{}),
    CAVE_KRAKEN("Cave kraken", false, false, new int[]{}),
    CAVE_SLIMES("Cave slimes", false, false, new int[]{}),
    COCKATRICE("Cockatrice", false, false, new int[]{}),
    CRAWLING_HANDS("Crawling hands", true, false, new int[]{}),
    CROCODILES("Crocodiles", false, false, new int[]{}),
    DAGANNOTH("Dagannoth", true, false, new int[]{}),
    DARK_BEASTS("Dark beasts", true, false, new int[]{}),
    DUST_DEVILS("Dust devils", true, false, new int[]{}),
    EARTH_WARRIORS("Earth warriors", true, false, new int[]{}),
    FIRE_GIANTS("Fire giants", true, false, new int[]{}),
    FOSSIL_ISLAND_WYVERNS("Fossil island wyverns", false, false, new int[]{}),
    GARGOYLES("Gargoyles", false, true, new int[]{}),
    GREATER_DEMONS("Greater demons", true, false, new int[]{}),
    GREEN_DRAGONS("Green dragons", true, false, new int[]{}),
    HELLHOUNDS("Hellhounds", true, false, new int[]{}),
    HILL_GIANTS("Hill giants", true, false, new int[]{}),
    HOBGOBLINS("Hobgoblins", true, false, new int[]{}),
    ICEFIENDS("Icefiends", false, false, new int[]{}),
    ICE_GIANTS("Ice giants", false, false, new int[]{}),
    ICE_WARRIORS("Ice warriors", false, false, new int[]{}),
    INFERNAL_MAGES("Infernal mages", false, false, new int[]{}),
    JELLIES("Jellies", false, false, new int[]{}),
    JUNGLE_HORROR("Jungle horrors", false, false, new int[]{}),
    KALPHITE("Kalphite", true, false, new int[]{}),
    KURASK("Kurask", false, false, new int[]{}),
    LESSER_DEMONS("Lesser demons", true, false, new int[]{}),
    LIZARDMEN("Lizardmen", true, false, new int[]{}),
    LIZARDS("Lizards", false, true, new int[]{}),
    MAGIC_AXES("Magic axes", false, false, new int[]{}),
    METAL_DRAGONS("Metal dragons", false, false, new int[]{}),
    MINOTAURS("Minotaurs", false, false, new int[]{}),
    MOGRES("Mogres", false, false, new int[]{}),
    MOSS_GIANTS("Moss giants", true, false, new int[]{}),
    MUTATED_ZYGOMITES("Mutated zygomites", false, true, new int[]{}),
    NECHRYAEL("Nechryael", true, false, new int[]{}),
    OGRES("Ogres", true, false, new int[]{}),
    PYREFIENDS("Pyrefiends", false, false, new int[]{}),
    RED_DRAGONS("Red dragons", false, false, new int[]{}),
    ROCKSLUGS("Rockslugs", false, true, new int[]{}),
    SKELETAL_WYVERNS("Skeletal wyverns", false, false, new int[]{}),
    SMOKE_DEVILS("Smoke devils", false, false, new int[]{}),
    SPIRITUAL_CREATURES("Spiritual creatures", false, false, new int[]{}),
    SUQAHS("Suqahs", false, false, new int[]{}),
    TERROR_DOGS("Terror dogs", false, false, new int[]{}),
    TROLLS("Trolls", true, false, new int[]{}),
    TUROTH("Turoth", false, false, new int[]{}),
    TZHAAR("TzHaar", false, false, new int[]{}),
    VAMPYRES("Vampyres", false, false, new int[]{}),
    WALL_BEASTS("Wall beasts", false, false, new int[]{}),
    WATERFIENDS("Waterfiends", false, false, new int[]{}),
    WEREWOLVES("Werewolves", false, false, new int[]{}),
    WYRMS("Wyrms", false, false, new int[]{});

    private final String taskName;
    private final boolean cannonCompatible;
    private final boolean requiresSpecialKill;
    private final int[] requiredItems;

    SlayerTaskEnum(String taskName, boolean cannonCompatible, boolean requiresSpecialKill, int[] requiredItems) {
        this.taskName = taskName;
        this.cannonCompatible = cannonCompatible;
        this.requiresSpecialKill = requiresSpecialKill;
        this.requiredItems = requiredItems;
    }

    public static SlayerTaskEnum fromTaskName(String taskName) {
        if (taskName == null) return null;
        
        for (SlayerTaskEnum task : values()) {
            if (task.getTaskName().equalsIgnoreCase(taskName)) {
                return task;
            }
        }
        return null;
    }

    public enum TaskAction {
        KILL("Kill"),
        SKIP("Skip"),
        BLOCK("Block");

        @Getter
        private final String displayName;

        TaskAction(String displayName) {
            this.displayName = displayName;
        }
    }
}
