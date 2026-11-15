package com.adjustclient.module.impl.combat.autoblock;

import com.adjustclient.event.Listener;
import com.adjustclient.setting.ModeOption;
import com.adjustclient.util.MinecraftInstance;

public abstract class AutoBlockMode implements Listener, MinecraftInstance, ModeOption {
    protected final AutoBlockModule module;
    protected final String name;
    protected AutoBlockMode(AutoBlockModule module, String name) { this.module = module; this.name = name; }
    @Override public String getName() { return name; }
    public boolean isEnabled() { return module.isEnabled() && module.mode.getValue() == this; }
}
