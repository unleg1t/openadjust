package com.adjustclient.utilities.anticheat.impl;

import com.adjustclient.utilities.anticheat.AntiCheat;
import com.adjustclient.utilities.anticheat.feature.Feature;
import com.adjustclient.utilities.Timer;
import java.util.LinkedHashSet;

public final class FeatureBlockState {
    public final Timer creationTimer = new Timer();
    public final Timer lastReportTimer = new Timer();
    public final Feature feature;
    public final LinkedHashSet<AntiCheat> blockingAntiCheats = new LinkedHashSet<>();
    public Object field20;

    public FeatureBlockState(Feature feature) { this.feature = feature; }
}
