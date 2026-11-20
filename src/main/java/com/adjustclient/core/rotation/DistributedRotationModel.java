package com.adjustclient.core.rotation;

import com.adjustclient.utilities.RandomUtil;
import com.adjustclient.utilities.Timer;
import com.adjustclient.utilities.Vec2f;
import net.minecraft.util.MathHelper;

public final class DistributedRotationModel implements RotationModel {
    private final Timer timer = new Timer();
    private float corrections;
    private Object field12;
    public static final DistributedRotationModel INSTANCE;

    static { INSTANCE = new DistributedRotationModel(); }

    @Override public Vec2f apply(Vec2f from, Vec2f to, float factor) {
        float yaw = Math.abs(MathHelper.wrapAngleTo180_float(from.x - to.x)) * factor;
        float pitch = Math.abs(from.y - to.y) * factor;
        if (factor == 1.0F) corrections = !timer.hasElapsed(70L) ? corrections + 1.0F : 0.0F;
        if (yaw > 11.0F) timer.reset();
        float extra = corrections <= 5.0F ? corrections * 8.0F : 40.0F;
        float base = (1.0F - yaw / 360.0F) * 35.0F + 57.0F + extra;
        double yawSpeed = Math.max(yaw / 317.0F * base, RandomUtil.jitter(8.5D, 2.5D));
        double pitchSpeed = Math.max(Math.abs(pitch) / 75.0F * base, RandomUtil.jitter(3.5D, 0.5D));
        yawSpeed += RandomUtil.nextFloat(0.01F, 0.05F);
        pitchSpeed += RandomUtil.nextFloat(0.01F, 0.05F);
        Vec2f yawStep = new LinearRotationModel(yawSpeed).apply(from, to, factor);
        Vec2f pitchStep = new LinearRotationModel(pitchSpeed).apply(from, to, factor);
        return new Vec2f(yawStep.x, pitchStep.y);
    }
}
