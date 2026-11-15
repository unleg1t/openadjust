package com.adjustclient.module.impl.player.autopotion.condition;

import net.minecraft.entity.player.EntityPlayer;

public final class HealingCondition implements PotionCondition {
    public boolean shouldThrow(EntityPlayer player, int amplifier, double multiplier) {
        int healing = (int) (multiplier * (4 << amplifier) + 0.5);
        if (healing < 5) {
            float health = player.getHealth();
            if (player.getMaxHealth() * 0.7F - healing < health) return false;
        }
        float health = player.getHealth();
        return (float) (player.getEntityAttribute(net.minecraft.entity.SharedMonsterAttributes.maxHealth)
                .getAttributeValue() - (float) healing) >= health;
    }
}
