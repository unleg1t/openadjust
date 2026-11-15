package com.adjustclient.module.impl.movement.step;

import com.adjustclient.event.EventHandler;
import com.adjustclient.event.MoveHeadingEvent;
import com.adjustclient.util.BlockUtil;
import com.adjustclient.util.Timer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition;
import net.minecraft.util.AxisAlignedBB;
import com.adjustclient.service.util.ServiceUtil;

public class NcpStepMode extends StepMode {
    public static NcpStepMode INSTANCE;
    private final Timer stepTimer = new Timer();
    private int timerTicks;

    public NcpStepMode(StepModule module) {
        super(module, "NCP");
    }

    @com.adjustclient.event.EventHandler
    public void onUpdate(com.adjustclient.event.CancellablePlayerUpdateEvent event) {
        if (event.isCancelled() || !isActive()) return;
        tickTimer();
    }

    public void tickTimer() {
        if (timerTicks >= 1 && --timerTicks == 0) {
            mc.timer.timerSpeed = 1.0F;
        }
    }

    public boolean isStepping() {
        return timerTicks >= 1 || stepTimer.lastMs + 50L > System.currentTimeMillis();
    }

    public static boolean canStep(float height) {
        EntityPlayerSP player = mc.thePlayer;
        AxisAlignedBB ahead = player.getEntityBoundingBox().offset(player.motionX, height, player.motionZ);
        // World.getCollidingBoundingBoxes includes the Breaker collision-list event in this client.
        if (mc.theWorld.getCollidingBoundingBoxes(player, ahead).isEmpty()) {
            return false;
        }
        AxisAlignedBB above = player.getEntityBoundingBox().offset(player.motionX, height + 0.6D, player.motionZ);
        return mc.theWorld.getCollidingBoundingBoxes(player, above, true, false).isEmpty();
    }

    public static float measureDrop(double height) {
        // The scan accumulator is double, but its increment is the widened float 0.01F.
        for (double drop = 0.0D; drop < 2.0D; drop += (double) 0.01F) {
            EntityPlayerSP player = mc.thePlayer;
            AxisAlignedBB ahead = player.getEntityBoundingBox().offset(player.motionX, height - drop, player.motionZ);
            if (!mc.theWorld.getCollidingBoundingBoxes(player, ahead).isEmpty()) {
                return (float) drop;
            }
        }
        return 0.0F;
    }

    @EventHandler
    public void onStepHeight(com.adjustclient.event.StepHeightEvent event) {
        if (!isActive()) return;
        EntityPlayerSP player = mc.thePlayer;
        if (!player.onGround || player.motionY >= 0.0D || !canStepNow()) {
            return;
        }
        float height = getStepHeight();
        if (height <= 0.5F || !canStep(height)) {
            return;
        }
        int index = Math.round(height * 2.0F) - 2;
        if (index < 0 || index >= 3) {
            return;
        }
        event.stepHeight = height + 0.01F;
        double[] offsets = new double[][] {
                {0.42D, 0.753D},
                {0.42D, 0.74998D, 1.0008D, 1.16D, 1.23D, 1.2D},
                {0.42D, 0.78D, 0.63D, 0.51D, 0.9D, 1.21D, 1.45D, 1.43D}
        }[index];
        for (int i = 0; i < offsets.length; i++) {
            double offset = offsets[i];
            if (i != 0) {
                offset += 0.0003000000142492354D * ServiceUtil.movementAdjustment;
            }
            player = mc.thePlayer;
            player.sendQueue.addToSendQueue(new C04PacketPlayerPosition(
                    player.posX, player.posY + offset, player.posZ, false));
        }
        timerTicks = 3;
        float speed = new float[] {0.7F, 0.35F, 0.25F}[index];
        if (speed != 1.0F && mc.timer.timerSpeed == 1.0F) {
            mc.timer.unscaledElapsedPartialTicks = mc.timer.elapsedPartialTicks;
            mc.timer.unscaledElapsedTicks = mc.timer.elapsedTicks;
        }
        mc.timer.timerSpeed = speed;
        stepTimer.reset();
    }

    public static boolean isClear(double height) {
        EntityPlayerSP player = mc.thePlayer;
        AxisAlignedBB ahead = player.getEntityBoundingBox().offset(player.motionX, height, player.motionZ);
        return mc.theWorld.getCollidingBoundingBoxes(player, ahead).isEmpty();
    }

    public boolean canStepNow() {
        EntityPlayerSP player = mc.thePlayer;
        if (player == null || !player.onGround || module.disableBind.isDown()
                || !stepTimer.hasElapsed(100L)) {
            return false;
        }
        if (mc.thePlayer.isInWater() || mc.thePlayer.isInLava()) {
            return false;
        }
        return !BlockUtil.isOverLiquid(mc.thePlayer);
    }

    public float getStepHeight() {
        if (isClear(1.6D)) {
            return 1.5F - measureDrop(0.0D);
        }
        if (!isClear(1.1D)) {
            return 2.0F - measureDrop(2.0D);
        }
        if (!isClear(0.6D)) {
            return 1.0F - measureDrop(1.0D);
        }
        return 0.0F;
    }
}
