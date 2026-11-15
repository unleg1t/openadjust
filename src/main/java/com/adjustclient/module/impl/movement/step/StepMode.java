package com.adjustclient.module.impl.movement.step;

import com.adjustclient.anticheat.feature.Feature;
import com.adjustclient.event.Listener;
import com.adjustclient.setting.ModeOption;
import com.adjustclient.util.MinecraftInstance;

public abstract class StepMode implements Listener, MinecraftInstance, ModeOption {
    protected final StepModule module;
    protected final String name;
    protected final Feature feature;

    protected StepMode(StepModule module, String name) {
        this.module = module;
        this.name = name;
        this.feature = "Vanilla".equals(name) ? com.adjustclient.anticheat.feature.Features.STEP_VANILLA
                : "NCP".equals(name) ? com.adjustclient.anticheat.feature.Features.STEP_NCP
                : com.adjustclient.anticheat.feature.Features.STEP_MOTION;
    }

    @Override public String getName() { return name; }

    public boolean isActive() {
        return module.isEnabled() && module.mode.getValue() == this && feature.isAllowed();
    }
}
