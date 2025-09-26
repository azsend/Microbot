package net.runelite.client.plugins.microbot.azsend.aioslayer;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;
import java.time.Duration;
import java.time.Instant;

public class AioSlayerOverlay extends OverlayPanel {

    @Inject
    AioSlayerOverlay(AioSlayerPlugin plugin, AioSlayerConfig config) {
        super(plugin);
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

            if (AioSlayerScript.botState != null) {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("State:")
                        .right(AioSlayerScript.botState.getDescription())
                        .build());
            }

            if (AioSlayerScript.currentTask != null) {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Task:")
                        .right(AioSlayerScript.currentTask)
                        .build());
            }

            if (AioSlayerScript.taskCount > 0) {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Remaining:")
                        .right(String.valueOf(AioSlayerScript.taskCount))
                        .build());
            }

            if (AioSlayerScript.tasksCompleted > 0) {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Tasks Done:")
                        .right(String.valueOf(AioSlayerScript.tasksCompleted))
                        .build());
            }

            if (AioSlayerScript.startTime != null) {
                Duration runtime = Duration.between(AioSlayerScript.startTime, Instant.now());
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
