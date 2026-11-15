package com.adjustclient.module.impl.combat.targetstrafe;
import com.adjustclient.util.anticheat.entity.TrackedLivingEntity;
public final class StrafeState {
    public final TrackedLivingEntity target;
    public StrafeDirection direction;
    public float movementYaw;
    public StrafeState(TrackedLivingEntity target) { this.target = target; direction = StrafeDirection.LEFT; }
}
