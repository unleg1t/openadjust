package com.adjustclient.modules.impl.movement.step;

import com.adjustclient.utilities.anticheat.feature.Feature;
import com.adjustclient.core.events.Listener;
import com.adjustclient.core.settings.ModeOption;
import com.adjustclient.utilities.MinecraftInstance;

public abstract class StepMode implements Listener, MinecraftInstance, ModeOption {
    protected final StepModule module;
    protected final String name;
    protected final Feature feature;

    protected StepMode(StepModule module, String name) {
        this.module = module;
        this.name = name;
        this.feature = "Vanilla".equals(name) ? com.adjustclient.utilities.anticheat.feature.Features.STEP_VANILLA
                : "NCP".equals(name) ? com.adjustclient.utilities.anticheat.feature.Features.STEP_NCP
                : com.adjustclient.utilities.anticheat.feature.Features.STEP_MOTION;
    }

    @Override public String getName() { return name; }

    public boolean isActive() {
        return module.isEnabled() && module.mode.getValue() == this && feature.isAllowed();
    }
}
