package com.adjustclient.event;

public final class BlockRenderStateEvent extends Event {
    public net.minecraft.util.BlockPos position;
    public net.minecraft.block.state.IBlockState state;

    public BlockRenderStateEvent(net.minecraft.util.BlockPos position, net.minecraft.block.state.IBlockState state) {
        this.position = position;
        this.state = state;
    }
}
