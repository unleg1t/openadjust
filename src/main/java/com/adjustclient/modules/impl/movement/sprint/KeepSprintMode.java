package com.adjustclient.modules.impl.movement.sprint;

import com.adjustclient.core.events.Listener;
import com.adjustclient.core.settings.ModeOption;
import com.adjustclient.utilities.MinecraftInstance;

public abstract class KeepSprintMode implements Listener, MinecraftInstance, ModeOption {
    protected final SprintModule module;
    private final String name;

    protected KeepSprintMode(SprintModule module, String name) { this.module = module; this.name = name; }

    @Override public String getName() { return name; }

    public boolean isActive() {
        return module.isEnabled() && module.keepSprintEnabled.isEnabled() && module.keepSprintMode.getValue() == this;
    }

    public void onEnable() { }
    public void onDisable() { }
}
