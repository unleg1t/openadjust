package com.adjustclient.modules.impl.combat.killaura.target.priority.impl;

import com.adjustclient.modules.impl.combat.killaura.target.priority.TargetPriority;
import com.adjustclient.utilities.DamageUtil;
import com.adjustclient.utilities.anticheat.entity.TrackedLivingEntity;
import net.minecraft.entity.EntityLivingBase;

public class HealthPriority extends TargetPriority {
    public HealthPriority() { super("Health"); }
    @Override public double score(TrackedLivingEntity target) {
        return target.getTotalHealth() * 100.0D;
    }
}
