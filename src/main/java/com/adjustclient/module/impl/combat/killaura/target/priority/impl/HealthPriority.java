package com.adjustclient.module.impl.combat.killaura.target.priority.impl;

import com.adjustclient.module.impl.combat.killaura.target.priority.TargetPriority;
import com.adjustclient.util.DamageUtil;
import com.adjustclient.util.anticheat.entity.TrackedLivingEntity;
import net.minecraft.entity.EntityLivingBase;

public class HealthPriority extends TargetPriority {
    public HealthPriority() { super("Health"); }
    @Override public double score(TrackedLivingEntity target) {
        return target.getTotalHealth() * 100.0D;
    }
}
