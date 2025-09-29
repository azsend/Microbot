package net.runelite.client.plugins.microbot.azsend.aioslayer;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.skills.slayer.Rs2Slayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;
import java.time.Duration;
import java.time.Instant;

public class AioSlayerOverlay extends OverlayPanel {

    private final AioSlayerPlugin plugin;

    @Inject
    AioSlayerOverlay(AioSlayerPlugin plugin, AioSlayerConfig config) {
        super(plugin);
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            panelComponent.setPreferredSize(new Dimension(200, 300));
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("AIO Slayer")
                    .color(Color.GREEN)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder().build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Version:")
                    .right("1.0.0")
                    .build());

            // Display bot state
            if (AioSlayerScriptNew.botState != null) {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("State:")
                        .right(AioSlayerScriptNew.botState.getDescription())
                        .build());
            }

            // Display current task with count
            String currentTask = Rs2Slayer.getSlayerTask();
            int taskCount = Rs2Slayer.getSlayerTaskSize();
            
            if (currentTask != null && !currentTask.isEmpty()) {
                String taskDisplay = taskCount > 0 ? 
                    currentTask + " (" + taskCount + ")" : 
                    currentTask;
                    
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Task:")
                        .right(taskDisplay)
                        .build());
            } else if (AioSlayerScriptNew.currentTask != null && !AioSlayerScriptNew.currentTask.isEmpty()) {
                // Fallback to script's task info if Rs2Slayer doesn't have it
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Task:")
                        .right(AioSlayerScriptNew.currentTask)
                        .build());
            } else {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Task:")
                        .right("No Task")
                        .build());
            }

            if (AioSlayerScriptNew.tasksCompleted > 0) {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Tasks Done:")
                        .right(String.valueOf(AioSlayerScriptNew.tasksCompleted))
                        .build());
            }

            if (AioSlayerScriptNew.startTime != null) {
                Duration runtime = Duration.between(AioSlayerScriptNew.startTime, Instant.now());
                String formattedTime = String.format("%02d:%02d:%02d", 
                    runtime.toHours(), 
                    runtime.toMinutesPart(), 
                    runtime.toSecondsPart());
                
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Runtime:")
                        .right(formattedTime)
                        .build());
            }

            panelComponent.getChildren().add(LineComponent.builder().build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Status:")
                    .right(Microbot.status)
                    .build());

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return super.render(graphics);
    }
}
