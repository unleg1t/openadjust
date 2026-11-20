package com.adjustclient.modules.impl.render;

import java.util.function.BooleanSupplier;
import com.adjustclient.core.settings.BooleanSetting;

public final class ItemAnimationsGlintCondition implements BooleanSupplier {
    private final ItemAnimationsModule module;
    public ItemAnimationsGlintCondition(ItemAnimationsModule module) { this.module = module; }
    @Override public boolean getAsBoolean() { return ((BooleanSetting) module.enchantmentGlintEnabled).isEnabled(); }
}
