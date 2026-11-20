package com.adjustclient.modules.impl.combat.killaura.target;

import com.adjustclient.utilities.Timer;
import com.adjustclient.utilities.anticheat.entity.TrackedLivingEntity;
import com.adjustclient.utilities.rotation.AimPoint;

public class TargetInfo {
    public final TrackedLivingEntity entity;
    public AimPoint aimPoint;
    public Timer extraAttackTimer;
    public final Timer attackTimer = new Timer(0L);
    public TargetInfo(TrackedLivingEntity entity) { this.entity = entity; }
}
