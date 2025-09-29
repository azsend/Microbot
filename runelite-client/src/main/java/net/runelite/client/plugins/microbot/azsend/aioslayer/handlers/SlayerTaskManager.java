package net.runelite.client.plugins.microbot.azsend.aioslayer.handlers;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.azsend.aioslayer.AioSlayerConfig;
import net.runelite.client.plugins.microbot.azsend.aioslayer.config.TaskConfigurationFactory;
import net.runelite.client.plugins.microbot.azsend.aioslayer.enums.SlayerBotState;
import net.runelite.client.plugins.microbot.azsend.aioslayer.models.TaskConfiguration;
import net.runelite.client.plugins.microbot.util.Global;
import net.runelite.client.plugins.microbot.util.npc.MonsterLocation;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.skills.slayer.Rs2Slayer;
import net.runelite.client.plugins.microbot.util.skills.slayer.enums.SlayerMaster;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

/**
 * Central manager that coordinates all slayer task handlers
 */
@Slf4j
public class SlayerTaskManager implements StateCallback {
    
    private final AioSlayerConfig config;
    private TaskConfiguration currentTaskConfig;
    private MonsterLocation currentTaskLocation;
    
    // Handlers
    private BankingHandler bankingHandler;
    private EquipmentHandler equipmentHandler;
    private InventorySetupHandler inventoryHandler;
    private CombatHandler combatHandler;
    private CannonHandler cannonHandler;
    private FinishingBlowHandler finishingBlowHandler;
    
    // State management
    private SlayerBotState currentState = SlayerBotState.INITIALIZING;
    private String statusMessage = "";
    private Exception lastError = null;
    
    // Statistics
    private int monstersKilled = 0;
    private long taskStartTime = 0;
    
    public SlayerTaskManager(AioSlayerConfig config) {
        this.config = config;
    }
    
    /**
     * Main execution method - coordinates all handlers based on current state
     */
    public boolean execute() {
        try {
            log.debug("Executing SlayerTaskManager in state: {}", currentState);
            
            switch (currentState) {
                case INITIALIZING:
                    return handleInitializing();
                case CHECKING_TASK:
                    return handleCheckingTask();
                case GETTING_TASK:
                    return handleGettingTask();
                case BANKING:
                    return handleBanking();
                case TRAVELING_TO_LOCATION:
                    return handleTravelingToLocation();
                case SETTING_UP_CANNON:
                    return handleSettingUpCannon();
                case COMBAT:
                    return handleCombat();
                case TASK_COMPLETE:
                    return handleTaskComplete();
                case ERROR:
                    return handleError();
                case STOPPED:
                    return false;
                default:
                    log.error("Unknown state: {}", currentState);
                    updateState(SlayerBotState.ERROR, "Unknown state encountered");
                    return false;
            }
            
        } catch (Exception e) {
            log.error("Error in SlayerTaskManager", e);
            reportError("Unexpected error in task manager", e);
            return false;
        }
    }
    
    private boolean handleInitializing() {
        log.info("Initializing slayer task manager");
        updateState(SlayerBotState.CHECKING_TASK, "Checking current slayer task");
        return true;
    }
    
    private boolean handleCheckingTask() {
        if (!Rs2Slayer.hasSlayerTask()) {
            log.error("No active slayer task found");
            updateState(SlayerBotState.GETTING_TASK, "Getting new slayer task");
            return true;
        }
        
        String taskName = Rs2Slayer.getSlayerTask();
        log.info("Current slayer task: {}", taskName);
        
        // Create task configuration
        currentTaskConfig = TaskConfigurationFactory.createConfiguration(taskName);
        initializeHandlers();
        
        // Find task location
        currentTaskLocation = Rs2Slayer.getSlayerTaskLocation(3, true);
        if (currentTaskLocation == null) {
            log.error("Could not find location for task: {}", taskName);
            updateState(SlayerBotState.ERROR, "Cannot find task location");
            return false;
        }
        
        log.error("Task location found: {}", currentTaskLocation.getClosestToCenter());
        
        // Check if we need to bank before starting
        if (bankingHandler.needsToBankBefore()) {
            log.error("Updating state to BANKING. Need to prepare for task.");
            updateState(SlayerBotState.BANKING, "Preparing for task");
        } else {
            updateState(SlayerBotState.TRAVELING_TO_LOCATION, "Traveling to task location");
        }
        
        taskStartTime = System.currentTimeMillis();
        return true;
    }
    
    private boolean handleGettingTask() {
        log.info("Getting new slayer task from slayer master");
        
        try {
            // Get the configured slayer master
            SlayerMaster slayerMaster = getConfiguredSlayerMaster();
            if (slayerMaster == null || slayerMaster == SlayerMaster.NONE) {
                log.error("No slayer master configured");
                updateState(SlayerBotState.ERROR, "No slayer master configured");
                return false;
            }
            
            log.info("Walking to slayer master: {}", slayerMaster.getName());
            
            // Walk to slayer master
            if (!Rs2Walker.walkTo(slayerMaster.getWorldPoint())) {
                log.error("Failed to walk to slayer master: {}", slayerMaster.getName());
                updateState(SlayerBotState.ERROR, "Cannot reach slayer master");
                return false;
            }
            
            // Wait a moment to arrive
            Global.sleep(2000);
            
            // Check if we're close enough to the slayer master
            WorldPoint playerLocation = Rs2Player.getWorldLocation();
            if (playerLocation.distanceTo(slayerMaster.getWorldPoint()) > 5) {
                log.warn("Still too far from slayer master, continuing to walk");
                return true; // Continue trying to walk
            }
            
            log.info("Getting assignment from slayer master: {}", slayerMaster.getName());
            
            // Talk to the slayer master to get a new task
            if (!Rs2Npc.interact(slayerMaster.getName(), "Assignment")) {
                log.error("Failed to get assignment from slayer master: {}", slayerMaster.getName());
                // Try alternative interaction
                if (!Rs2Npc.interact(slayerMaster.getName(), "Talk-to")) {
                    log.error("Failed to talk to slayer master");
                    updateState(SlayerBotState.ERROR, "Cannot talk to slayer master");
                    return false;
                }
            }
            
            // Wait for the task assignment dialog and interaction
            Global.sleep(3000);
            
            // Check if we now have a slayer task
            if (Rs2Slayer.hasSlayerTask()) {
                log.info("Successfully received new slayer task: {}", Rs2Slayer.getSlayerTask());
                updateState(SlayerBotState.CHECKING_TASK, "Received new task");
                return true;
            } else {
                log.warn("Did not receive a slayer task. Waiting a bit longer...");
                Global.sleep(2000);
                
                // Check again
                if (Rs2Slayer.hasSlayerTask()) {
                    log.info("Successfully received new slayer task: {}", Rs2Slayer.getSlayerTask());
                    updateState(SlayerBotState.CHECKING_TASK, "Received new task");
                    return true;
                } else {
                    log.error("Failed to receive slayer task from master");
                    updateState(SlayerBotState.ERROR, "No task received from slayer master");
                    return false;
                }
            }
            
        } catch (Exception e) {
            log.error("Error while getting new slayer task", e);
            updateState(SlayerBotState.ERROR, "Error getting new task: " + e.getMessage());
            return false;
        }
    }
    
    private boolean handleBanking() {
        if (bankingHandler.execute()) {
            log.info("Banking completed successfully");
            updateState(SlayerBotState.TRAVELING_TO_LOCATION, "Traveling to task location");
            return true;
        } else {
            log.error("Banking failed");
            updateState(SlayerBotState.ERROR, "Banking failed");
            return false;
        }
    }
    
    private boolean handleTravelingToLocation() {
        if (currentTaskLocation == null) {
            log.error("No task location set");
            updateState(SlayerBotState.ERROR, "No task location");
            return false;
        }
        
        // Check if we're already at the location
        if (Rs2Player.getWorldLocation().distanceTo(currentTaskLocation.getClosestToCenter()) <= 10) {
            log.info("Arrived at task location");
            
            // Set up cannon if needed
            if (cannonHandler.canExecute()) {
                updateState(SlayerBotState.SETTING_UP_CANNON, "Setting up cannon");
            } else {
                updateState(SlayerBotState.COMBAT, "Starting combat");
            }
            return true;
        }
        
        // Walk to the location
        log.info("Walking to task location: {}", currentTaskLocation.getClosestToCenter());
        boolean walkSuccess = Rs2Walker.walkTo(currentTaskLocation.getClosestToCenter());
        
        if (!walkSuccess) {
            log.error("Failed to walk to task location");
            updateState(SlayerBotState.ERROR, "Cannot reach task location");
            return false;
        }
        
        return true;
    }
    
    private boolean handleSettingUpCannon() {
        if (cannonHandler.execute()) {
            log.info("Cannon setup completed");
            updateState(SlayerBotState.COMBAT, "Starting combat");
            return true;
        } else {
            log.warn("Cannon setup failed, continuing without cannon");
            updateState(SlayerBotState.COMBAT, "Starting combat");
            return true; // Continue even if cannon setup fails
        }
    }
    
    private boolean handleCombat() {
        // Check if task is complete first
        if (!Rs2Slayer.hasSlayerTask() || Rs2Slayer.getSlayerTaskSize() == 0) {
            updateState(SlayerBotState.TASK_COMPLETE, "Task completed");
            return true;
        }
        
        return combatHandler.execute();
    }
    
    private boolean handleTaskComplete() {
        log.info("Slayer task completed!");
        
        // Pick up cannon if placed
        if (cannonHandler != null && cannonHandler.canExecute()) {
            // cannonHandler.pickupCannon(); // Method not available in API
        }
        
        // Report statistics
        long timeElapsed = System.currentTimeMillis() - taskStartTime;
        reportTaskComplete(monstersKilled, timeElapsed);
        
        // Reset for next task
        resetForNewTask();
        
        updateState(SlayerBotState.CHECKING_TASK, "Checking for new task");
        return true;
    }
    
    private boolean handleError() {
        log.error("Bot is in error state: {}", statusMessage);
        if (lastError != null) {
            log.error("Last error:", lastError);
        }
        
        // Could implement error recovery here
        // For now, we'll stop the bot
        updateState(SlayerBotState.STOPPED, "Bot stopped due to error");
        return false;
    }
    
    private void initializeHandlers() {
        if (currentTaskConfig == null) {
            log.error("Cannot initialize handlers without task configuration");
            return;
        }
        
        bankingHandler = new BankingHandler(config, currentTaskConfig, this);
        equipmentHandler = new EquipmentHandler(config, currentTaskConfig, this);
        inventoryHandler = new InventorySetupHandler(config, currentTaskConfig, this);
        combatHandler = new CombatHandler(config, currentTaskConfig, this);
        cannonHandler = new CannonHandler(config, currentTaskConfig, this);
        finishingBlowHandler = new FinishingBlowHandler(config, currentTaskConfig, this);
        
        log.info("Handlers initialized for task: {}", currentTaskConfig.getTaskName());
    }
    
    private void resetForNewTask() {
        currentTaskConfig = null;
        currentTaskLocation = null;
        monstersKilled = 0;
        taskStartTime = 0;
        lastError = null;
        
        // Don't reset handlers - they'll be recreated for the new task
        bankingHandler = null;
        equipmentHandler = null;
        inventoryHandler = null;
        combatHandler = null;
        cannonHandler = null;
        finishingBlowHandler = null;
    }
    
    /**
     * Gets the configured slayer master from the plugin config
     */
    private SlayerMaster getConfiguredSlayerMaster() {
        try {
            return config.slayerMaster();
        } catch (Exception e) {
            log.error("Error getting configured slayer master", e);
            return SlayerMaster.NIEVE; // Default fallback
        }
    }
    
    // StateCallback implementation
    @Override
    public void updateState(SlayerBotState newState) {
        updateState(newState, "");
    }
    
    @Override
    public void updateState(SlayerBotState newState, String statusMessage) {
        SlayerBotState previousState = this.currentState;
        this.currentState = newState;
        this.statusMessage = statusMessage;
        
        log.info("State changed: {} -> {} ({})", previousState, newState, statusMessage);
    }
    
    @Override
    public void reportError(String errorMessage, Exception exception) {
        log.error("Error reported: {}", errorMessage, exception);
        this.lastError = exception;
        updateState(SlayerBotState.ERROR, errorMessage);
    }
    
    @Override
    public void reportTaskComplete(int monstersKilled, long timeElapsed) {
        this.monstersKilled = monstersKilled;
        log.info("Task completed - Monsters killed: {}, Time elapsed: {}ms", monstersKilled, timeElapsed);
    }
    
    @Override
    public void requestStateTransition(SlayerBotState requestedState, String reason) {
        log.info("State transition requested: {} ({})", requestedState, reason);
        
        if (requestedState != null) {
            updateState(requestedState, reason);
        } else {
            // Handle special requests that don't map to specific states
            if (reason.contains("Need supplies") || reason.contains("restock")) {
                updateState(SlayerBotState.BANKING, reason);
            }
        }
    }
    
    @Override
    public void updateProgress(int currentCount, int targetCount, String action) {
        // This could be used to update UI or logging
        log.debug("Progress update: {}/{} ({})", currentCount, targetCount, action);
    }
    
    @Override
    public int getTaskCount() {
        return Rs2Slayer.getSlayerTaskSize(); // Get from game state
    }
    
    // Getters for external access
    public SlayerBotState getCurrentState() {
        return currentState;
    }
    
    public String getStatusMessage() {
        return statusMessage;
    }
    
    public TaskConfiguration getCurrentTaskConfig() {
        return currentTaskConfig;
    }
    
    public MonsterLocation getCurrentTaskLocation() {
        return currentTaskLocation;
    }
    
    public String getTaskInfo() {
        if (currentTaskConfig == null) {
            return "No active task";
        }
        
        StringBuilder info = new StringBuilder();
        info.append("Task: ").append(currentTaskConfig.getTaskName());
        
        if (currentTaskLocation != null) {
            info.append(" at ").append(currentTaskLocation.getClosestToCenter());
        }
        
        if (Rs2Slayer.hasSlayerTask()) {
            info.append(" (").append(Rs2Slayer.getSlayerTaskSize()).append(" remaining)");
        }
        
        return info.toString();
    }
}
