package com.adjustclient.module.impl.player;

import com.adjustclient.event.EventHandler;
import com.adjustclient.event.PreClientTickEvent;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
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
