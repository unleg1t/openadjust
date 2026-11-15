package com.adjustclient.util.anticheat.model;
import com.adjustclient.server.ServerType;
import com.adjustclient.util.anticheat.blocker.BlockerState;
import com.adjustclient.anticheat.impl.GrimAntiCheat;
import com.adjustclient.anticheat.impl.VulcanAntiCheat;
public final class VulcanGrimModel extends AntiCheatModel {
    public Object field20;
    public VulcanGrimModel(ServerType serverType) { super(serverType); }
    public void setServerData(BlockerState server) {
        super.setServerData(server);
        if (server.featureBlocker.getAntiCheat(GrimAntiCheat.class) != null)
            throw new IllegalStateException("Anti-cheat model already registered");
        server.featureBlocker.antiCheats.put(GrimAntiCheat.class, new GrimAntiCheat());
        if (server.featureBlocker.getAntiCheat(VulcanAntiCheat.class) != null)
            throw new IllegalStateException("Anti-cheat model already registered");
        server.featureBlocker.antiCheats.put(VulcanAntiCheat.class, new VulcanAntiCheat(
                com.adjustclient.anticheat.feature.Features.VELOCITY_HORIZONTAL,
                com.adjustclient.anticheat.feature.Features.TERRAIN_VULCAN,
                com.adjustclient.anticheat.feature.Features.NO_SLOW_SWAP,
                com.adjustclient.anticheat.feature.Features.ANTI_VOID_BLINK,
                com.adjustclient.anticheat.feature.Features.STEP_MOTION,
                com.adjustclient.anticheat.feature.Features.NO_MOVEMENT_FIX));
    }
    public boolean allowsCapabilityC() { return true; }
    public boolean allowsCapabilityD() { return false; }
}
