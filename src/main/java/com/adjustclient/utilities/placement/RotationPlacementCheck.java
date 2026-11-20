package com.adjustclient.utilities.placement;
import com.adjustclient.utilities.*;
import com.adjustclient.utilities.anticheat.ServerTracker;
import java.util.*;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.network.play.client.*;
import net.minecraft.util.*;
import com.adjustclient.Adjust;
public final class RotationPlacementCheck extends PlacementCheck<SimplePlacementContext> {
    public FloatArrayList yawChanges = new FloatArrayList();
    public LongArrayList intervals = new LongArrayList();
    public List<BlockPos> positions = new ArrayList<>();
    public float lastPitch;
    public double violations;
    public long lastPlacement;
    public Object field40;
    public void reset() {
        yawChanges.clear(); intervals.clear(); positions.clear(); lastPlacement = 0; violations = 0;
    }
    public boolean isStraightPath(List<BlockPos> path) {
        boolean first = true, sameX = false, sameZ = false;
        int remainingHeightChanges = 1, x = 0, y = 0, z = 0;
        for (BlockPos pos : path) {
            if (!first) {
                if (pos.getY() != y) {
                    if (remainingHeightChanges < 1) return false;
                    remainingHeightChanges--;
                } else {
                    boolean equalX = pos.getX() == x;
                    if (!equalX && sameX) return false;
                    if (pos.getZ() == z) sameZ = true;
                    else { if (sameZ) return false; sameZ = false; }
                    sameX = equalX;
                }
            }
            x = pos.getX(); y = pos.getY(); z = pos.getZ(); first = false;
        }
        return sameX || sameZ;
    }
    public void onMovement(PlacementSnapshot snapshot, C03PacketPlayer packet) {
        float yaw = packet.getRotating() ? packet.getYaw() : snapshot.yaw;
        float delta = Math.abs(MathHelper.wrapAngleTo180_float(yaw - snapshot.yaw));
        if (System.currentTimeMillis() - lastPlacement >= 2001) return;
        if (ServerTracker.INSTANCE.velocityTimer.hasElapsed(100L) && packet.getRotating()) {
            while (yawChanges.size() >= 61) yawChanges.removeFloat(0);
            yawChanges.add(delta);
        }
    }
    public void onPlacement(PlacementSnapshot snapshot, C08PacketPlayerBlockPlacement packet) {
        if (packet.getPlacedBlockDirection() == 255) return;
        EnumFacing side = EnumFacing.getFront(packet.getPlacedBlockDirection());
        if (side.getAxis() == EnumFacing.Axis.Y) return;
        if (!Adjust.placementVerifier.facingTracker.contains(packet)) return;
        BlockPos pos = packet.getPosition();
        long elapsed = Math.min(1000L, System.currentTimeMillis() - lastPlacement);
        while (positions.size() >= 5 || (!positions.isEmpty() && System.currentTimeMillis() - lastPlacement >= 5001))
            positions.remove(0);
        positions.add(pos);
        intervals.add(elapsed);
        lastPlacement = System.currentTimeMillis();
        double average = 500.0;
        if (intervals.size() >= 8) {
            average = intervals.longStream().asDoubleStream().average().orElse(500.0);
            intervals.removeLong(0);
        }
        Vec3 eyes = new Vec3(snapshot.x, snapshot.y + 1.620000004768372, snapshot.z);
        Vec3 center = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)
                .addVector(side.getFrontOffsetX() * 0.5, side.getFrontOffsetY() * 0.5, side.getFrontOffsetZ() * 0.5);
        Vec2f top = RotationUtil.getYawBetween(center.addVector(0, 0.5, 0), eyes);
        Vec2f bottom = RotationUtil.getYawBetween(center.addVector(0, -0.5, 0), eyes);
        RotationUtil.getYawBetween(center.addVector(side.getAxis() == EnumFacing.Axis.X ? 0 : 0.5, 0,
                side.getAxis() == EnumFacing.Axis.X ? 0.5 : 0), eyes);
        RotationUtil.getYawBetween(center.addVector(side.getAxis() == EnumFacing.Axis.X ? 0 : -0.5, 0,
                side.getAxis() == EnumFacing.Axis.X ? -0.5 : 0), eyes);
        float previous = lastPitch;
        float pitch = snapshot.pitch;
        lastPitch = pitch;
        float delta = Math.abs(pitch - previous);
        if (delta > 3.0F && delta < 20.0F && pitch > 70.0F) {
            float span = Math.abs(top.y - bottom.y);
            if (span < 5.0F) {
                if ((average < 400.0 || isStraightPath(positions)) && elapsed < 800) {
                    violations += Math.min(delta / (span / 10.0F), 20.0F);
                    if (pitch > 89.5F) violations += 5.0;
                    if (violations > 100.0) {
                        new PendingPlacement(SimplePlacementContext.class, "suspicious block-placement", "exhibits micro pitch adjustments", 10.0).report();
                        violations -= 10.0;
                    }
                }
                return;
            }
        }
        if (violations > 0) violations = violations * 0.99 - 0.01;
    }
}
