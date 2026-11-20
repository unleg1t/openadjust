package com.adjustclient.utilities.anticheat.entity;

import com.adjustclient.Adjust;
import com.adjustclient.modules.impl.render.streamer.StreamerModule;
import com.adjustclient.utilities.Timer;
import com.adjustclient.utilities.anticheat.ServerTracker;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S19PacketEntityStatus;

public class TrackedLivingEntity extends TrackedEntity<EntityLivingBase> {
    public CombatStats combatStats;
    public int hurtTicks;
    public int pendingHurtTicks;
    public TrackedLivingEntity(EntityLivingBase entity) { super(entity); combatStats = new CombatStats(this); }

    public float getTotalHealth() { return entity.getHealth() + entity.getAbsorptionAmount(); }
    public boolean isDead() { return stale || entity.deathTime > 0 || entity.getHealth() <= 0.0F || removed; }
    public boolean matchesType(int mask) {
        if (entity.isUser() && (mask & 0x80) != 0) return true;
        if (entity.isInvisible() && (mask & 0x40) == 0) return false;
        return (mask & ((entity instanceof EntityMob || entity instanceof EntityIronGolem) ? 4 : 8)) != 0;
    }
    protected void updateState() {
        if (pendingHurtTicks != 0) { hurtTicks = pendingHurtTicks; pendingHurtTicks = 0; }
        if (hurtTicks >= 1) --hurtTicks;
    }
    public String getDisplayName() { return entity.getName(); }
    public String getName() {
        return entity instanceof EntityPlayer ? StreamerModule.replaceNames(entity.getName()) : getDisplayName();
    }
    public boolean isTagged() { return false; }
    public boolean isRecentlyTracked() {
        var tracker = Adjust.entityTrackerService.tracker;
        Timer timer;
        synchronized (tracker.recentlyDestroyed) {
            timer = tracker.recentlyDestroyed.get(entityId);
        }
        return timer != null && !timer.hasElapsed(1500L);
    }
    public void onStatus(S19PacketEntityStatus packet) {
        if (packet.getOpCode() != 2) return;
        int latencyTicks = 0;
        if (ServerTracker.INSTANCE != null) {
            var state = ServerTracker.INSTANCE.serverState;
            var samples = state == null ? null : state.packetWatcher;
            if (samples != null) {
                int latency = samples.delayCount == 0 ? 0
                        : samples.totalDelay / samples.delayCount;
                latencyTicks = Math.min(8, latency / 50);
            }
        }
        pendingHurtTicks = 20 - latencyTicks;
        long timestamp = packet.timestamp;
        combatStats.lastHurt = timestamp == 0L ? System.currentTimeMillis() : timestamp;
    }
}
