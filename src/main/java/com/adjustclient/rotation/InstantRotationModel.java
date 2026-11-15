package com.adjustclient.rotation;

import com.adjustclient.util.Vec2f;
import net.minecraft.util.MathHelper;

public final class InstantRotationModel implements RotationModel {
    private Object field4;
    public static final InstantRotationModel INSTANCE = new InstantRotationModel();

    @Override public Vec2f apply(Vec2f from, Vec2f to, float factor) {
        return new Vec2f(from.x + (MathHelper.wrapAngleTo180_float(to.x - from.x) * factor),
                from.y + ((to.y - from.y) * factor));
    }
}
