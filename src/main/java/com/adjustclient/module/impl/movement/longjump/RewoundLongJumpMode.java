package com.adjustclient.module.impl.movement.longjump;

import com.adjustclient.event.EventHandler;
import com.adjustclient.event.MoveFlyingEvent;
import com.adjustclient.ui.hud.notification.NotificationBuilder;
import com.adjustclient.util.MovementUtil;
import net.minecraft.potion.Potion;

public final class RewoundLongJumpMode extends LongJumpMode {
    private double speed;
    private int stage;

    public RewoundLongJumpMode(LongJumpModule module) {
        super(module);
    }

    @EventHandler
    public void onGameLoop(MoveFlyingEvent event) {
        if (!isActive() || event.pre) return;
        double baseSpeed = MovementUtil.getSpeedWithPotion(0.2873, 0.2);
        double initialSpeed = (baseSpeed * 1.38 - 0.01) / 1.6;
        switch (stage) {
            case 0:
                if (!mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                    module.setEnabled(false, true);
                    new NotificationBuilder(module.getName())
                            .format("This feature requires you to have the {} potion effect.", "Speed")
                            .show();
                    return;
                }
                if (mc.thePlayer.onGround) {
                    stage = 1;
                }
                break;
            case 1:
                speed = initialSpeed;
                stage = 2;
                break;
            case 2:
                mc.thePlayer.motionY = MovementUtil.getJumpMotion((double) 0.42F);
                speed *= 2.149;
                stage = 3;
                break;
            case 3:
                if (mc.thePlayer.onGround) {
                    module.setEnabled(false, true);
                    return;
                }
                double lastDistance = mc.thePlayer.lastDistance;
                if (mc.thePlayer.offGroundTicks == 1) {
                    speed = lastDistance - (lastDistance - baseSpeed) * 0.66;
                    double boost = mc.thePlayer.isPotionActive(Potion.jump)
                            ? 2.2 - (mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1
                            : 2.2;
                    speed *= boost;
                } else {
                    double friction = mc.thePlayer.motionY < -0.1 ? 25.0 : 159.0;
                    speed = lastDistance - lastDistance / friction;
                }
                break;
        }
        if (stage >= 1) {
            speed = Math.max(speed, baseSpeed);
            MovementUtil.setSpeed(speed);
        }
    }

    @Override
    public void onEnable() {
        stage = 0;
    }

    @Override
    public String getName() {
        return "Rewound";
    }
}
