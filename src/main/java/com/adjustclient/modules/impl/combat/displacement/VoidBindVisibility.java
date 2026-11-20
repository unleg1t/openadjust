package com.adjustclient.modules.impl.combat.displacement;

import java.util.function.BooleanSupplier;

class VoidBindVisibility implements BooleanSupplier {
    private final DisplacementSettings settings;
    public VoidBindVisibility(DisplacementSettings settings) { this.settings = settings; }
    @Override public boolean getAsBoolean() { return settings.voidEnabled.isEnabled(); }
}
