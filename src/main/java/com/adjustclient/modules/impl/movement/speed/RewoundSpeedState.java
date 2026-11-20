package com.adjustclient.modules.impl.movement.speed;

import com.adjustclient.utilities.Timer;
import net.minecraft.entity.Entity;

public final class RewoundSpeedState extends SpeedState {
    public Timer damageTimer = new Timer(0L);
    public boolean started;
    public double speed;

    public RewoundSpeedState(Entity entity) {
        super(entity);
    }

    @Override
    public RewoundSpeedState copy(Entity entity) {
        RewoundSpeedState copy = new RewoundSpeedState(entity);
        copy.damageTimer = new Timer(damageTimer.lastMs);
        copy.started = started;
        copy.speed = speed;
        return copy;
    }
}
