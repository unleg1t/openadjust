package com.adjustclient.modules.impl.movement.jesus;

import com.adjustclient.core.events.Listener;
import com.adjustclient.core.settings.ModeOption;
import com.adjustclient.utilities.MinecraftInstance;

public abstract class JesusMode implements Listener, MinecraftInstance, ModeOption {
    protected final JesusModule module;
    protected final String name;

    protected JesusMode(JesusModule module, String name) {
        this.module = module;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public boolean isActive() {
        return module.isEnabled() && module.mode.getValue() == this;
    }

    public void onEnable() {
    }

    public void onDisable() {
    }
}
