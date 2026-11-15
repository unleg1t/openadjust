package com.adjustclient.module.impl.player.fastuse;

import com.adjustclient.module.impl.player.fastuse.feature.AutomationFeature;

public abstract class FastUseFeature implements com.adjustclient.event.Listener, com.adjustclient.util.MinecraftInstance {
    public final FastUseModule module;

    protected FastUseFeature(FastUseModule module) { this.module = module; }

    public void onEnable() { }

    public void onDisable() { }

    public abstract boolean isFeatureEnabled();

    public boolean isActive() {
        if (!module.isEnabled()) return false;
        if (this instanceof AutomationFeature) return true;
        return isFeatureEnabled();
    }
}
