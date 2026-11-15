package com.adjustclient.module.impl.render;

import java.util.function.BooleanSupplier;
import com.adjustclient.setting.BooleanSetting;

final class AtmosphereFogCondition implements BooleanSupplier {
    private final AtmosphereModule module;
    public AtmosphereFogCondition(AtmosphereModule module) { this.module = module; }
    @Override public boolean getAsBoolean() {
        BooleanSetting fogEnabled = module.fogEnabled;
        return fogEnabled.isEnabled();
    }
}
