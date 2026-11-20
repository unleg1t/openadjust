package com.adjustclient.core.events;

public final class BlockBreakProgressEvent extends Event {
    public net.minecraft.util.BlockPos pos;
    public float strength;
    public float damage;

    public BlockBreakProgressEvent(net.minecraft.util.BlockPos pos, float strength, float damage) {
        this.pos = pos;
        this.strength = strength;
        this.damage = damage;
    }
}
