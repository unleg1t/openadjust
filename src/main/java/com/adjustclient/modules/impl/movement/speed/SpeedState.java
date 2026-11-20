package com.adjustclient.modules.impl.movement.speed;

import net.minecraft.entity.Entity;

public abstract class SpeedState {
    public final Entity entity;
    protected SpeedState(Entity entity) { this.entity = entity; }
    public abstract SpeedState copy(Entity entity);
}
