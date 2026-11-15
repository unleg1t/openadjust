package com.adjustclient.module.impl.player.nofall;

import com.adjustclient.event.Listener;
import com.adjustclient.setting.ModeOption;
import com.adjustclient.util.MinecraftInstance;

public abstract class NoFallMode implements Listener, MinecraftInstance, ModeOption {
    public final NoFallModule module;
    public final String name;
    protected NoFallMode(NoFallModule module, String name) { this.module = module; this.name = name; }
    public String getName() { return name; }
    public boolean isActive() { return module.isActive() && module.mode.getValue() == this; }
}
