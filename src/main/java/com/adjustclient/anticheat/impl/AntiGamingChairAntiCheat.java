package com.adjustclient.anticheat.impl;

import com.adjustclient.anticheat.AntiCheat;
import com.adjustclient.anticheat.feature.Feature;
import java.util.Arrays;
import java.util.function.BooleanSupplier;

public class AntiGamingChairAntiCheat extends AntiCheat {
    public Object field16;
    public AntiGamingChairAntiCheat(Feature... allowed) {
        super("AntiGamingChair");
        allowedFeatures.addAll(Arrays.asList(allowed));
    }
}
