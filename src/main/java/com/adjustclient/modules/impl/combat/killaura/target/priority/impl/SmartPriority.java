package com.adjustclient.modules.impl.combat.killaura.target.priority.impl;

import com.adjustclient.modules.impl.combat.killaura.target.priority.TargetPriority;
import com.adjustclient.utilities.DamageUtil;
import com.adjustclient.utilities.anticheat.entity.TrackedLivingEntity;
import net.minecraft.entity.EntityLivingBase;

public class SmartPriority extends TargetPriority {
    public SmartPriority() { super("Smart"); }
    @Override public boolean usesSecondaryPriority() { return false; }
    @Override public double score(TrackedLivingEntity target) {
        EntityLivingBase entity = target.getEntity();
        double distance = entity.getDistanceSqToEntity(mc.thePlayer);
        float damage = DamageUtil.computeDamage(entity, mc.thePlayer, mc.thePlayer.inventory.getCurrentItem(), false).damage;
        return target.getTotalHealth() + (float) (100.0D - damage) + distance / 1000.0D;
    }
}
