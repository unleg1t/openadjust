package com.adjustclient.core.events;

public final class BlockBreakEvent extends Event {
    public net.minecraft.util.BlockPos pos;

    public BlockBreakEvent(net.minecraft.util.BlockPos pos) {
        this.pos = pos;
    }
}
