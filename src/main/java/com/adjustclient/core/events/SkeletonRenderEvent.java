package com.adjustclient.core.events;

public final class SkeletonRenderEvent extends Event {
    public net.minecraft.client.entity.AbstractClientPlayer player;
    public net.minecraft.client.model.ModelPlayer model;
    public float partialTicks;

    public SkeletonRenderEvent(net.minecraft.client.entity.AbstractClientPlayer player,
            net.minecraft.client.model.ModelPlayer model, float partialTicks) {
        this.player = player;
        this.model = model;
        this.partialTicks = partialTicks;
    }
}
