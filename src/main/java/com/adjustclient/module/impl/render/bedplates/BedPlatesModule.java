package com.adjustclient.module.impl.render.bedplates;

import com.adjustclient.event.EventHandler;
import com.adjustclient.event.Listener;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;

public final class BedPlatesModule extends Module implements Listener {
    public static BedPlatesModule INSTANCE;
    public final BedPlatesSettings settings;
    public final BedPlatesRenderer renderer = new BedPlatesRenderer(this);
    public BedTracker tracker;

    @EventHandler public void onTick(com.adjustclient.event.CancellablePlayerUpdateEvent event) {
        if (event.isCancelled() || !isEnabled()) return;
        var server = com.adjustclient.util.anticheat.ServerTracker.INSTANCE.serverState;
        if (server != null && server.antiCheatModel != null
                && server.antiCheatModel.serverType == com.adjustclient.server.ServerType.HYPIXEL) {
            var state = com.adjustclient.Adjust.gameStateTracker.state;
            if (!Boolean.FALSE.equals(state.lobby)
                    || state.game != com.adjustclient.hypixel.HypixelGame.BED_WARS) {
                tracker.clear();
                return;
            }
        }
        tracker.tick();
    }

    @EventHandler public void onBlockState(com.adjustclient.event.BlockRenderStateEvent event) {
        if (!isEnabled()) return;
        var server = com.adjustclient.util.anticheat.ServerTracker.INSTANCE.serverState;
        if (server != null && server.antiCheatModel != null
                && server.antiCheatModel.serverType == com.adjustclient.server.ServerType.HYPIXEL) {
            var state = com.adjustclient.Adjust.gameStateTracker.state;
            if (!Boolean.FALSE.equals(state.lobby)
                    || state.game != com.adjustclient.hypixel.HypixelGame.BED_WARS) return;
        }
        tracker.onBlockChange(event);
    }

    @Override public void onEnable() {
        tracker = new BedTracker(this);
        net.minecraft.client.Minecraft.getMinecraft().renderGlobal.loadRenderers();
    }

    @Override public void onDisable() {
        tracker = null;
    }

    public BedPlatesModule() {
        super("Bed Plates", Category.RENDER);
        INSTANCE = this;
        settings = new BedPlatesSettings(this);
        setHidden(false);
    }
}
