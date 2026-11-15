package com.adjustclient.module.impl.player.autohypixel.feature;

import com.adjustclient.event.Listener;
import com.adjustclient.module.impl.player.autohypixel.AutoHypixelModule;
import com.adjustclient.server.ServerType;
import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.util.anticheat.ServerTracker;

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
