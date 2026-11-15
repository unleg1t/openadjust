package com.adjustclient.util;

public final class BlockPlacement {
    public net.minecraft.util.BlockPos pos;
    public net.minecraft.util.EnumFacing facing;
    public net.minecraft.util.Vec3 hitVec;
    public Vec2f angles;
    public Object field20;
    public BlockPlacement(net.minecraft.util.BlockPos pos, net.minecraft.util.EnumFacing facing,
                          net.minecraft.util.Vec3 hitVec, Vec2f angles) {
        this.pos = pos; this.facing = facing; this.hitVec = hitVec; this.angles = angles;
    }
    public boolean isInvalidHitVec() {
        net.minecraft.util.Vec3 relative = hitVec.subtract(new net.minecraft.util.Vec3(pos));
        double x = relative.xCoord, y = relative.yCoord, z = relative.zCoord;
        if (x < 0 || x > 1 || y < 0 || y > 1 || z < 0 || z > 1) return true;
        return switch (facing) {
            case NORTH -> z != 0;
            case EAST -> x != 1;
            case SOUTH -> z != 1;
            case WEST -> x != 0;
            case UP -> y != 1;
            case DOWN -> y != 0;
        };
    }
}
