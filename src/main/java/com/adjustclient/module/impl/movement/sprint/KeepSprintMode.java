package com.adjustclient.module.impl.movement.sprint;

import com.adjustclient.event.Listener;
import com.adjustclient.setting.ModeOption;
import com.adjustclient.util.MinecraftInstance;

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
