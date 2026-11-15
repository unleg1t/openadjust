package com.adjustclient.module.impl.combat.killaura.target;

import com.adjustclient.util.Timer;
import com.adjustclient.util.anticheat.entity.TrackedLivingEntity;
import com.adjustclient.util.rotation.AimPoint;

public class TargetInfo {
    public final TrackedLivingEntity entity;
    public AimPoint aimPoint;
    public Timer extraAttackTimer;
    public final Timer attackTimer = new Timer(0L);
    public TargetInfo(TrackedLivingEntity entity) { this.entity = entity; }
}
