package com.adjustclient.utilities.prediction;
import java.util.LinkedList;
import com.adjustclient.utilities.anticheat.impl.PredictionAntiCheat;
import com.adjustclient.utilities.anticheat.ServerTracker;
public final class MotionPredictor {
    public LinkedList<MotionSimulator> history = new LinkedList<>();
    public MotionSimulator current;
    public MotionValidator validator = new MotionValidator(this);
    public MotionPredictionFlags flags = new MotionPredictionFlags(this);
    public PredictionLimits limits = new PredictionLimits();
    public int historyTicks;
    public int jumpTicks;
    public Object field32;
    public static PredictionAntiCheat getPredictionAntiCheat() {
        var state = ServerTracker.INSTANCE.serverState;
        return state == null || state.featureBlocker == null ? null
                : state.featureBlocker.getAntiCheat(PredictionAntiCheat.class);
    }
}
