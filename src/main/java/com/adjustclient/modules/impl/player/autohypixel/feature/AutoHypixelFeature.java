package com.adjustclient.modules.impl.player.autohypixel.feature;

import com.adjustclient.core.events.Listener;
import com.adjustclient.modules.impl.player.autohypixel.AutoHypixelModule;
import com.adjustclient.utilities.server.ServerType;
import com.adjustclient.utilities.MinecraftInstance;
import com.adjustclient.utilities.anticheat.ServerTracker;

public abstract class AutoHypixelFeature implements Listener, MinecraftInstance {
    public final AutoHypixelModule owner;
    protected AutoHypixelFeature(AutoHypixelModule owner) { this.owner = owner; }
    protected abstract boolean isFeatureEnabled();

    public boolean isActive() {
        return owner.isEnabled() && ServerTracker.INSTANCE.serverState != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel.serverType == ServerType.HYPIXEL
                && isFeatureEnabled();
    }
}
