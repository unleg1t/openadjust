package com.adjustclient.module.impl.movement.sprint;

import java.util.function.BooleanSupplier;

class StrafeCondition implements BooleanSupplier {
    private final SprintModule module;

    StrafeCondition(SprintModule module) {
        this.module = module;
    }

    @Override
    public boolean getAsBoolean() {
        return module.ignoreHunger.isEnabled()
                || module.ignoreBlindness.isEnabled()
                || module.ignoreUsingItem.isEnabled();
    }
}
