package com.adjustclient.module.impl.render;

import java.util.function.BooleanSupplier;

public final class ThirdPersonViewClipCondition implements BooleanSupplier {
    private final ThirdPersonModule module;
    public ThirdPersonViewClipCondition(ThirdPersonModule module) { this.module = module; }
    @Override public boolean getAsBoolean() { return module.viewClipEnabled.isEnabled(); }
}
