package com.adjustclient.module.impl.movement.scaffold;

import com.adjustclient.util.Vec2f;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

public final class PlacementTarget {

    public BlockPos pos;
    public EnumFacing facing;
    public Vec3 hitVec;
    public Vec2f rotation;
    public BlockPos placedPos;
    /** Player yaw (as double) at the time the placement was solved; read as a double by BlockCountHud.onPlace. */
    public double yaw;

    public PlacementTarget(BlockPos pos, EnumFacing facing, Vec3 hitVec, Vec2f rotation, double yaw) {
        this.pos = pos;
        this.facing = facing;
        this.hitVec = hitVec;
        this.rotation = rotation;
        this.yaw = yaw;
        this.placedPos = com.adjustclient.util.BlockUtil.getPlacePos(pos, facing);
    }
}
