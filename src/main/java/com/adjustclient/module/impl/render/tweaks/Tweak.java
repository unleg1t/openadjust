package com.adjustclient.module.impl.render.tweaks;

import com.adjustclient.event.Listener;
import com.adjustclient.util.MinecraftInstance;

public abstract class Tweak implements Listener, MinecraftInstance {
    public final TweaksModule module;

    protected Tweak(TweaksModule module) {
        this.module = module;
    }

    protected void onEnable() { }

    protected void onDisable() { }

    public boolean isEnabled() {
        return module.isEnabled() && enabledSetting();
    }

    /** Implemented by each tweak through its setting object. */
    protected abstract boolean enabledSetting();
}
