package com.adjustclient.module.impl.movement.step;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

final class StepResult {
    final Vec3[] motions;
    final AxisAlignedBB[] boxes;
    int index;

    StepResult(Vec3[] motions, AxisAlignedBB[] boxes) {
        this.motions = motions;
        this.boxes = boxes;
    }
}
