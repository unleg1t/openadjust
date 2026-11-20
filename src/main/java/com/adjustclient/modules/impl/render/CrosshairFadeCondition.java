package com.adjustclient.modules.impl.render;

import java.util.function.BooleanSupplier;
import com.adjustclient.core.settings.BooleanSetting;

public final class CrosshairFadeCondition implements BooleanSupplier {
    private final CrosshairModule module;
    public CrosshairFadeCondition(CrosshairModule module) { this.module = module; }
    @Override public boolean getAsBoolean() { return ((BooleanSetting) module.inactivityFadeEnabled).isEnabled(); }
}
