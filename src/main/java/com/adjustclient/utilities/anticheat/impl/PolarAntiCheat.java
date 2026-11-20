package com.adjustclient.utilities.anticheat.impl;

import com.adjustclient.utilities.anticheat.AntiCheat;
import com.adjustclient.utilities.anticheat.feature.Feature;
import java.util.Arrays;
import java.util.function.BooleanSupplier;

public class PolarAntiCheat extends AntiCheat {
    public Object field16;
    public PolarAntiCheat(BooleanSupplier condition) { super("Polar", condition); }
}
