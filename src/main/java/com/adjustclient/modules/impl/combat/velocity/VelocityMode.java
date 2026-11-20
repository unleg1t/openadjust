package com.adjustclient.modules.impl.combat.velocity;

import com.adjustclient.core.events.Listener;
import com.adjustclient.core.settings.ModeOption;
import com.adjustclient.utilities.MinecraftInstance;

public abstract class VelocityMode implements Listener, MinecraftInstance, ModeOption {
    protected final VelocityModule module;
    protected final String name;

    protected VelocityMode(VelocityModule module, String name) { this.module = module; this.name = name; }
    public void reset() { }
    @Override public String getName() { return name; }
    public String getSuffix() { return name; }
    public boolean isActive() { return module.isEnabled() && module.mode.getValue() == this; }
}
