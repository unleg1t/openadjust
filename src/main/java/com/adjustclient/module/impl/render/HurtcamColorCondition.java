package com.adjustclient.module.impl.render;

import java.util.function.BooleanSupplier;
import com.adjustclient.setting.BooleanSetting;

public final class HurtcamColorCondition implements BooleanSupplier {
    private final HurtcamModule module;
    public HurtcamColorCondition(HurtcamModule module) { this.module = module; }
    @Override public boolean getAsBoolean() { return ((BooleanSetting) module.localDisabled).isEnabled(); }
}
