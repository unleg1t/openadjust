package com.adjustclient.module.impl.movement.longjump;

import com.adjustclient.event.EventHandler;
import com.adjustclient.event.MoveFlyingEvent;
import com.adjustclient.util.MovementUtil;

public final class VanillaLongJumpMode extends LongJumpMode {
    private boolean jumped;
    public VanillaLongJumpMode(LongJumpModule module) { super(module); }

    @EventHandler public void onGameLoop(MoveFlyingEvent event) {
        if (!isActive() || event.pre) return;
        if (jumped) {
            if (mc.thePlayer.onGround) module.setEnabled(false, true);
            else {
                MovementUtil.setSpeed(true, Math.hypot(mc.thePlayer.motionX, mc.thePlayer.motionZ),
                        MovementUtil.getDirection() * 0.0174532925199433);
                if (mc.thePlayer.motionY < 0.0) mc.thePlayer.motionY /= 2.0;
            }
        } else if (mc.thePlayer.onGround) {
            mc.thePlayer.jump();
            MovementUtil.setSpeed(0.0);
            jumped = true;
        }
    }

    @Override public void onEnable() { jumped = false; }

    @Override public String getName() { return "Vanilla"; }
}
