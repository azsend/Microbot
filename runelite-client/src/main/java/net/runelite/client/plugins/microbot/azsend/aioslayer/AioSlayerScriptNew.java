package net.runelite.client.plugins.microbot.azsend.aioslayer;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.azsend.aioslayer.enums.SlayerBotState;
import net.runelite.client.plugins.microbot.azsend.aioslayer.handlers.SlayerTaskManager;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.antiban.enums.Activity;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Modernized AIO Slayer Script using modular handler system
 */
@Slf4j
public class AioSlayerScriptNew extends Script {
    
    public static SlayerBotState botState = SlayerBotState.INITIALIZING;
    public static String currentTask = "";
    public static int taskCount = 0;
    public static int tasksCompleted = 0;
    public static Instant startTime;
    
    private AioSlayerConfig config;
    private SlayerTaskManager taskManager;
    
    public boolean run(AioSlayerConfig config) {
        this.config = config;
        this.taskManager = new SlayerTaskManager(config);
        
        // Initialize bot settings
        Microbot.enableAutoRunOn = false;
        Rs2Antiban.resetAntibanSettings();
        Rs2Antiban.setActivity(Activity.GENERAL_SLAYER);
        startTime = Instant.now();
        botState = SlayerBotState.INITIALIZING;
        
        log.info("Starting AIO Slayer Bot with modular architecture");
        
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!super.run() || !Microbot.isLoggedIn() || Rs2AntibanSettings.actionCooldownActive) {
                    return;
                }
                
                // Update bot state from task manager
                botState = taskManager.getCurrentState();
                currentTask = taskManager.getTaskInfo();
                
                // Execute the task manager
                boolean success = taskManager.execute();
                
                if (!success && botState == SlayerBotState.ERROR) {
                    log.error("Task manager execution failed, stopping bot");
                    shutdown();
                }
                
            } catch (Exception ex) {
                log.error("Error in AioSlayerScript: " + ex.getMessage(), ex);
                botState = SlayerBotState.ERROR;
                shutdown();
            }
        }, 0, 600, TimeUnit.MILLISECONDS);

        return true;
    }
    
    /**
     * Handles chat messages to detect task completion and other events
     */
    public void onChatMessage(ChatMessage event) {
        if (event.getType() != ChatMessageType.GAMEMESSAGE) {
            return;
        }

        String message = event.getMessage();
        
        // Handle task completion messages
        if (message.contains("You have completed your task")) {
            log.info("Task completion detected from chat message");
            botState = SlayerBotState.TASK_COMPLETE;
            tasksCompleted++;
            
            // Notify task manager if available
            if (taskManager != null) {
                taskManager.updateState(SlayerBotState.TASK_COMPLETE, "Task completed");
            }
        }
        
        // Handle level up messages
        if (message.contains("Your Slayer level is now")) {
            log.info("Slayer level up detected!");
        }
        
        // Handle new task assignment messages
        if (message.contains("Your new task is to kill")) {
            log.info("New task assignment detected from chat message");
            if (taskManager != null) {
                taskManager.updateState(SlayerBotState.CHECKING_TASK, "New task assigned");
            }
        }
    }

    @Override
    public void shutdown() {
        log.info("Shutting down AIO Slayer Bot");
        
        // Calculate runtime
        if (startTime != null) {
            long runtimeMs = Instant.now().toEpochMilli() - startTime.toEpochMilli();
            long runtimeMinutes = runtimeMs / (1000 * 60);
            log.info("Bot ran for {} minutes", runtimeMinutes);
            log.info("Tasks completed: {}", tasksCompleted);
        }
        
        super.shutdown();
    }
    
    // Getters for overlay and external access
    public SlayerBotState getBotState() {
        return botState;
    }
    
    public String getCurrentTask() {
        return currentTask;
    }
    
    public int getTaskCount() {
        return taskCount;
    }
    
    public int getTasksCompleted() {
        return tasksCompleted;
    }
    
    public Instant getStartTime() {
        return startTime;
    }
    
    public String getStatusMessage() {
        if (taskManager != null) {
            return taskManager.getStatusMessage();
        }
        return "Initializing...";
    }
    
    public String getTaskInfo() {
        if (taskManager != null) {
            return taskManager.getTaskInfo();
        }
        return "No task information available";
    }
}
