package com.adjustclient.module.impl.render;

import java.util.function.BooleanSupplier;
import com.adjustclient.setting.BooleanSetting;

public final class ItemAnimationsGlintCondition implements BooleanSupplier {
    private final ItemAnimationsModule module;
    public ItemAnimationsGlintCondition(ItemAnimationsModule module) { this.module = module; }
    @Override public boolean getAsBoolean() { return ((BooleanSetting) module.enchantmentGlintEnabled).isEnabled(); }
}
