package com.adjustclient.module.impl.movement.scaffold;

import com.adjustclient.event.MotionEvent;
import com.adjustclient.event.MoveFlyingEvent;
import com.adjustclient.event.MovementInputEvent;
import com.adjustclient.event.DirectionalPacketEvent;
import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.util.BlockPos;
import com.adjustclient.Adjust;
import com.adjustclient.module.impl.movement.MovementFixModule;
import com.adjustclient.service.util.ServiceUtil;
import com.adjustclient.server.ServerType;
import com.adjustclient.util.anticheat.ServerTracker;

public final class HypixelScaffoldMode extends ScaffoldMode {

    @Override
    public boolean isAboveBuildLimit(int y) {
        Integer limit = (Integer) Adjust.gameStateTracker.state.getAdditionalData();
        if (limit == null) {
            return false;
        }
        return limit.intValue() <= y;
    }

    /** At the start of each controller update (pre) without an active tower, release the module's input override. */
    @com.adjustclient.event.EventHandler
    public void onControllerUpdate(com.adjustclient.event.GameLoopEvent e) {
        if (!this.isActive()) {
            return;
        }
        if (e.preScheduled) {                                         // *(byte *)(e + 4)
            if (this.module.towering) {
                return;
            }
            this.module.releaseInput();
        }
    }

    /** The tower flag of the module is cleared again as soon as the player is on the ground (or in the air for a long time). */
    @com.adjustclient.event.EventHandler(priority = 1)
    public void onMotionPre(MotionEvent e) {
        if (!this.isActive() || !e.pre) {
            return;
        }
        EntityPlayerSP player = mc.thePlayer;
        if (!player.onGround && player.offGroundTicks < 11) {
            return;
        }
        this.module.towering = false;
    }

    /**
     * Hypixel tower: on the sixth tick off the ground (and when not blocked by the server state), if the block two
     * below the player is not air, the vertical motion is reduced by {@code 0.25 - latency * 0.001} and the module is
     * marked as towering.
     */
    @com.adjustclient.event.EventHandler
    public void onMotion(MoveFlyingEvent e) {
        if (!this.isActive() || e.pre) {
            return;
        }
        if (!this.module.settings.isTower()) {
            return;
        }
        EntityPlayerSP player = mc.thePlayer;
        if (player.offGroundTicks != 6) {
            return;
        }
        if (ServerTracker.INSTANCE.ticksSinceTabUpdate != 0) {
            return;
        }
        BlockPos below = new BlockPos(player.posX, player.posY - 2.0D, player.posZ);
        Block block = mc.theWorld.getBlockState(below).getBlock();
        if (com.adjustclient.util.BlockUtil.canReplace(block, mc.theWorld, below)) {
            return;
        }
        double reduction = 0.25D - ServiceUtil.movementAdjustment * (double) 0.001F;
        if (ServiceUtil.isActiveModel(ServerType.HYPIXEL)) {     // "is on server" check
            player.motionY = player.motionY - reduction;
        }
        this.module.towering = true;
    }

    @SuppressWarnings("unused")
    private static void vendorReportBypass() {
        ServiceUtil.reportFeatureUse(com.adjustclient.service.telemetry.BypassFeature.SCAFFOLD_HYPIXEL_TOWER);
    }

    @Override
    public void onPlace(PlacementTarget placement) {
        int remaining = Adjust.placementLimiter.placements;
        if (remaining >= 1) {
            Adjust.placementLimiter.placements = remaining - 1;
        }
    }

    @Override
    public boolean canSprint() {
        return this.module.settings.isAllowSprint() && MovementFixModule.INSTANCE.isEnabled();
    }

    @Override
    public boolean e() {
        return false;
    }

    /**
     * Keeps the module's input override in sync with the server: teleports and join packets release it, and (while
     * towering on Hypixel) an explosion with motion or a velocity packet for the local player re-arms it.
     */
    @com.adjustclient.event.EventHandler
    public void onPacket(DirectionalPacketEvent e) {
        if (e.isCancelled() || !this.isActive() || e.outgoing) {
            return;
        }
        Packet<?> packet = e.packet;
        if (this.module.towering) {
            // only when playing on Hypixel and the game state check passes
            if (ServiceUtil.isActiveModel(ServerType.HYPIXEL)
                    && ServerTracker.INSTANCE.serverState.antiCheatModel.isAllChecksPassed()) {
                if (packet instanceof S27PacketExplosion) {
                    S27PacketExplosion explosion = (S27PacketExplosion) packet;
                    if (explosion.func_149149_c() == 0.0F && explosion.func_149144_d() == 0.0F
                            && explosion.func_149147_e() == 0.0F) {
                        return;
                    }
                    this.module.holdInput();
                } else if (packet instanceof S12PacketEntityVelocity) {
                    if (((S12PacketEntityVelocity) packet).getEntityID() == mc.thePlayer.getEntityId()) {
                        this.module.holdInput();
                    }
                }
                return;
            }
        }
        if (packet instanceof S08PacketPlayerPosLook || packet instanceof S01PacketJoinGame) {
            this.module.releaseInput();
        }
    }

    @com.adjustclient.event.EventHandler
    public void onMoveInput(MovementInputEvent e) {
        if (!this.isActive()) {
            return;
        }
        if (this.module.towering) {
            e.back = false;
            e.forward = false;
            e.left = false;
            e.right = false;
        }
    }
}
