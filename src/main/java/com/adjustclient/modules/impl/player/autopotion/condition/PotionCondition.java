package com.adjustclient.modules.impl.player.autopotion.condition;

import net.minecraft.entity.player.EntityPlayer;

public interface PotionCondition {
    boolean shouldThrow(EntityPlayer player, int amplifier, double multiplier);
}
