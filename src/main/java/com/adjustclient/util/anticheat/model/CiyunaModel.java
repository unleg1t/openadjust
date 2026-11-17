package com.adjustclient.util.anticheat.model;

import com.adjustclient.Adjust;
import com.adjustclient.server.ServerType;

public final class CiyunaModel extends AntiCheatModel {
    public CiyunaModel(ServerType serverType) {
        super(serverType);
    }

    @Override
    public boolean allowsCapabilityC() {
        var state = Adjust.rewoundGameTracker.state;
        return state.startTimer != null && !state.ended && !state.allowFlying && (state.game.flags & 0x20) == 0;
    }

    @Override
    public boolean allowsCapabilityD() {
        return true;
    }
}
