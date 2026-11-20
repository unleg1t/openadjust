package com.adjustclient.modules.impl.movement.longjump;

import com.adjustclient.core.events.Listener;
import com.adjustclient.core.settings.ModeOption;
import com.adjustclient.utilities.MinecraftInstance;

public abstract class LongJumpMode implements Listener, MinecraftInstance, ModeOption {
    protected final LongJumpModule module;
    protected LongJumpMode(LongJumpModule module) { this.module = module; }

    public void onDisable() { }

    public boolean isActive() { return module.isEnabled() && module.mode.getValue() == this; }
    public abstract void onEnable();
}
