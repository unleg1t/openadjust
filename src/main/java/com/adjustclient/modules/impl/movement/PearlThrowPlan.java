package com.adjustclient.modules.impl.movement;

import com.adjustclient.utilities.Timer;
import net.minecraft.util.Vec3;

public class PearlThrowPlan {
    public Vec3 launch;
    public Vec3 impact;
    public Timer timer;
    public double distance;
    public int flightMillis;
    public int flightTicks;
    public int targetId;

    public PearlThrowPlan(Vec3 launch, Vec3 impact, int flightTicks, int targetId, double distance) {
        this.launch = launch;
        this.impact = impact;
        this.timer = new Timer();
        this.distance = distance;
        this.flightTicks = flightTicks;
        this.targetId = targetId;
    }
}
