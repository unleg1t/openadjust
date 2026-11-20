package com.adjustclient.utilities.anticheat.entity;

import com.adjustclient.utilities.DamageUtil;
import com.adjustclient.utilities.anticheat.ServerTracker;
import net.minecraft.client.Minecraft;

public class CombatStats {
    public TrackedEntity<?> tracked;
    public double lastDamage;
    public long lastHurt;
    public long lastAttack;
    public boolean hurtWindowOnly;
    public Object field36;
    public CombatStats(TrackedEntity<?> tracked) { this.tracked = tracked; }

    public boolean getHitsToKill(int ticks) {
        var state = ServerTracker.INSTANCE.serverState;
        int latency = 0;
        if (state != null) {
            var samples = state.packetWatcher;
            latency = samples.delayCount == 0 ? 0
                    : (int) ((samples.totalDelay / samples.delayCount) * 1.5F);
        }
        long hurtDelay = 50L * (9 - ticks) - latency;
        if (hurtWindowOnly) return hurtDelay <= System.currentTimeMillis() - lastHurt;
        return 50L * (10 - ticks) <= System.currentTimeMillis() - lastAttack
                && hurtDelay <= System.currentTimeMillis() - lastHurt;
    }

    public boolean canKillWithHeldItem(int ticks) {
        if (getHitsToKill(ticks)) return true;
        var player = Minecraft.getMinecraft().thePlayer;
        if (player == null) return false;
        return DamageUtil.computeDamage(tracked.getEntity(), player, player.getHeldItem(), false)
                .damage > lastDamage;
    }

    public void updateAttack() {
        var player = Minecraft.getMinecraft().thePlayer;
        if (player == null) return;
        if (canKillWithHeldItem(0)) {
            lastDamage = DamageUtil.computeDamage(tracked.getEntity(), player, player.getHeldItem(), false)
                    .damage;
        }
        if (getHitsToKill(0)) lastAttack = System.currentTimeMillis();
    }
}
