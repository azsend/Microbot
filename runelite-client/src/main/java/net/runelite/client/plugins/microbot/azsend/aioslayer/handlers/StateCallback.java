package net.runelite.client.plugins.microbot.azsend.aioslayer.handlers;

import net.runelite.client.plugins.microbot.azsend.aioslayer.enums.SlayerBotState;

/**
 * Interface for handling state callbacks from task handlers to the main script
 */
public interface StateCallback {
    
    /**
     * Updates the bot state and optionally provides a status message
     */
    void updateState(SlayerBotState newState);
    
    /**
     * Updates the bot state with a custom status message
     */
    void updateState(SlayerBotState newState, String statusMessage);
    
    /**
     * Reports an error that occurred during task execution
     */
    void reportError(String errorMessage, Exception exception);
    
    /**
     * Reports task completion with statistics
     */
    void reportTaskComplete(int monstersKilled, long timeElapsed);
    
    /**
     * Requests a state transition (e.g., need to bank, need to restock)
     */
    void requestStateTransition(SlayerBotState requestedState, String reason);
    
    /**
     * Updates progress information
     */
    void updateProgress(int currentCount, int targetCount, String action);
    
    /**
     * Gets the current task count remaining
     */
    int getTaskCount();
}
