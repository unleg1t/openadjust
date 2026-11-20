package com.adjustclient.utilities.anticheat;

import com.adjustclient.utilities.anticheat.feature.Feature;
import java.util.HashSet;
import java.util.function.BooleanSupplier;

public abstract class AntiCheat {
    public final String name;
    protected final HashSet<Feature> allowedFeatures = new HashSet<>();
    protected final BooleanSupplier condition;

    protected AntiCheat(String name) {
        this(name, () -> false);
    }

    protected AntiCheat(String name, BooleanSupplier condition) {
        this.name = name;
        this.condition = condition;
    }

    public boolean isAllowed(Feature feature) {
        return condition.getAsBoolean() || feature.unrestricted || allowedFeatures.contains(feature);
    }
}
