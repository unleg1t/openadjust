package com.adjustclient.util.anticheat.entity;

import net.minecraft.entity.projectile.EntityArrow;

public final class TrackedOtherEntity extends TrackedEntity<EntityArrow> {
    public Object field40;
    public TrackedOtherEntity(EntityArrow entity) { super(entity); }
    public boolean matchesType(int mask) { return (mask & 0x100) != 0; }
    public String getDisplayName() { return entity.getName(); }
}
