package com.adjustclient.event;

public final class EntityOpacityEvent extends Event {
    public net.minecraft.entity.EntityLivingBase entity;
    public float bodyAlpha = 1.0F;
    public float armorAlpha = 1.0F;
    public boolean invisible;

    public EntityOpacityEvent(net.minecraft.entity.EntityLivingBase entity, float partialTicks) {
        this.entity = entity;
        this.invisible = entity.isInvisible();
    }
}
