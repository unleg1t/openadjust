package com.adjustclient.module.impl.render;

import java.util.function.BooleanSupplier;
import com.adjustclient.setting.BooleanSetting;

public final class AtmosphereTimeCondition implements BooleanSupplier {
    private final AtmosphereModule module;
    public AtmosphereTimeCondition(AtmosphereModule module) { this.module = module; }
    @Override public boolean getAsBoolean() { return ((BooleanSetting) module.timeEnabled).isEnabled(); }
}
