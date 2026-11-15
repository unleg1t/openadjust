package com.adjustclient.module.impl.movement.antivoid;

import com.adjustclient.event.Listener;
import com.adjustclient.setting.ModeOption;
import com.adjustclient.util.MinecraftInstance;

public abstract class AntiVoidMode implements Listener, MinecraftInstance, ModeOption {
    protected final AntiVoidModule module;
    protected final String name;
    protected float collisionWidth = 0.5F;

    protected AntiVoidMode(AntiVoidModule module, String name) {
        this.module = module;
        this.name = name;
    }

    @Override public String getName() { return name; }

    public boolean isActive() { return module.isEnabled() && module.mode.getValue() == this; }
}
