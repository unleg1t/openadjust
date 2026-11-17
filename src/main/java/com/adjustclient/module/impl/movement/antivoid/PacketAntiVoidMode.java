package com.adjustclient.module.impl.movement.antivoid;

import com.adjustclient.anticheat.feature.Features;
import com.adjustclient.event.EventHandler;
import com.adjustclient.event.MotionEvent;
import com.adjustclient.module.impl.movement.flight.FlightModule;
import com.adjustclient.module.impl.movement.highjump.HighJumpModule;
import com.adjustclient.module.impl.movement.longjump.LongJumpModule;
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition;
import net.minecraft.util.Vec3;

public final class PacketAntiVoidMode extends AntiVoidMode {
    public static PacketAntiVoidMode INSTANCE;
    public boolean saved;

    public PacketAntiVoidMode(AntiVoidModule module) {
        super(module, "Packet");
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        saved = false;
    }

    @EventHandler
    public void onMotion(MotionEvent event) {
        if (!isActive() || event.pre) return;
        if (mc.thePlayer.onGround) {
            saved = false;
        }
        if (!isFallingIntoVoid()) return;
        Vec3 position = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        if (!module.isOverVoid(position)) return;
        if (mc.thePlayer.fallDistance <= module.fallDistance.getDouble()) return;
        mc.thePlayer.sendQueue.getNetworkManager().sendPacketNoEvent(
                new C04PacketPlayerPosition(0.0D, 0.0D, 0.0D, false));
        saved = false;
    }

    private boolean isFallingIntoVoid() {
        if (mc.thePlayer.onGround) {
            if (Math.hypot(mc.thePlayer.motionX, mc.thePlayer.motionZ) == 0.0D || mc.thePlayer.isSneaking()) {
                return false;
            }
        }
        if (isSafeEnvironment(mc.thePlayer)) return false;
        if (!Features.ANTI_VOID_PACKET.isAllowed()) return false;
        if (FlightModule.INSTANCE.isEnabled() || LongJumpModule.INSTANCE.isEnabled() || HighJumpModule.INSTANCE.isEnabled()) return false;
        if (module.shouldIgnore()) return false;
        return mc.thePlayer.motionY <= 0.0D && mc.thePlayer.lastGroundY > mc.thePlayer.posY;
    }
}
