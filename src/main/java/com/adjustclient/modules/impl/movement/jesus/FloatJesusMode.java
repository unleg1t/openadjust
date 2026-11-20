package com.adjustclient.modules.impl.movement.jesus;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.MotionEvent;
import com.adjustclient.utilities.MovementUtil;

public final class FloatJesusMode extends JesusMode {
    public int waterTicks;
    public double surfaceY;
    public double speed;
    public int dryTicks;
    public int ascentTicks;

    public FloatJesusMode(JesusModule module) {
        super(module, "Float");
    }

    @Override
    public void onDisable() {
        waterTicks = 0;
        ascentTicks = 0;
    }

    @EventHandler
    public void onMotion(MotionEvent event) {
        if (!isActive() || !event.pre) return;
        if (!mc.thePlayer.isInWater()) {
            if (++dryTicks >= 36) {
                waterTicks = 0;
                ascentTicks = 0;
            }
        } else {
            double vertical = 0.03;
            if (waterTicks != 0) {
                double randomOffset = Math.random() * 0.0005;
                if (speed > 0.0993 - randomOffset) {
                    speed *= 0.949999988079071;
                    MovementUtil.setSpeed(speed);
                } else {
                    MovementUtil.setSpeed(true, 0.0993 - randomOffset,
                            MovementUtil.getDirection() * 0.0174532925199433);
                    if (Adjust.movementPredictor.historyTicks >= 21 && ++ascentTicks >= 41) {
                        ascentTicks = 0;
                        vertical = 0.4000000059604645;
                    }
                }
                waterTicks++;
                dryTicks = 0;
            } else {
                speed = 1.149999976158142 - Math.random() * 0.001000000047497451;
                MovementUtil.setSpeed(speed);
                waterTicks++;
                dryTicks = 0;
            }
            if (vertical != 0.03) {
                mc.thePlayer.motionY = vertical;
            } else if ((int) mc.thePlayer.posY + 0.6 > mc.thePlayer.posY) {
                mc.thePlayer.motionY = 0.03;
            }
            surfaceY = (int) mc.thePlayer.posY;
        }
        if (dryTicks < 2 && surfaceY != 0.0) mc.thePlayer.posY = (int) surfaceY + 1.0;
    }
}
