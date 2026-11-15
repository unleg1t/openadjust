package com.adjustclient.module.impl.player.breaker;

import com.adjustclient.util.BlockUtil;
import com.adjustclient.util.MinecraftInstance;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

public class BlockReachCheck implements MinecraftInstance {
    public final BlockPos pos;
    public final BlockPos targetPos;
    public final Block block;
    public final List<AxisAlignedBB> rangeBoxes;
    public final List<Vec3> rangePoints = new ArrayList<>();
    public final ToolChoice tool;
    public final Vec3 center;
    public final List<BooleanSupplier> conditions = new ArrayList<>();
    public final AxisAlignedBB box;
    public final long breakTime;

    public BlockReachCheck(BlockPos pos, BlockPos targetPos, ToolChoice tool, long breakTime, BooleanSupplier... conditions) {
        this.pos = pos; this.targetPos = targetPos; this.tool = tool;
        block = mc.theWorld.getBlockState(pos).getBlock();
        box = Objects.requireNonNullElseGet(BlockUtil.getCollisionBox(pos),
                () -> new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0, pos.getY() + 1.0, pos.getZ() + 1.0));
        center = box.getCenter();
        rangeBoxes = buildRange();
        this.conditions.addAll(List.of(conditions));
        this.breakTime = breakTime;
    }

    public boolean isValid() {
        for (BooleanSupplier condition : conditions) if (!condition.getAsBoolean()) return false;
        return true;
    }

    private List<AxisAlignedBB> buildRange() {
        Vec3 origin = new Vec3(center.xCoord, pos.getY() + 0.01, center.zCoord);
        double reach = mc.playerController.getBlockReachDistance();
        double radius = mc.thePlayer.getEntityBoundingBox().maxX - mc.thePlayer.getEntityBoundingBox().minX + reach;
        List<AxisAlignedBB> boxes = new ArrayList<>();
        for (float angle = -180.0F; angle <= 180.0F; angle += 0.5F) {
            double radians = Math.toRadians(angle);
            double x = -Math.sin(radians), z = Math.cos(radians);
            Vec3 point = origin.addVector(radius * x, 0.0, radius * z);
            boxes.add(new AxisAlignedBB(point.xCoord - 0.25, point.yCoord - 0.25,
                    point.zCoord - 0.25, point.xCoord + 0.25, point.yCoord + 0.25,
                    point.zCoord + 0.25).expand(0.0, 10.0, 0.0));
            if (Math.abs(angle - (int) angle) <= 0.1F || angle >= 179.0F || angle <= -179.0F)
                rangePoints.add(origin.addVector(reach * x, 0.0, reach * z));
        }
        return boxes;
    }
}
