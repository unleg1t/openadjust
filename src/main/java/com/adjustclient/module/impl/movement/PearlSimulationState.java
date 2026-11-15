package com.adjustclient.module.impl.movement;

import net.minecraft.util.Vec3;

public class PearlSimulationState {
    public Vec3 launch;
    public Vec3 impact;
    public float yaw;
    public float pitch;
    public int flightTicks;

    public PearlSimulationState(int flightTicks, Vec3 launch, Vec3 impact, double distance, float yaw, float pitch) {
        this.flightTicks = flightTicks;
        this.launch = launch;
        this.impact = impact;
        this.yaw = yaw;
        this.pitch = pitch;
    }
}
