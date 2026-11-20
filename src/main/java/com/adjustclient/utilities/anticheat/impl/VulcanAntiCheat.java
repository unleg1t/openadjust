package com.adjustclient.utilities.anticheat.impl;

import com.adjustclient.utilities.anticheat.AntiCheat;
import com.adjustclient.utilities.anticheat.feature.Feature;
import java.util.Arrays;
import java.util.function.BooleanSupplier;

public class VulcanAntiCheat extends AntiCheat {
    public Object field16;
    public VulcanAntiCheat(Feature... allowed) {
        super("Vulcan");
        allowedFeatures.addAll(Arrays.asList(allowed));
    }
}
