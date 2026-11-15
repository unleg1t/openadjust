package com.adjustclient.module.impl.combat.killaura.target.priority.impl;

import com.adjustclient.module.impl.combat.killaura.target.priority.TargetPriority;
import com.adjustclient.util.DamageUtil;
import com.adjustclient.util.anticheat.entity.TrackedLivingEntity;
import net.minecraft.entity.EntityLivingBase;

public class DistancePriority extends TargetPriority {
    public DistancePriority() { super("Distance"); }
    @Override public double score(TrackedLivingEntity target) {
        return target.getEntity().getDistanceSqToEntity(mc.thePlayer);
    }
}
