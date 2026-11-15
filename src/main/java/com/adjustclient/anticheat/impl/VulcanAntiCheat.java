package com.adjustclient.anticheat.impl;

import com.adjustclient.anticheat.AntiCheat;
import com.adjustclient.anticheat.feature.Feature;
import java.util.Arrays;
import java.util.function.BooleanSupplier;

public class VulcanAntiCheat extends AntiCheat {
    public Object field16;
    public VulcanAntiCheat(Feature... allowed) {
        super("Vulcan");
        allowedFeatures.addAll(Arrays.asList(allowed));
    }
}
