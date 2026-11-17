package com.adjustclient.event;

public final class AttackEntityEvent extends CancellableEvent {
    public final net.minecraft.entity.Entity entity;
    public final boolean pre;

    public AttackEntityEvent(net.minecraft.entity.Entity entity, boolean pre) {
        this.entity = entity;
        this.pre = pre;
    }
}
