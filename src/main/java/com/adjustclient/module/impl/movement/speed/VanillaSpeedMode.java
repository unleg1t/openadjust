package com.adjustclient.module.impl.movement.speed;

import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.setting.NumberSetting;
import com.adjustclient.util.MovementUtil;
import com.adjustclient.util.RandomUtil;
import com.adjustclient.util.SimulationPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public final class VanillaSpeedMode extends SpeedMode<VanillaSpeedState> {
    public final NumberSetting speed;
    public final BooleanSetting low;

    public VanillaSpeedMode(SpeedModule module) {
        super(module, com.adjustclient.anticheat.feature.Features.SPEED_VANILLA);
        java.util.function.BooleanSupplier hidden = () -> module.mode.getValue() != this;
        speed = new NumberSetting(module, "Speed", 1.0F, 0.3F, 10.0F, 0.05F, hidden);
        low = new BooleanSetting(module, "Low", false, hidden);
        low.setDisplayName("vanillaLow");
    }

    @Override public void update(VanillaSpeedState state) {
        updateMovement(state);
    }

    public void initialize(VanillaSpeedState state) {
        state.speed = MovementUtil.getSpeedWithPotion(speed.getDouble(), 0.2);
    }

    public void onDisable(VanillaSpeedState state) {
        limitSpeed(state);
    }

    @Override public String getName() { return "Vanilla"; }

    public void updateMovement(VanillaSpeedState state) {
        EntityPlayer entity = (EntityPlayer) state.entity;
        if (!MovementUtil.isMoving()) {
            state.jumps = 0;
            strafe(entity, 0.0);
            return;
        }
        if (entity.onGround) {
            if (state.jumps < 5) ++state.jumps;
            state.speed = MovementUtil.getSpeedWithPotion(speed.getDouble(), 0.2);
            entity.jump();
            state.speed *= state.jumps * 0.035 + 1.0;
        } else if (entity.offGroundTicks == 1) {
            state.speed /= 1.350000023841858;
            if (low.isEnabled() && !mc.gameSettings.keyBindJump.isKeyDown()) entity.motionY = -0.0784;
            state.speed *= state.jumps * 0.035 + 1.0;
        } else {
            state.speed -= state.speed / 159.8999938964844;
            if ((entity.motionY == 0.0 || entity.isCollidedVertically) && entity.offGroundTicks == 5)
                entity.motionY -= 0.15;
        }
        strafe(entity, entity instanceof SimulationPlayer ? state.speed : RandomUtil.jitter(state.speed, 0.001));
    }

    public void limitSpeed(VanillaSpeedState state) {
        double current = Math.hypot(state.entity.motionX, state.entity.motionZ);
        double cap = MovementUtil.getSpeedWithPotion(0.154, 0.2);
        MovementUtil.setSpeed(Math.min(current, cap));
    }

    @Override public VanillaSpeedState createState(Entity entity) { return new VanillaSpeedState(entity); }
}
