package com.adjustclient.modules.impl.player;

import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.PreClientTickEvent;
import com.adjustclient.modules.Category;
import com.adjustclient.modules.Module;
import net.minecraft.client.gui.GuiGameOver;

public final class AutoRespawnModule extends Module {
    public static AutoRespawnModule INSTANCE;

    public AutoRespawnModule() {
        super("Auto Respawn", Category.PLAYER);
        INSTANCE = this;
    }

    @EventHandler
    public void onTick(PreClientTickEvent event) {
        if (!isEnabled()) return;
        if (mc.thePlayer != null && mc.currentScreen instanceof GuiGameOver) {
            mc.thePlayer.respawnPlayer();
            mc.displayGuiScreen(null);
        }
    }
}
