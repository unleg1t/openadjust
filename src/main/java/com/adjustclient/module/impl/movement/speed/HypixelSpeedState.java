package com.adjustclient.module.impl.movement.speed;

import net.minecraft.entity.Entity;

public final class HypixelSpeedState extends SpeedState {
    public HypixelSpeedState(Entity entity) { super(entity); }
    @Override public HypixelSpeedState copy(Entity entity) { return new HypixelSpeedState(entity); }
}
