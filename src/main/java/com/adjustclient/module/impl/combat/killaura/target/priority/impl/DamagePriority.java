package com.adjustclient.module.impl.combat.killaura.target.priority.impl;

import com.adjustclient.module.impl.combat.killaura.target.priority.TargetPriority;
import com.adjustclient.util.DamageUtil;
import com.adjustclient.util.anticheat.entity.TrackedLivingEntity;
import net.minecraft.entity.EntityLivingBase;

public class DamagePriority extends TargetPriority {
    public DamagePriority() { super("Damage"); }
    @Override public double score(TrackedLivingEntity target) {
        float damage = DamageUtil.computeDamage(target.getEntity(), mc.thePlayer, mc.thePlayer.inventory.getCurrentItem(), false).damage;
        return target.getTotalHealth() + (float) (10000.0D - damage) * 1000.0D;
    }
}
