package com.adjustclient.anticheat.impl;

import com.adjustclient.anticheat.AntiCheat;
import com.adjustclient.anticheat.feature.Feature;
import java.util.Arrays;
import java.util.function.BooleanSupplier;

public class WatchdogAntiCheat extends AntiCheat {
    public Object field16;
    public WatchdogAntiCheat(BooleanSupplier condition, Feature... allowed) {
        super("Watchdog", condition);
        allowedFeatures.addAll(Arrays.asList(allowed));
    }
}
