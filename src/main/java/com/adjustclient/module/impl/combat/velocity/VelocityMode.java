package com.adjustclient.module.impl.combat.velocity;

import com.adjustclient.event.Listener;
import com.adjustclient.setting.ModeOption;
import com.adjustclient.util.MinecraftInstance;

public abstract class VelocityMode implements Listener, MinecraftInstance, ModeOption {
    protected final VelocityModule module;
    protected final String name;

    protected VelocityMode(VelocityModule module, String name) { this.module = module; this.name = name; }
    public void reset() { }
    @Override public String getName() { return name; }
    public String getSuffix() { return name; }
    public boolean isActive() { return module.isEnabled() && module.mode.getValue() == this; }
}
