package com.adjustclient.modules.impl.player.autopotion.condition;

import net.minecraft.entity.player.EntityPlayer;

public final class FullEffectCondition implements PotionCondition {
    public boolean shouldThrow(EntityPlayer player, int amplifier, double multiplier) { return multiplier >= 1.0; }
}
