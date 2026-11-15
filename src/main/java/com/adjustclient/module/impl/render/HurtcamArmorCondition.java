package com.adjustclient.module.impl.render;

import java.util.function.BooleanSupplier;
import com.adjustclient.setting.BooleanSetting;

public final class HurtcamArmorCondition implements BooleanSupplier {
    private final HurtcamModule module;
    public HurtcamArmorCondition(HurtcamModule module) { this.module = module; }
    @Override public boolean getAsBoolean() { return ((BooleanSetting) module.modelColorEnabled).isEnabled(); }
}
