package com.adjustclient.module.impl.movement.speed;

import net.minecraft.entity.Entity;

public final class MinibloxSpeedState extends SpeedState {
    public MinibloxSpeedState(Entity entity) { super(entity); }
    @Override public MinibloxSpeedState copy(Entity entity) { return new MinibloxSpeedState(entity); }
}
