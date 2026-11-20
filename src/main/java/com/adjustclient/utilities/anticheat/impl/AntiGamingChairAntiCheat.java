package com.adjustclient.utilities.anticheat.impl;

import com.adjustclient.utilities.anticheat.AntiCheat;
import com.adjustclient.utilities.anticheat.feature.Feature;
import java.util.Arrays;
import java.util.function.BooleanSupplier;

public class AntiGamingChairAntiCheat extends AntiCheat {
    public Object field16;
    public AntiGamingChairAntiCheat(Feature... allowed) {
        super("AntiGamingChair");
        allowedFeatures.addAll(Arrays.asList(allowed));
    }
}
