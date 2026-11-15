package com.adjustclient.util.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;

public final class PotionSimulation extends ProjectileSimulation {
    public Object field100;

    @Override
    public List<Entity> getCollisionEntities() {
        AxisAlignedBB search = boundingBox.addCoord(motionX, motionY, motionZ).expand(1.0, 1.0, 1.0);
        List<Entity> entities = new ArrayList<>(super.getCollisionEntities());
        if (target != null && search.intersectsWith(target.getEntityBoundingBox())) entities.add(target);
        return Collections.unmodifiableList(entities);
    }
}
