package com.adjustclient.module.impl.movement.flight;

import com.adjustclient.event.EventHandler;
import com.adjustclient.event.MoveEvent;
import com.adjustclient.setting.ExpandSetting;
import com.adjustclient.setting.NumberSetting;
import com.adjustclient.util.MovementUtil;
import com.adjustclient.util.RandomUtil;

public final class VanillaFlightMode extends FlightMode {
    public static VanillaFlightMode INSTANCE;
    public final NumberSetting horizontal;
    public final NumberSetting vertical;

    public VanillaFlightMode(FlightModule module) {
        super(module, "Vanilla");
        INSTANCE = this;
        ExpandSetting speed = new ExpandSetting(module, "Speed",
                () -> module.mode.getValue() != this);
        speed.setDisplayName("speedExpand" + name);
        horizontal = new NumberSetting(speed, "Horizontal", 1.0F, 0.1F, 10.0F, 0.1F);
        vertical = new NumberSetting(speed, "Vertical", 1.0F, 0.1F, 10.0F, 0.1F);
    }

    public static boolean canControlVertical() {
        return mc.thePlayer.mc.getRenderViewEntity() == mc.thePlayer
                && mc.cameraOverride == null
                && (!mc.thePlayer.mc.renderingFrame
                || mc.thePlayer.mc.cameraOverride == null);
    }

    @Override
    public void onDisable() {
        if (mc.thePlayer != null) {
            MovementUtil.setSpeed(Math.min(MovementUtil.getSpeed(),
                    MovementUtil.getSpeedWithPotion(0.164D, 0.2D)));
            mc.thePlayer.motionY = 0.0D;
        }
    }

    @EventHandler
    public void onMove(MoveEvent event) {
        if (!isActive()) return;
        float horizontalSpeed = RandomUtil.jitter(horizontal.getFloat(), 0.001F);
        if (mc.thePlayer != null) {
            MovementUtil.setSpeed(true, (double) horizontalSpeed, Math.toRadians(MovementUtil.getDirection()));
        }
        float verticalSpeed = RandomUtil.jitter(vertical.getFloat(), 0.002F);
        double motionY = Math.random() * (double) 0.0001F;
        motionY = mc.thePlayer.ticksExisted % 5 == 0 ? motionY * 10.0D : -motionY;
        if (canControlVertical()) {
            if (mc.gameSettings.keyBindJump.isKeyDown()) motionY += (double) verticalSpeed;
            if (mc.gameSettings.keyBindSneak.isKeyDown()) motionY -= (double) verticalSpeed;
        }
        mc.thePlayer.motionY = motionY;
        event.x = mc.thePlayer.motionX;
        event.y = motionY;
        event.z = mc.thePlayer.motionZ;
    }
}
