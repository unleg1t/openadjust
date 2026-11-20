package com.adjustclient.modules.impl.player.fastuse;

import com.adjustclient.modules.impl.player.fastuse.feature.AutomationFeature;

public abstract class FastUseFeature implements com.adjustclient.core.events.Listener, com.adjustclient.utilities.MinecraftInstance {
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
