package com.adjustclient.modules.impl.movement.scaffold;

import com.adjustclient.core.rotation.RotationModel;
import com.adjustclient.utilities.Vec2f;
import net.minecraft.util.MathHelper;

public final class ScaffoldWeightedRotationModel implements RotationModel {

    private final float factor;

    public ScaffoldWeightedRotationModel(float[] weights, int remainingTicks) {
        float sum = 0.0F;
        for (int i = weights.length - remainingTicks; i < weights.length; i++) {
            sum += weights[i];
        }
        this.factor = weights[weights.length - remainingTicks] / sum;
    }

    @Override
    public Vec2f apply(Vec2f from, Vec2f to, float f) {
        float yawDelta = MathHelper.wrapAngleTo180_float(to.x - from.x);
        float pitchDelta = to.y - from.y;
        return new Vec2f(
                from.x + yawDelta * this.factor * f,
                from.y + pitchDelta * this.factor * f
        );
    }
}
