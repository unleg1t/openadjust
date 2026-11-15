package com.adjustclient.module.impl.movement.scaffold;

import com.adjustclient.util.Timer;
import net.minecraft.util.BlockPos;

final class PredictionState {

    /** The predicted last block (the reused MutableBlockPos returned by {@link BlockCountHud#predictDestination}). */
    BlockPos destination;
    /** Time of the last update. */
    Timer timer;
    /** Number of yaw samples. */
    int samples;
    /** Sum of the yaw samples. */
    double yawSum;
    /** {@code yawSum / samples}. */
    double yawAverage;
    /** Second sample counter (incremented together with {@link #samples}; a prediction is made from the 2nd sample on). */
    int samples2;

    PredictionState() {
        this.timer = new Timer();
    }
}
