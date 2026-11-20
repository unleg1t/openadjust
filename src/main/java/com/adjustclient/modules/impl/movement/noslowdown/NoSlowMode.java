package com.adjustclient.modules.impl.movement.noslowdown;

import com.adjustclient.utilities.anticheat.feature.Feature;
import com.adjustclient.core.events.Listener;
import com.adjustclient.core.settings.ModeOption;
import com.adjustclient.utilities.MinecraftInstance;

public abstract class NoSlowMode implements Listener, MinecraftInstance, ModeOption {
    protected final NoSlowdownModule module;
    protected final String name;
    public final Feature feature;

    protected NoSlowMode(NoSlowdownModule module, String name, Feature feature) {
        this.module = module;
        this.name = name;
        this.feature = feature;
    }

    public void onDisable() {
    }

    @Override
    public String getName() {
        return name;
    }

    public void onEnable() {
    }

    public boolean isActive() {
        return module.isEnabled() && module.mode.getValue() == this && feature.isAllowed();
    }

    public abstract boolean removesSlowdown();
    public abstract boolean isBlocking();
}
