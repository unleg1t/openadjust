package com.adjustclient.core.events;

public final class BlockRaytraceEvent extends CancellableEvent {
    public net.minecraft.entity.Entity viewEntity;
    public float partialTicks;
    public double reach;

    public BlockRaytraceEvent(net.minecraft.entity.Entity viewEntity, float partialTicks, double reach) {
        this.viewEntity = viewEntity;
        this.partialTicks = partialTicks;
        this.reach = reach;
    }
}
