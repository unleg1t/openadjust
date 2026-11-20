package com.adjustclient.modules.impl.combat.autoblock;

import com.adjustclient.core.events.Listener;
import com.adjustclient.core.settings.ModeOption;
import com.adjustclient.utilities.MinecraftInstance;

public abstract class AutoBlockMode implements Listener, MinecraftInstance, ModeOption {
    protected final AutoBlockModule module;
    protected final String name;
    protected AutoBlockMode(AutoBlockModule module, String name) { this.module = module; this.name = name; }
    @Override public String getName() { return name; }
    public boolean isEnabled() { return module.isEnabled() && module.mode.getValue() == this; }
}
