package com.adjustclient.event;

public final class EntityPreUpdateEvent extends Event {
    public net.minecraft.entity.Entity entity;

    public EntityPreUpdateEvent(net.minecraft.entity.Entity entity) {
        this.entity = entity;
    }
}
