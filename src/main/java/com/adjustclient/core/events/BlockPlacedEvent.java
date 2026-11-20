package com.adjustclient.core.events;

public final class BlockPlacedEvent extends Event {
    public net.minecraft.util.BlockPos pos;
    public net.minecraft.block.Block block;
    public net.minecraft.util.EnumFacing facing;

    public BlockPlacedEvent(net.minecraft.util.BlockPos pos, net.minecraft.block.Block block, net.minecraft.util.EnumFacing facing) {
        this.pos = pos;
        this.block = block;
        this.facing = facing;
    }
}
