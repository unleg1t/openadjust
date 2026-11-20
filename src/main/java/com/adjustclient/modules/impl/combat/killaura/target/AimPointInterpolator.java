package com.adjustclient.modules.impl.combat.killaura.target;

import com.adjustclient.utilities.rotation.AimPoint;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

public final class AimPointInterpolator {
    public AimPoint sample;
    public Vec3 previousOffset;
    public Vec3 offset;
    private int entityId;

    public Vec3 interpolate(Entity entity, AimPoint point, Vec3 position, float partialTicks) {
        if (sample != point) {
            Vec3 next = point.aimVec().subtract(new Vec3(entity.posX, entity.posY, entity.posZ));
            if (offset == null || entity.getEntityId() != entityId) previousOffset = next;
            else previousOffset = offset;
            sample = point;
            entityId = entity.getEntityId();
            offset = next;
        }
        return new Vec3(previousOffset.xCoord + (offset.xCoord - previousOffset.xCoord) * partialTicks,
                previousOffset.yCoord + (offset.yCoord - previousOffset.yCoord) * partialTicks,
                previousOffset.zCoord + (offset.zCoord - previousOffset.zCoord) * partialTicks).add(position);
    }
}
