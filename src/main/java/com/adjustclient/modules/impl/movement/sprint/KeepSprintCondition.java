package com.adjustclient.modules.impl.movement.sprint;

import java.util.function.BooleanSupplier;

class KeepSprintCondition implements BooleanSupplier {
    private final SprintModule module;
    KeepSprintCondition(SprintModule module) { this.module = module; }

    @Override public boolean getAsBoolean() { return module.strafeEnabled.isEnabled(); }
}
