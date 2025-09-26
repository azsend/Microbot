package net.runelite.client.plugins.microbot.azsend.aioslayer;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcManager;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Mocrosoft + "AIO Slayer",
        description = "All-in-one slayer bot that automates slayer tasks from start to finish",
        tags = {"slayer", "combat", "microbot", "aio"},
        enabledByDefault = false
)
@Slf4j
public class AioSlayerPlugin extends Plugin {
    
    @Inject
    private AioSlayerConfig config;
    
    @Provides
    AioSlayerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(AioSlayerConfig.class);
    }
    
    @Inject
    private OverlayManager overlayManager;
    
    @Inject
    private AioSlayerOverlay aioSlayerOverlay;
    
    @Inject
    AioSlayerScript aioSlayerScript;

    @Override
    protected void startUp() throws AWTException {
        log.error("!!! AIO SLAYER PLUGIN STARTING UP - NEW VERSION WITH DEBUG LOGS !!!");
        
        // Initialize Rs2NpcManager before starting the script
        try {
            Rs2NpcManager.loadJson();
            log.info("Rs2NpcManager initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Rs2NpcManager", e);
        }
        
        if (overlayManager != null) {
            overlayManager.add(aioSlayerOverlay);
        }
        aioSlayerScript.run(config);
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (aioSlayerScript != null) {
            aioSlayerScript.onChatMessage(event);
        }
    }

    @Override
    protected void shutDown() {
        aioSlayerScript.shutdown();
        overlayManager.remove(aioSlayerOverlay);
    }
}
