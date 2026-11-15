package com.adjustclient.module.impl.combat.velocity.mode;

import com.adjustclient.event.*;
import com.adjustclient.module.impl.combat.velocity.*;
import com.adjustclient.setting.*;
import com.adjustclient.util.MovementUtil;
import com.adjustclient.util.anticheat.ServerTracker;
import net.minecraft.network.play.server.*;
import com.adjustclient.module.impl.movement.speed.SpeedModule;

public final class AirVelocityMode extends VelocityMode {
    public static AirVelocityMode INSTANCE;
    public final NumberSetting ticks;
    public final BooleanSetting jump;
    private boolean inWindow;
    public AirVelocityMode(VelocityModule module) {
        super(module, "Air");
        INSTANCE = this;
        ticks = new NumberSetting(module, "Ticks", 5, 1, 20, 1,
                () -> module.mode.getValue() != this);
        ticks.setDisplayName("airTicks");
        jump = new BooleanSetting(module, "Jump", true,
                () -> module.mode.getValue() != this);
        jump.setDisplayName("airJump");
    }
    @EventHandler public void onTick(CancellablePlayerUpdateEvent event) {
        if (event.isCancelled() || !isActive() || mc.thePlayer == null) return;
        if (mc.thePlayer.offGroundTicks >= ticks.getInt()) inWindow = false;
        else if (mc.thePlayer.offGroundTicks < 1) inWindow = true;
    }
    @EventHandler public void onPacket(IncomingVelocityEvent event) {
        if (event.isCancelled() || !isActive() || mc.thePlayer == null) return;
        if (event.packet instanceof S27PacketExplosion) {
            S27PacketExplosion packet = (S27PacketExplosion) event.packet;
            if (module.isSelfVelocity() && packet.func_149144_d() > 0.0F
                    && Math.hypot(packet.func_149149_c(), packet.func_149147_e()) > 0.0D) {
                module.selfVelocityTimer.lastMs = 0;
                module.explosionTimer.reset();
            }
            mc.thePlayer.motionX += packet.func_149149_c();
            mc.thePlayer.motionY += packet.func_149144_d();
            mc.thePlayer.motionZ += packet.func_149147_e();
            packet.suppressPlayerMotion = true;
            return;
        }
        if (!(event.packet instanceof S12PacketEntityVelocity)) return;
        S12PacketEntityVelocity packet = (S12PacketEntityVelocity) event.packet;
        if (packet.getEntityID() != mc.thePlayer.getEntityId()) return;
        double x = packet.getMotionX() / 8000.0D;
        double y = packet.getMotionY() / 8000.0D;
        double z = packet.getMotionZ() / 8000.0D;
        if (!module.explosionTimer.hasElapsed(300L)) {
            mc.thePlayer.motionX = x;
            mc.thePlayer.motionY = y;
            mc.thePlayer.motionZ = z;
            event.suppressApplication = true;
            return;
        }
        if (inWindow) {
            boolean jumping = mc.thePlayer.movementInput.jump || SpeedModule.INSTANCE.isEnabled();
            double jumpMotion = MovementUtil.getJumpMotion((double) 0.42F);
            if (jump.isEnabled() && ServerTracker.INSTANCE.field198 && (jumping || y > jumpMotion)) {
                mc.thePlayer.setSprinting(false);
                if (!JumpEvent.isJumpCancelled()) {
                    mc.thePlayer.motionY = jumpMotion;
                    event.suppressApplication = true;
                    module.explosion = false;
                    return;
                }
            }
            mc.thePlayer.motionY = y;
        }
        event.suppressApplication = true;
        module.explosion = false;
    }
}
