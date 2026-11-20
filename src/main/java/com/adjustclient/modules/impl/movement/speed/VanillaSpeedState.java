package com.adjustclient.modules.impl.movement.speed;

import net.minecraft.entity.Entity;

public final class VanillaSpeedState extends SpeedState {
    public double speed;
    public int jumps;
    public VanillaSpeedState(Entity entity) { super(entity); }

    @Override public VanillaSpeedState copy(Entity entity) {
        VanillaSpeedState copy = new VanillaSpeedState(entity);
        copy.speed = speed;
        copy.jumps = jumps;
        return copy;
    }
}
