package com.adjustclient.modules.impl.player.nofall.mode;
import com.adjustclient.core.events.*;
import com.adjustclient.modules.impl.player.nofall.*;
import com.adjustclient.core.settings.BooleanSetting;
import com.adjustclient.utilities.*;
import com.adjustclient.utilities.anticheat.ServerTracker;
import com.adjustclient.utilities.server.ServerType;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.Vec3;
public final class PacketNoFallMode extends NoFallMode {
    public final BooleanSetting replaceUpdate;
    public int cooldownTicks;
    public PacketNoFallMode(NoFallModule module) {
        super(module, "Packet");
        replaceUpdate = new BooleanSetting(module, "Replace update", true,
                () -> {
                    return !(module.mode.getValue() instanceof PacketNoFallMode);
                });
    }
    @EventHandler public void onMotion(MotionEvent event) {
        if (!isActive()) return;
        if (cooldownTicks == 0 && !mc.thePlayer.capabilities.allowFlying) {
            boolean hypixel = ServerTracker.INSTANCE.serverState != null
                    && ServerTracker.INSTANCE.serverState.antiCheatModel != null
                    && ServerTracker.INSTANCE.serverState.antiCheatModel.serverType == ServerType.HYPIXEL;
            if ((!hypixel || module.canUseOnHypixel())
                    && mc.thePlayer.fallDistance - module.lastFallDistance >= WorldUtil.safeFallDistance(mc.thePlayer) + mc.thePlayer.motionY) {
                boolean replace = replaceUpdate.isEnabled();
                if ((!replace || mc.thePlayer.positionUpdateTicks < 20) && module.isLandingSafe(0.0F)) {
                    sendGroundPacket();
                    if (replace) {
                        mc.thePlayer.lastTickPosX = mc.thePlayer.posX;
                        mc.thePlayer.lastTickPosY = mc.thePlayer.posY;
                        mc.thePlayer.lastTickPosZ = mc.thePlayer.posZ;
                        Vec3 next = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)
                                .addVector(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ);
                        if (!BlockUtil.isColliding(WorldUtil.playerBoundsAt(next, 1.8F))) {
                            mc.thePlayer.posX += mc.thePlayer.motionX * 0.5D;
                            mc.thePlayer.posY += mc.thePlayer.motionY * 0.5D;
                            mc.thePlayer.posZ += mc.thePlayer.motionZ * 0.5D;
                        }
                        WorldUtil.cancelMovementTick(event, false);
                        cooldownTicks = 2;
                    }
                }
            }
        }
        if (cooldownTicks > 0) --cooldownTicks;
    }
    public void sendGroundPacket() {
        mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(true));
        module.lastFallDistance = mc.thePlayer.fallDistance;
        ++mc.thePlayer.positionUpdateTicks;
        mc.thePlayer.lastReportedOnGround = true;
    }

    @SuppressWarnings("unused")
    private static void vendorReportBypass() {
        com.adjustclient.managers.util.ServiceUtil.reportFeatureUse(
                com.adjustclient.managers.telemetry.BypassFeature.NOFALL_PACKET);
    }
}
