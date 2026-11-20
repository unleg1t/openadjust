package com.adjustclient.modules.impl.movement.speed;

import net.minecraft.entity.Entity;

public final class NcpSpeedState extends SpeedState {
    public Double pendingSpeed;
    public int ticks;
    public double speed;
    public int jumpTicks = 10;
    public boolean damaged;
    public boolean jumped;
    public boolean field38;
    public boolean field39;

    public NcpSpeedState(Entity entity) {
        super(entity);
    }

    @Override
    public NcpSpeedState copy(Entity entity) {
        NcpSpeedState copy = new NcpSpeedState(entity);
        copy.pendingSpeed = pendingSpeed;
        copy.ticks = ticks;
        copy.speed = speed;
        copy.jumpTicks = jumpTicks;
        copy.damaged = damaged;
        copy.jumped = jumped;
        copy.field38 = field38;
        copy.field39 = field39;
        return copy;
    }
}
