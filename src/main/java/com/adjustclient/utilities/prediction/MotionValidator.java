package com.adjustclient.utilities.prediction;
import com.adjustclient.Adjust;
import com.adjustclient.utilities.MinecraftInstance;
import com.adjustclient.utilities.MovementUtil;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.*;
public final class MotionValidator implements MinecraftInstance {
    public MotionPredictor predictor;
    public Vec3 field8;
    public Object field12;
    public MotionValidator(MotionPredictor predictor) { this.predictor = predictor; }
    public void validateMotion() {
        if (Adjust.movementPredictor.history.isEmpty()) return;
        MotionSimulator last = Adjust.movementPredictor.history.getFirst();
        if (last == null) return;
        try {
            Vec3 previous = last.position;
            Vec3 predicted = new Vec3(mc.thePlayer.posX + mc.thePlayer.motionX,
                    mc.thePlayer.posY + mc.thePlayer.motionY, mc.thePlayer.posZ + mc.thePlayer.motionZ);
            double dx = Math.abs(previous.xCoord - predicted.xCoord);
            double dz = Math.abs(previous.zCoord - predicted.zCoord);
            double speed = Math.sqrt(dx * dx + dz * dz);
            MotionSimulator sample = new MotionSimulator();
            sample.field14 = sample.isOnStairs(previous);
            AxisAlignedBB bounds = new AxisAlignedBB(predicted.xCoord - 0.300000011920929,
                    predicted.yCoord - 0.015625, predicted.zCoord - 0.300000011920929,
                    predicted.xCoord + 0.300000011920929, predicted.yCoord + 1.799999952316284,
                    predicted.zCoord + 0.300000011920929);
            for (AxisAlignedBB collision : mc.theWorld.getCollisionBoxes(bounds)) {
                if (predicted.yCoord >= collision.minY) { sample.onGround = true; break; }
            }
            sample.speed = speed;
            sample.isOnIce(predicted);
            sample.field13 = sample.hasBlockTwoBelow(predicted);
            MovementUtil.getSpeedWithPotion(0.2873, 0.2);
            sample.previous = previous;
            sample.position = predicted;
            Adjust.movementPredictor.current = sample;
            predictor.flags.after(sample, last);
            predictor.flags.before(sample, last);
        } catch (Exception exception) { exception.printStackTrace(); }
    }
    public void onCorrection(S08PacketPlayerPosLook packet) {
        if (!packet.func_179834_f().isEmpty()) return;
        double x = packet.getX(), y = packet.getY(), z = packet.getZ();
        if (x % 0.5 != 0.0 || z % 0.5 != 0.0) { predictor.historyTicks = 0; return; }
        for (MotionSimulator sample : Adjust.movementPredictor.history) {
            Vec3 point = sample.position;
            double dx = x - point.xCoord, dz = z - point.zCoord, dy = y - point.yCoord;
            if (point.yCoord > y) {
                double fall = Math.abs(point.yCoord - y);
                if (fall > 1.0 && fall < 8.5 && dx * dx + dz * dz < 1.0) {
                    predictor.historyTicks = 0; return;
                }
            }
            if (dy * dy + dx * dx + dz * dz < 0.1) { predictor.historyTicks = 0; return; }
        }
    }
}
