package com.adjustclient.event;

public final class EntityNameRenderEvent extends CancellableEvent {
    public net.minecraft.entity.Entity entity;

    public EntityNameRenderEvent(net.minecraft.entity.Entity entity) {
        this.entity = entity;
    }
}
