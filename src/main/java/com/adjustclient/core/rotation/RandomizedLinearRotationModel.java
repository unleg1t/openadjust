package com.adjustclient.core.rotation;

import com.adjustclient.utilities.RotationUtil;
import com.adjustclient.utilities.RandomUtil;
import com.adjustclient.utilities.Vec2f;

public final class RandomizedLinearRotationModel implements RotationModel {
    private Object field4;
    private final double threshold;
    private final double residual;

    public RandomizedLinearRotationModel(double threshold, double residual) {
        this.threshold = threshold;
        this.residual = residual;
    }

    @Override public Vec2f apply(Vec2f from, Vec2f to, float factor) {
        float distance = Math.abs(net.minecraft.util.MathHelper.wrapAngleTo180_float(from.x - to.x)) * factor;
        if (distance <= threshold) return InstantRotationModel.INSTANCE.apply(from, to, factor);
        return new LinearRotationModel(distance - residual + RandomUtil.nextFloat(0.01F, 0.05F))
                .apply(from, to, factor);
    }
}
