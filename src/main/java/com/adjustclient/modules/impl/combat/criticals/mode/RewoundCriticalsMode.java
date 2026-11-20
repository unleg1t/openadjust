package com.adjustclient.modules.impl.combat.criticals.mode;

import com.adjustclient.utilities.anticheat.feature.Features;
import com.adjustclient.core.events.AttackEntityEvent;
import com.adjustclient.core.events.CancellablePlayerUpdateEvent;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.MotionEvent;
import com.adjustclient.core.events.WorldResetEvent;
import com.adjustclient.modules.impl.combat.criticals.CriticalsMode;
import com.adjustclient.modules.impl.combat.criticals.CriticalsModule;
import com.adjustclient.modules.impl.movement.flight.FlightModule;
import com.adjustclient.utilities.DamageUtil;

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
