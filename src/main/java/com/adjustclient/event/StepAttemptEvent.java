package com.adjustclient.event;

public class StepAttemptEvent extends CancellableEvent {
    public net.minecraft.util.Vec3 from;
    public net.minecraft.util.Vec3 to;

    public StepAttemptEvent(net.minecraft.util.Vec3 from, net.minecraft.util.Vec3 to) {
        this.from = from;
        this.to = to;
    }
}
