package com.adjustclient.event;

public final class EntityRenderEligibilityEvent extends Event {
    public net.minecraft.entity.Entity entity;
    public boolean forceRender;

    public EntityRenderEligibilityEvent(net.minecraft.entity.Entity entity) {
        this.entity = entity;
    }
}
