package com.adjustclient.hypixel;

import com.adjustclient.event.Listener;
import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.util.RandomUtil;
import com.adjustclient.util.Timer;

public final class HypixelPlacementLimiter implements Listener, MinecraftInstance {
    public int placements;
    public Timer timer;
    public static final double TIMING_MARGIN;

    static {
        TIMING_MARGIN = 0.001 - RandomUtil.nextDouble(0.00001, 0.00005);
    }
}
