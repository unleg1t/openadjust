package com.adjustclient.utilities;

import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public final class DistanceUtil {
    public static float distanceToBlock(Vec3 eyes, BlockPos pos) {
        double x = Math.max(pos.getX(), Math.min(pos.getX() + 1, eyes.xCoord));
        double y = Math.max(pos.getY(), Math.min(pos.getY() + 1, eyes.yCoord));
        double z = Math.max(pos.getZ(), Math.min(pos.getZ() + 1, eyes.zCoord));
        double dx = eyes.xCoord - x, dy = eyes.yCoord - y, dz = eyes.zCoord - z;
        return (float) (dx * dx + dy * dy + dz * dz);
    }
    public static float distanceToEntity(Vec3 eyes, Entity entity) {
        AxisAlignedBB box = entity.getEntityBoundingBox();
        float border = entity.getCollisionBorderSize();
        box = box.expand(border, border, border);
        Vec3 point = closestPoint(eyes, box);
        double dx = eyes.xCoord - point.xCoord, dy = eyes.yCoord - point.yCoord, dz = eyes.zCoord - point.zCoord;
        return MathHelper.sqrt_double(dx * dx + dy * dy + dz * dz);
    }
    public static Vec3 closestPoint(Vec3 point, AxisAlignedBB box) {
        return new Vec3(Math.max(box.minX, Math.min(box.maxX, point.xCoord)),
                Math.max(box.minY, Math.min(box.maxY, point.yCoord)),
                Math.max(box.minZ, Math.min(box.maxZ, point.zCoord)));
    }
}
