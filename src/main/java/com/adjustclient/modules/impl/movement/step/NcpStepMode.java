package com.adjustclient.modules.impl.movement.step;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.CancellablePlayerUpdateEvent;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.StepHeightEvent;
import com.adjustclient.utilities.server.ServerType;
import com.adjustclient.managers.util.ServiceUtil;
import com.adjustclient.core.settings.NumberSetting;
import com.adjustclient.utilities.BlockUtil;
import com.adjustclient.utilities.Timer;
import java.util.List;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition;
import net.minecraft.util.AxisAlignedBB;

public class NcpStepMode extends StepMode {
    public static NcpStepMode INSTANCE;
    private static final float[] STEP_HEIGHTS = {1.0F, 1.5F, 2.0F};
    private static final float[] STEP_TIMERS = {0.7F, 0.35F, 0.25F};
    private static final double[][] STEP_OFFSETS = {
            {0.42D, 0.753D},
            {0.42D, 0.74998D, 1.0008D, 1.16D, 1.23D, 1.2D},
            {0.42D, 0.78D, 0.63D, 0.51D, 0.9D, 1.21D, 1.45D, 1.43D}
    };

    public final NumberSetting height;
    private final Timer stepTimer = new Timer();
    public float timerSpeed = 1.0F;
    private int timerTicks;

    public NcpStepMode(StepModule module) {
        super(module, "NCP");
        INSTANCE = this;
        height = new NumberSetting(module, "Height", 2.5D, 1.0D, 2.5D, 0.5D,
                () -> module.mode.getValue() != this);
        height.setDisplayName("ncpHeight");
    }

    @EventHandler
    public void onUpdate(CancellablePlayerUpdateEvent event) {
        if (event.isCancelled() || !isActive()) return;
        tickTimer();
    }

    public void tickTimer() {
        if (timerTicks >= 1 && --timerTicks >= 1) {
            if (timerSpeed > 1.0F && ServiceUtil.isActiveModel(ServerType.HYPIXEL)
                    && Adjust.outboundPacketQueue.packets.size() >= 800) {
                return;
            }
            mc.timer.setTimerSpeed(timerSpeed);
        }
    }

    public boolean isStepping() {
        return timerTicks >= 1 || stepTimer.lastMs + 50L > System.currentTimeMillis();
    }

    @EventHandler
    public void onStepHeight(StepHeightEvent event) {
        if (!isActive()) return;
        EntityPlayerSP player = mc.thePlayer;
        if (!player.onGround || player.motionY >= 0.0D || !canStepNow()) {
            return;
        }
        float maxStep = Math.min(height.getFloat(), 2.0F);
        double halfWidth = player.width / 2.0D;
        AxisAlignedBB currentBox = player.getEntityBoundingBox();
        AxisAlignedBB initialBox = new AxisAlignedBB(
                player.posX - halfWidth, currentBox.minY, player.posZ - halfWidth,
                player.posX + halfWidth, currentBox.maxY, player.posZ + halfWidth);
        double clippedX = currentBox.minX - initialBox.minX;
        double clippedZ = currentBox.minZ - initialBox.minZ;
        double actualHeight = measureStepOffset(initialBox, player.motionX, player.motionZ, clippedX, clippedZ, maxStep);
        if (event.stepHeight >= actualHeight) {
            return;
        }
        int index = findStepIndex((float) actualHeight);
        if (index < 0) {
            return;
        }
        event.stepHeight = maxStep;
        double[] offsets = STEP_OFFSETS[index];
        for (int i = 0; i < offsets.length; i++) {
            double offset = offsets[i];
            if (i != 0) {
                offset += 0.0003000000142492354D * ServiceUtil.movementAdjustment;
            }
            player = mc.thePlayer;
            player.sendQueue.getNetworkManager().sendPacketNoEvent(new C04PacketPlayerPosition(
                    player.posX, player.posY + (float) offset, player.posZ, false));
        }
        timerTicks = ServiceUtil.isActiveModel(ServerType.HYPIXEL) ? 1 : 3;
        timerSpeed = STEP_TIMERS[index];
        mc.timer.setTimerSpeed(timerSpeed);
        stepTimer.reset();
    }

    public static double measureStepOffset(AxisAlignedBB initialBox, double origX, double origZ,
                                           double clippedX, double clippedZ, float maxStep) {
        EntityPlayerSP player = mc.thePlayer;
        List<AxisAlignedBB> boxes = mc.theWorld.getCollidingBoundingBoxes(
                player, initialBox.addCoord(origX, maxStep, origZ), true, true);
        AxisAlignedBB box1 = initialBox;
        AxisAlignedBB swept1 = box1.addCoord(origX, 0.0D, origZ);
        double y1 = maxStep;
        for (AxisAlignedBB box : boxes) {
            y1 = box.calculateYOffset(swept1, y1);
        }
        box1 = box1.offset(0.0D, y1, 0.0D);
        double x1 = origX;
        for (AxisAlignedBB box : boxes) {
            x1 = box.calculateXOffset(box1, x1);
        }
        box1 = box1.offset(x1, 0.0D, 0.0D);
        double z1 = origZ;
        for (AxisAlignedBB box : boxes) {
            z1 = box.calculateZOffset(box1, z1);
        }
        box1 = box1.offset(0.0D, 0.0D, z1);

        AxisAlignedBB box2 = initialBox;
        double y2 = maxStep;
        for (AxisAlignedBB box : boxes) {
            y2 = box.calculateYOffset(box2, y2);
        }
        box2 = box2.offset(0.0D, y2, 0.0D);
        double x2 = origX;
        for (AxisAlignedBB box : boxes) {
            x2 = box.calculateXOffset(box2, x2);
        }
        box2 = box2.offset(x2, 0.0D, 0.0D);
        double z2 = origZ;
        for (AxisAlignedBB box : boxes) {
            z2 = box.calculateZOffset(box2, z2);
        }
        box2 = box2.offset(0.0D, 0.0D, z2);

        double dist1 = x1 * x1 + z1 * z1;
        double dist2 = x2 * x2 + z2 * z2;
        double bestX;
        double bestY;
        double bestZ;
        AxisAlignedBB bestBox;
        if (dist1 >= dist2) {
            bestX = x1;
            bestZ = z1;
            bestY = -y1;
            bestBox = box1;
        } else {
            bestX = x2;
            bestZ = z2;
            bestY = -y2;
            bestBox = box2;
        }
        for (AxisAlignedBB box : boxes) {
            bestY = box.calculateYOffset(bestBox, bestY);
        }
        if (clippedX * clippedX + clippedZ * clippedZ >= bestX * bestX + bestZ * bestZ) {
            return 0.0D;
        }
        AxisAlignedBB landed = bestBox.offset(0.0D, bestY, 0.0D);
        return landed.minY - initialBox.minY;
    }

    private static int findStepIndex(float actualHeight) {
        for (int i = 0; i < STEP_HEIGHTS.length; i++) {
            if (STEP_HEIGHTS[i] + 0.0001F >= actualHeight) {
                return i;
            }
        }
        return -1;
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
}
