package com.adjustclient.util.anticheat.model;
import com.adjustclient.Adjust;
import com.adjustclient.server.ServerType;
import com.adjustclient.util.anticheat.blocker.BlockerState;
import com.adjustclient.anticheat.impl.PolarAntiCheat;
import com.adjustclient.game.mineplex.MineplexGame;
public final class PolarModel extends AntiCheatModel {
    public Object field20;
    public PolarModel(ServerType serverType) { super(serverType); }
    public void setServerData(BlockerState server) {
        super.setServerData(server);
        if (server.featureBlocker.getAntiCheat(PolarAntiCheat.class) != null)
            throw new IllegalStateException("Anti-cheat model already registered");
        server.featureBlocker.antiCheats.put(PolarAntiCheat.class, new PolarAntiCheat(() -> Adjust.mineplexGameTracker.state.game
                        == MineplexGame.LOBBY)
                );
    }
    public boolean allowsCapabilityC() { return (Adjust.mineplexGameTracker.state.game.flags & 4) == 0; }
    public boolean allowsCapabilityD() { return true; }
}
