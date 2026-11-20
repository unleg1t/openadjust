package com.adjustclient.modules.impl.player.nofall;

import com.adjustclient.core.events.Listener;
import com.adjustclient.core.settings.ModeOption;
import com.adjustclient.utilities.MinecraftInstance;

public abstract class NoFallMode implements Listener, MinecraftInstance, ModeOption {
    public final NoFallModule module;
    public final String name;
    protected NoFallMode(NoFallModule module, String name) { this.module = module; this.name = name; }
    public String getName() { return name; }
    public boolean isActive() { return module.isActive() && module.mode.getValue() == this; }
}
