package com.adjustclient.anticheat.feature;

import com.adjustclient.anticheat.AntiCheat;
import com.adjustclient.util.anticheat.ServerTracker;
import com.adjustclient.util.anticheat.blocker.FeatureBlocker;
import com.adjustclient.module.impl.player.AntiCheatModule;

public class Feature {
    public String name;
    public boolean unrestricted;
    public Object field12;

    public Feature(String name, boolean unrestricted) {
        this.name = name;
        this.unrestricted = unrestricted;
    }

    public boolean isAllowed() {
        if (!AntiCheatModule.restrictionsEnabled) return true;
        var server = ServerTracker.INSTANCE.serverState;
        if (server == null || server.featureBlocker == null) return true;
        FeatureBlocker blocker = server.featureBlocker;
        for (AntiCheat antiCheat : blocker.antiCheats.values()) {
            if (!antiCheat.isAllowed(this)) {
                blocker.reportBlocked(antiCheat, this);
                return false;
            }
        }
        return true;
    }
}
