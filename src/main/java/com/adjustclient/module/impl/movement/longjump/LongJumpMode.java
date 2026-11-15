package com.adjustclient.module.impl.movement.longjump;

import com.adjustclient.event.Listener;
import com.adjustclient.setting.ModeOption;
import com.adjustclient.util.MinecraftInstance;

public abstract class LongJumpMode implements Listener, MinecraftInstance, ModeOption {
    protected final LongJumpModule module;
    protected LongJumpMode(LongJumpModule module) { this.module = module; }

    public void onDisable() { }

    public boolean isActive() { return module.isEnabled() && module.mode.getValue() == this; }
    public abstract void onEnable();
}
