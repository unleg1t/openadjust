package com.adjustclient.utilities.prediction;
import com.adjustclient.Adjust;
import net.minecraft.client.Minecraft;
public final class MotionPredictionFlags {
    public MotionPredictor predictor;
    public Object field8;
    public MotionPredictionFlags(MotionPredictor predictor) { this.predictor = predictor; }
    public void before(MotionSimulator previous, MotionSimulator current) {
        var player = Minecraft.getMinecraft().thePlayer;
        if (player == null) return;
        if (!current.onGround && previous.onGround && !previous.field13)
            predictor.jumpTicks = StepHistoryHelper.hasRecentStep(player, 10) ? 11 : 3;
    }
    public void after(MotionSimulator previous, MotionSimulator current) {
        var player = Minecraft.getMinecraft().thePlayer;
        if (player == null || current.speed == 0.0) return;
        if (!previous.onGround || current.onGround) {
            if (predictor.jumpTicks >= 1) {
                if (predictor.jumpTicks >= 3) StepHistoryHelper.hasRecentStep(player, 13);
                java.util.Objects.requireNonNull(Adjust.movementPredictor.limits);
            }
            java.util.Objects.requireNonNull(Adjust.movementPredictor);
        } else java.util.Objects.requireNonNull(Adjust.movementPredictor.limits);
    }
}
