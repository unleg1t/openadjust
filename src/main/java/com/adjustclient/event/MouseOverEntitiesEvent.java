package com.adjustclient.event;

public final class MouseOverEntitiesEvent extends Event {
    public java.util.List<net.minecraft.entity.Entity> entities;

    public MouseOverEntitiesEvent(java.util.List<net.minecraft.entity.Entity> entities) {
        this.entities = entities;
    }
}
