package com.adjustclient.modules.impl.movement.sprint;

import java.util.function.BooleanSupplier;

class OmniSprintCondition implements BooleanSupplier {
    private final SprintModule module;
    OmniSprintCondition(SprintModule module) { this.module = module; }

    @Override public boolean getAsBoolean() { return module.omnidirectionalEnabled.isEnabled(); }
}
