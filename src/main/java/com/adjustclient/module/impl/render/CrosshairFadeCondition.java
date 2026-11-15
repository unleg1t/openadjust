package com.adjustclient.module.impl.render;

import java.util.function.BooleanSupplier;
import com.adjustclient.setting.BooleanSetting;

public final class CrosshairFadeCondition implements BooleanSupplier {
    private final CrosshairModule module;
    public CrosshairFadeCondition(CrosshairModule module) { this.module = module; }
    @Override public boolean getAsBoolean() { return ((BooleanSetting) module.inactivityFadeEnabled).isEnabled(); }
}
