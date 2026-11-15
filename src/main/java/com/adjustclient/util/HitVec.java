package com.adjustclient.util;

public final class HitVec extends net.minecraft.util.Vec3 {
    public Vec2f angles;
    public net.minecraft.util.EnumFacing facing;
    public net.minecraft.util.BlockPos blockPos;
    public Object field40;
    public HitVec(double x, double y, double z, Vec2f angles,
                  net.minecraft.util.EnumFacing facing, net.minecraft.util.BlockPos blockPos, Object field40) {
        super(x, y, z); this.angles = angles; this.facing = facing;
        this.blockPos = blockPos; this.field40 = field40;
    }
    @Override public boolean equals(Object other) {
        if (other != null && other.getClass() == HitVec.class) {
            HitVec that = (HitVec) other;
            return (this == that || (Double.compare(xCoord, that.xCoord) == 0
                    && Double.compare(yCoord, that.yCoord) == 0 && Double.compare(zCoord, that.zCoord) == 0))
                    && facing == that.facing && angles.x == that.angles.x && angles.y == that.angles.y;
        }
        return false;
    }
}
