package com.adjustclient.module.impl.movement.antivoid;

import net.minecraft.util.Vec3;

public class FallSimulationResult {
    public final Vec3 position;
    public final Vec3 motion;

    public FallSimulationResult(Vec3 position, Vec3 motion) {
        this.position = position;
        this.motion = motion;
    }
}
