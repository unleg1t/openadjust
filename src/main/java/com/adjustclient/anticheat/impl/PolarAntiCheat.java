package com.adjustclient.anticheat.impl;

import com.adjustclient.anticheat.AntiCheat;
import com.adjustclient.anticheat.feature.Feature;
import java.util.Arrays;
import java.util.function.BooleanSupplier;

public class PolarAntiCheat extends AntiCheat {
    public Object field16;
    public PolarAntiCheat(BooleanSupplier condition) { super("Polar", condition); }
}
