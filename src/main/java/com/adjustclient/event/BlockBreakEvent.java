package com.adjustclient.event;

public final class BlockBreakEvent extends Event {
    public net.minecraft.util.BlockPos pos;

    public BlockBreakEvent(net.minecraft.util.BlockPos pos) {
        this.pos = pos;
    }
}
