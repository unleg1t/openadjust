package com.adjustclient.module.impl.render;

import java.util.function.BooleanSupplier;
import com.adjustclient.setting.BooleanSetting;

public final class DamageParticlesColorCondition implements BooleanSupplier {
    private final DamageParticlesModule module;
    public DamageParticlesColorCondition(DamageParticlesModule module) { this.module = module; }
    @Override public boolean getAsBoolean() { return ((BooleanSetting) module.customColorEnabled).isEnabled(); }
}
