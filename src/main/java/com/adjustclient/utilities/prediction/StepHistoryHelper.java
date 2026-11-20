package com.adjustclient.utilities.prediction;
import com.adjustclient.Adjust;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
public final class StepHistoryHelper {
    public static boolean hasRecentStep(EntityLivingBase entity, int ticks) {
        MotionPredictor predictor = Adjust.movementPredictor;
        ticks = Math.min(ticks, predictor.history.size());
        if (ticks > predictor.historyTicks || predictor.current == null) return false;
        if (entity.isPotionActive(Potion.jump)) return false;
        for (int i = 0; i < ticks; i++) {
            MotionSimulator old = predictor.history.get(i);
            if (predictor.current.field14) return false;
            if (predictor.current.onGround) {
                double dy = predictor.current.position.yCoord - old.position.yCoord;
                if (dy < 1.35 && dy >= 0.9 && old.onGround) return true;
            }
        }
        return false;
    }
}
