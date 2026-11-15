package com.adjustclient.module.impl.player.autopotion.condition;

import net.minecraft.entity.player.EntityPlayer;
import com.adjustclient.util.Timer;
import com.adjustclient.util.anticheat.ServerTracker;
import com.adjustclient.util.anticheat.entity.*;
import net.minecraft.util.*;

public class EffectCondition implements PotionCondition {
    protected Timer threatTimer;
    public boolean shouldThrow(EntityPlayer player, int amplifier, double multiplier) {
        if (threatTimer != null && !threatTimer.hasElapsed(1000L)) return true;
        threatTimer = null;
        ServerTracker tracker = ServerTracker.INSTANCE;
        if (tracker.healthChange >= -0.5F) return false;
        double extra = Math.min(3.0, tracker.serverState == null ? 0.0
                : tracker.serverState.packetWatcher.getAverageDelay() / 100.0);
        Vec3 eye = player.getPositionVector().addVector(0, player.getEyeHeight(), 0);
        for (TrackedFakePlayer tracked : tracker.entityTracker.getEntities(1090, TrackedFakePlayer.class)) {
            AxisAlignedBB box = tracked.getEntity().getEntityBoundingBox().expand(
                    (double) 0.1F, (double) 0.1F, (double) 0.1F);
            Vec3 point = com.adjustclient.util.DistanceUtil.closestPoint(eye, box);
            double dx = eye.xCoord - point.xCoord;
            double dy = eye.yCoord - point.yCoord;
            double dz = eye.zCoord - point.zCoord;
            if (Math.sqrt(dx * dx + dy * dy + dz * dz) <= 6.0 + extra) {
                threatTimer = new Timer();
                return true;
            }
        }
        return false;
    }
}
