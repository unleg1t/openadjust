package com.adjustclient.event;

public final class AttackEntityEvent extends CancellableEvent {
    public net.minecraft.entity.Entity entity;

    public AttackEntityEvent(net.minecraft.entity.Entity entity) {
        this.entity = entity;
    }
}
