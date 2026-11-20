package com.adjustclient.modules.impl.movement.scaffold;

import com.adjustclient.core.rotation.RotationModel;
import com.adjustclient.utilities.RandomUtil;
import com.adjustclient.utilities.Vec2f;
import net.minecraft.util.MathHelper;

public final class ScaffoldLinearRotationModel implements RotationModel {

    private final double speed;

    public ScaffoldLinearRotationModel(double speed) {
        this.speed = speed + (double) RandomUtil.nextFloat(0.01F, 0.05F);
    }

    @Override
    public Vec2f apply(Vec2f from, Vec2f to, float f) {
        float yawDiff = MathHelper.wrapAngleTo180_float(to.x - from.x);
        float dx = f * yawDiff;
        float dy = (to.y - from.y) * f;
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length == 0.0F) {
            return new Vec2f(dx + from.x, dy + from.y);
        }
        double maxYaw = Math.abs(dx / length) * this.speed;
        // Math.clamp(value, min, max); the pitch is not limited
        double yaw = Math.clamp(dx, -maxYaw, maxYaw);
        return new Vec2f((float) yaw + from.x, dy + from.y);
    }
}
