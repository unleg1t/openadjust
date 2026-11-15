package com.adjustclient.event;

public class EntityCollisionBorderEvent extends Event {
    public net.minecraft.entity.Entity entity;
    public float border;

    public EntityCollisionBorderEvent(net.minecraft.entity.Entity entity, float border) {
        this.entity = entity;
        this.border = border;
    }
}
