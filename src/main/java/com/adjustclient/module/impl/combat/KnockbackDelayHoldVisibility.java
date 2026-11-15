package com.adjustclient.module.impl.combat;

import java.util.function.BooleanSupplier;

class KnockbackDelayHoldVisibility implements BooleanSupplier {
    private final KnockbackDelayModule module;
    KnockbackDelayHoldVisibility(KnockbackDelayModule module) { this.module = module; }
    @Override public boolean getAsBoolean() { return module.groundEnabled.isEnabled(); }
}
