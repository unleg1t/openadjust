package com.adjustclient.modules.impl.player.breaker;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class BreakTarget {
    public final BlockPos pos;
    public final java.util.List<EnumFacing> exposedFaces;
    public final IBlockState state;
    public ToolChoice tool;
    public long breakTime;
    public BreakTarget(BlockPos pos, java.util.List<EnumFacing> exposedFaces, IBlockState state) {
        this.pos = pos; this.exposedFaces = exposedFaces; this.state = state;
    }
}
