package com.adjustclient.utilities.hypixel;

import com.adjustclient.core.events.Listener;
import com.adjustclient.utilities.MinecraftInstance;
import com.adjustclient.utilities.RandomUtil;
import com.adjustclient.utilities.Timer;

public final class HypixelPlacementLimiter implements Listener, MinecraftInstance {
    public int placements;
    public Timer timer;
    public static final double TIMING_MARGIN;

    static {
        TIMING_MARGIN = 0.001 - RandomUtil.nextDouble(0.00001, 0.00005);
    }
}
