package com.adjustclient.module.impl.render.bedplates;

import com.adjustclient.event.EventHandler;
import com.adjustclient.event.Listener;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;

public final class BedPlatesModule extends Module implements Listener {
    public static BedPlatesModule INSTANCE;
    public final BedPlatesSettings settings;
    public final BedPlatesRenderer renderer = new BedPlatesRenderer(this);
    public volatile BedTracker tracker;

    @EventHandler public void onTick(com.adjustclient.event.CancellablePlayerUpdateEvent event) {
        if (event == null || event.isCancelled() || !isEnabled()) return;
        BedTracker currentTracker = tracker;
        if (currentTracker == null) return;
        var server = com.adjustclient.util.anticheat.ServerTracker.INSTANCE == null
                ? null : com.adjustclient.util.anticheat.ServerTracker.INSTANCE.serverState;
        if (server != null && server.antiCheatModel != null
                && server.antiCheatModel.serverType == com.adjustclient.server.ServerType.HYPIXEL) {
            var state = com.adjustclient.Adjust.gameStateTracker == null
                    ? null : com.adjustclient.Adjust.gameStateTracker.state;
            if (state == null || !Boolean.FALSE.equals(state.lobby)
                    || state.game != com.adjustclient.hypixel.HypixelGame.BED_WARS) {
                currentTracker.clear();
                return;
            }
        }
        currentTracker.tick();
    }

    @EventHandler public void onBlockState(com.adjustclient.event.BlockRenderStateEvent event) {
        if (event == null || !isEnabled()) return;
        BedTracker currentTracker = tracker;
        if (currentTracker == null) return;
        var server = com.adjustclient.util.anticheat.ServerTracker.INSTANCE == null
                ? null : com.adjustclient.util.anticheat.ServerTracker.INSTANCE.serverState;
        if (server != null && server.antiCheatModel != null
                && server.antiCheatModel.serverType == com.adjustclient.server.ServerType.HYPIXEL) {
            var state = com.adjustclient.Adjust.gameStateTracker == null
                    ? null : com.adjustclient.Adjust.gameStateTracker.state;
            if (state == null || !Boolean.FALSE.equals(state.lobby)
                    || state.game != com.adjustclient.hypixel.HypixelGame.BED_WARS) return;
        }
        currentTracker.onBlockChange(event);
    }

    @Override public void onEnable() {
        tracker = new BedTracker(this);
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        if (mc.renderGlobal != null) {
            if (mc.isCallingFromMinecraftThread()) mc.renderGlobal.loadRenderers();
            else mc.addScheduledTask(com.adjustclient.render.RenderUtil.outlineRefresh(mc));
        }
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
