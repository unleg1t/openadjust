package com.adjustclient.modules.impl.movement;

import java.util.function.BooleanSupplier;

class SafeWalkEagleCondition implements BooleanSupplier {
    private final SafeWalkModule module;

    SafeWalkEagleCondition(SafeWalkModule module) { this.module = module; }

    @Override public boolean getAsBoolean() {
        return module.validateRotationYaw.isEnabled() || module.validateRotationPitch.isEnabled();
    }
}
