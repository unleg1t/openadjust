package com.adjustclient.modules.impl.movement.flight;

import com.adjustclient.core.events.Listener;
import com.adjustclient.core.settings.ModeOption;
import com.adjustclient.utilities.MinecraftInstance;

public abstract class FlightMode implements Listener, MinecraftInstance, ModeOption {
    protected final FlightModule module;
    protected final String name;

    protected FlightMode(FlightModule module, String name) {
        this.module = module;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public boolean isActive() {
        return module.isEnabled() && module.mode.getValue() == this;
    }
}
