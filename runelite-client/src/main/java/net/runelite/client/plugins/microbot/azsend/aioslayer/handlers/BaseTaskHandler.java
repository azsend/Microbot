package net.runelite.client.plugins.microbot.azsend.aioslayer.handlers;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.microbot.azsend.aioslayer.AioSlayerConfig;
import net.runelite.client.plugins.microbot.azsend.aioslayer.models.TaskConfiguration;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;

/**
 * Base class for all task handlers providing common functionality
 */
@Slf4j
public abstract class BaseTaskHandler {
    
    protected final AioSlayerConfig config;
    protected final TaskConfiguration taskConfig;
    protected final StateCallback stateCallback;
    
    protected BaseTaskHandler(AioSlayerConfig config, TaskConfiguration taskConfig, StateCallback stateCallback) {
        this.config = config;
        this.taskConfig = taskConfig;
        this.stateCallback = stateCallback;
    }
    
    /**
     * Execute the handler's primary function
     * @return true if the handler completed successfully, false if it needs to retry or failed
     */
    public abstract boolean execute();
    
    /**
     * Check if the handler can execute (prerequisites met)
     * @return true if ready to execute, false otherwise
     */
    public abstract boolean canExecute();
    
    /**
     * Get the name of this handler for logging
     */
    public abstract String getHandlerName();
    
    /**
     * Cleanup method called when handler is finished
     */
    public void cleanup() {
        // Default implementation - subclasses can override
    }
    
    /**
     * Common method to handle antiban during long operations
     */
    protected void handleAntiban() {
        Rs2Antiban.actionCooldown();
        Rs2Antiban.takeMicroBreakByChance();
    }
    
    /**
     * Log handler-specific messages with consistent format
     */
    protected void logInfo(String message) {
        log.info("[{}] {}", getHandlerName(), message);
    }
    
    protected void logError(String message) {
        log.error("[{}] {}", getHandlerName(), message);
    }
    
    protected void logError(String message, Exception e) {
        log.error("[{}] {}", getHandlerName(), message, e);
    }
    
    /**
     * Sleep with antiban considerations
     */
    protected void sleep(int baseMs) {
        net.runelite.client.plugins.microbot.util.Global.sleep(baseMs);
    }
    
    /**
     * Sleep until condition is met
     */
    protected boolean sleepUntil(java.util.function.BooleanSupplier condition, int timeoutMs) {
        return net.runelite.client.plugins.microbot.util.Global.sleepUntil(condition, timeoutMs);
    }
}
