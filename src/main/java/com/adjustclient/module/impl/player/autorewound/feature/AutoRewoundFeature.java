package com.adjustclient.module.impl.player.autorewound.feature;

import com.adjustclient.event.Listener;
import com.adjustclient.module.impl.player.autorewound.AutoRewoundModule;
import com.adjustclient.server.ServerType;
import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.util.anticheat.ServerTracker;

public abstract class AutoRewoundFeature implements Listener, MinecraftInstance {
    public final AutoRewoundModule owner;

    protected AutoRewoundFeature(AutoRewoundModule owner) {
        this.owner = owner;
    }

    protected abstract boolean isFeatureEnabled();

    public boolean isActive() {
        return owner.isEnabled()
                && ServerTracker.INSTANCE.serverState != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel.serverType == ServerType.REWOUND
                && isFeatureEnabled();
    }
}
