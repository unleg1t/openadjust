package com.adjustclient.modules.impl.render;

import java.util.function.BooleanSupplier;
import com.adjustclient.core.settings.BooleanSetting;

public final class HurtcamArmorCondition implements BooleanSupplier {
    private final HurtcamModule module;
    public HurtcamArmorCondition(HurtcamModule module) { this.module = module; }
    @Override public boolean getAsBoolean() { return ((BooleanSetting) module.modelColorEnabled).isEnabled(); }
}
