package com.adjustclient.module.impl.combat.criticals.mode;

import com.adjustclient.anticheat.feature.Features;
import com.adjustclient.event.AttackEntityEvent;
import com.adjustclient.event.CancellablePlayerUpdateEvent;
import com.adjustclient.event.EventHandler;
import com.adjustclient.event.MotionEvent;
import com.adjustclient.event.WorldResetEvent;
import com.adjustclient.module.impl.combat.criticals.CriticalsMode;
import com.adjustclient.module.impl.combat.criticals.CriticalsModule;
import com.adjustclient.module.impl.movement.flight.FlightModule;
import com.adjustclient.util.DamageUtil;

public final class RewoundCriticalsMode extends CriticalsMode {
    public static RewoundCriticalsMode INSTANCE;
    public boolean offsetApplied;
    public boolean jumped;
    public double serverFallDistance;
    public double lastServerY;

    public RewoundCriticalsMode(CriticalsModule module) {
        super(module, "Rewound");
        INSTANCE = this;
    }

    @EventHandler
    public void onAttack(AttackEntityEvent event) {
        if (!event.pre || !isEnabled() || !Features.CRITICALS_REWOUND.isAllowed()) return;
        if (!mc.thePlayer.isSprinting() && serverFallDistance > 0.0D
                && DamageUtil.canCritical(mc.thePlayer, event.entity)) {
            mc.thePlayer.onCriticalHit(event.entity);
        }
    }

    @EventHandler
    public void onUpdate(CancellablePlayerUpdateEvent event) {
        if (event.isCancelled() || !isEnabled()) return;
        jumped = false;
    }

    @EventHandler
    public void onWorldReset(WorldResetEvent event) {
        if (!isEnabled()) return;
        offsetApplied = false;
        jumped = false;
        serverFallDistance = 0.0D;
        lastServerY = 0.0D;
    }

    @EventHandler
    public void onMotion(MotionEvent event) {
        if (!isEnabled() || !event.pre || !Features.CRITICALS_REWOUND.isAllowed()) return;
        if (FlightModule.INSTANCE.isEnabled()) return;
        double serverY = mc.thePlayer.lastReportedPosY;
        if (mc.thePlayer.isSprinting() || mc.thePlayer.inWater || mc.thePlayer.isOnLadder() || mc.thePlayer.isInWeb) {
            serverFallDistance = 0.0D;
        } else if (lastServerY > serverY) {
            serverFallDistance += lastServerY - serverY;
        }
        lastServerY = serverY;
        if (jumped) {
            offsetApplied = false;
            return;
        }
        double predictedFall = serverY - event.y + serverFallDistance;
        boolean ground = predictedFall >= 3.0D;
        boolean applied = mc.thePlayer.onGround && !offsetApplied && (serverFallDistance <= 0.0D || predictedFall >= 3.0D);
        if (applied) event.y += 0.0301D;
        offsetApplied = applied;
        event.onGround = ground;
    }
}
