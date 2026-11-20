package com.adjustclient.modules.impl.player.autorewound.feature;

import com.adjustclient.core.events.Listener;
import com.adjustclient.modules.impl.player.autorewound.AutoRewoundModule;
import com.adjustclient.utilities.server.ServerType;
import com.adjustclient.utilities.MinecraftInstance;
import com.adjustclient.utilities.anticheat.ServerTracker;

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
