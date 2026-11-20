package com.adjustclient.utilities.anticheat.model;
import com.adjustclient.Adjust;
import com.adjustclient.utilities.server.ServerType;
import com.adjustclient.utilities.anticheat.blocker.BlockerState;
import com.adjustclient.utilities.anticheat.impl.AntiGamingChairAntiCheat;
public final class AntiGamingChairModel extends AntiCheatModel {
    public Object field20;
    public AntiGamingChairModel(ServerType serverType) { super(serverType); }
    public void setServerData(BlockerState server) {
        super.setServerData(server);
        if (server.featureBlocker.getAntiCheat(AntiGamingChairAntiCheat.class) != null)
            throw new IllegalStateException("Anti-cheat model already registered");
        server.featureBlocker.antiCheats.put(AntiGamingChairAntiCheat.class, new AntiGamingChairAntiCheat(com.adjustclient.utilities.anticheat.feature.Features.DOUBLE_JUMP_MMC));
    }
    public boolean allowsCapabilityC() { return true; }
    public boolean allowsCapabilityD() { return true; }
}
