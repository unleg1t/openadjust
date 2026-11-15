package com.adjustclient.module.impl.movement.scaffold;

import com.adjustclient.rotation.InstantRotationModel;
import com.adjustclient.rotation.RotationModel;
import com.adjustclient.util.Vec2f;
import net.minecraft.util.MathHelper;

public final class ScaffoldRotationModel implements RotationModel {

    private final RotationModel model;

    public ScaffoldRotationModel(RotationModel model) {
        this.model = model;
    }

    @Override
    public Vec2f apply(Vec2f from, Vec2f to, float f) {
        if (!ScaffoldModule.INSTANCE.isEnabled()) {
            if (this.model instanceof InstantRotationModel) {
                float yawDiff = MathHelper.wrapAngleTo180_float(to.x - from.x);
                return new Vec2f(yawDiff * f + from.x, (to.y - from.y) * f + from.y);
            }
            return this.model.apply(from, to, f);
        }
        return ScaffoldModule.applyModel(this.model, from, to, f);
    }
}
