package com.adjustclient.util.anticheat.model;
import com.adjustclient.Adjust;
import com.adjustclient.server.ServerType;
import com.adjustclient.util.anticheat.blocker.BlockerState;
import com.adjustclient.anticheat.impl.GrimAntiCheat;
public final class GrimModel extends AntiCheatModel {
    public Object field20;
    public GrimModel(ServerType serverType) { super(serverType); }
    public void setServerData(BlockerState server) {
        super.setServerData(server);
        if (server.featureBlocker.getAntiCheat(GrimAntiCheat.class) != null)
            throw new IllegalStateException("Anti-cheat model already registered");
        server.featureBlocker.antiCheats.put(GrimAntiCheat.class, new GrimAntiCheat());
    }
    public boolean allowsCapabilityC() { return true; }
    public boolean allowsCapabilityD() { return true; }
}
