package com.adjustclient.hypixel.game.zombies.entity;

import com.adjustclient.hypixel.game.zombies.ZombiesGameState;
import com.adjustclient.hypixel.game.zombies.ZombiesPowerUp;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.IChatComponent;

public final class ZombiesEntityTracker extends TrackedEntityRef {
    public Object field12;
    public ZombiesEntityTracker(ZombiesGameState state, int id) { super(id); this.state = state; }
    @Override public void update() {
        Entity entity = getEntity();
        if (entity == null || mc.theWorld == null) return;
        Entity nearest = null;
        double distance = Double.MAX_VALUE;
        for (Entity candidate : mc.theWorld.loadedEntityList) {
            if (candidate == entity || !candidate.hasCustomName() || !(candidate instanceof EntityArmorStand)) continue;
            double d = candidate.getDistanceSqToEntity(entity);
            if (distance > d) { distance = d; nearest = candidate; }
        }
        if (nearest == null || !(getEntity() instanceof EntityItem item)) return;
        if (nearest.getDisplayName().getSiblings().size() != 1) return;
        IChatComponent label = nearest.getDisplayName().getSiblings().get(0);
        if (!label.getChatStyle().getBold()) return;
        for (ZombiesPowerUp powerUp : ZombiesPowerUp.values()) {
            if (powerUp.map != null && powerUp.map != state.map) continue;
            if (label.getChatStyle().getColor() == powerUp.color
                    && item.getEntityItem().getItem() == powerUp.icon.getItem()) {
                replace(new ZombiesEntity(state, entityId, powerUp));
                if (state.isNewEntity(entityId)) state.onPowerUp(powerUp);
                return;
            }
        }
    }
    public void replace(TrackedEntityRef replacement) {
        if (replacement.entityId != entityId)
            throw new RuntimeException("Mismatched entity IDs :" + replacement.entityId + " != " + entityId);
        state.entities.remove(entityId);
        state.entities.put(entityId, replacement);
    }
    @Override public boolean isExpired() { return getEntity() == null; }
}
