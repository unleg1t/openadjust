package com.adjustclient.modules.impl.render;

import java.util.function.BooleanSupplier;
import com.adjustclient.core.settings.BooleanSetting;

public final class AtmosphereTimeCondition implements BooleanSupplier {
    private final AtmosphereModule module;
    public AtmosphereTimeCondition(AtmosphereModule module) { this.module = module; }
    @Override public boolean getAsBoolean() { return ((BooleanSetting) module.timeEnabled).isEnabled(); }
}
