package net.runelite.client.plugins.microbot.azsend.aioslayer.enums;

import lombok.Getter;

@Getter
public enum SlayerBotState {
    INITIALIZING("Initializing bot..."),
    CHECKING_TASK("Checking current slayer task"),
    GETTING_TASK("Getting new task from slayer master"),
    BANKING("Banking for supplies"),
    TRAVELING_TO_LOCATION("Traveling to task location"),
    SETTING_UP_CANNON("Setting up cannon"),
    COMBAT("Fighting monsters"),
    LOOTING("Looting items"),
    SPECIAL_KILL("Performing special kill action"),
    RESTORING_STATS("Restoring combat stats"),
    TASK_COMPLETE("Task completed"),
    ERROR("Error occurred"),
    STOPPED("Bot stopped");

    private final String description;

    SlayerBotState(String description) {
        this.description = description;
    }
}
