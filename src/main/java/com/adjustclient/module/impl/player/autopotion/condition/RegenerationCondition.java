package com.adjustclient.module.impl.player.autopotion.condition;

import net.minecraft.entity.player.EntityPlayer;
import com.adjustclient.util.Timer;
import com.adjustclient.util.anticheat.ServerTracker;
import com.adjustclient.util.anticheat.entity.*;
import net.minecraft.util.*;

public final class RegenerationCondition extends EffectCondition {
    public boolean shouldThrow(EntityPlayer player, int amplifier, double multiplier) {
        double health = player.getHealth(), maxHealth = player.getMaxHealth();
        if (threatTimer != null && !threatTimer.hasElapsed(1000L) && maxHealth * 0.95 > health) return true;
        threatTimer = null;
        if (-((1.0 - health / maxHealth) * 3.0 + 1.0) < ServerTracker.INSTANCE.healthChange
                && maxHealth * 0.75 < health) return super.shouldThrow(player, amplifier, multiplier);
        threatTimer = new Timer();
        return true;
    }
}
