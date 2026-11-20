package com.adjustclient.utilities.anticheat.model;
import com.adjustclient.Adjust;
import com.adjustclient.utilities.server.ServerType;
import com.adjustclient.utilities.anticheat.blocker.BlockerState;
import com.adjustclient.utilities.anticheat.impl.PolarAntiCheat;
import com.adjustclient.utilities.game.mineplex.MineplexGame;
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
