package com.adjustclient.utilities.anticheat.feature;

import com.adjustclient.utilities.anticheat.AntiCheat;
import com.adjustclient.utilities.anticheat.ServerTracker;
import com.adjustclient.utilities.anticheat.blocker.FeatureBlocker;
import com.adjustclient.modules.impl.player.AntiCheatModule;

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
