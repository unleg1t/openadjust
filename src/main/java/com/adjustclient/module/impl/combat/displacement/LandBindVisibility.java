package com.adjustclient.module.impl.combat.displacement;

import java.util.function.BooleanSupplier;

class LandBindVisibility implements BooleanSupplier {
    private final DisplacementSettings settings;
    public LandBindVisibility(DisplacementSettings settings) { this.settings = settings; }
    @Override public boolean getAsBoolean() { return settings.landEnabled.isEnabled(); }
}
