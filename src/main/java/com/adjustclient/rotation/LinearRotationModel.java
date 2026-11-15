package com.adjustclient.rotation;

import com.adjustclient.util.Vec2f;
import net.minecraft.util.MathHelper;

public final class LinearRotationModel implements RotationModel {
    private Object field4;
    private final double speed;

    public LinearRotationModel(double speed) { this.speed = speed; }

    @Override public Vec2f apply(Vec2f from, Vec2f to, float factor) {
        float dx = MathHelper.wrapAngleTo180_float(to.x - from.x) * factor;
        float dy = (to.y - from.y) * factor;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length == 0.0F) return new Vec2f(from.x + dx, from.y + dy);
        double maxYaw = Math.abs(dx / length) * speed;
        double maxPitch = Math.abs(dy / length) * speed;
        return new Vec2f(from.x + (float) Math.max(-maxYaw, Math.min(maxYaw, dx)),
                from.y + (float) Math.max(-maxPitch, Math.min(maxPitch, dy)));
    }
}
