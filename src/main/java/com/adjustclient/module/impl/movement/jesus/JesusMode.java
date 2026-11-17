package com.adjustclient.module.impl.movement.jesus;

import com.adjustclient.event.Listener;
import com.adjustclient.setting.ModeOption;
import com.adjustclient.util.MinecraftInstance;

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
