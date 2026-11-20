package com.adjustclient.utilities.placement;

import net.minecraft.util.*;

public final class BlockPlacementSample extends PlacementSample {
    public BlockPos position;
    public EnumFacing facing;
    public Vec3 hit;
    public BlockPos adjacent;
    public Object field24;
    public BlockPlacementSample(PlacementSnapshot context, BlockPos position, EnumFacing facing, Vec3 hit, BlockPos adjacent) {
        this.context = context; this.position = position; this.facing = facing; this.hit = hit; this.adjacent = adjacent;
    }
}
