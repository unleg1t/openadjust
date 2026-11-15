package com.adjustclient.hypixel.game.zombies.entity;

import com.adjustclient.util.MinecraftInstance;
import net.minecraft.entity.Entity;

public abstract class TrackedEntityRef implements MinecraftInstance {
    public com.adjustclient.hypixel.game.zombies.ZombiesGameState state;
    public final int entityId;

    protected TrackedEntityRef(int entityId) { this.entityId = entityId; }
    public abstract void update();
    public abstract boolean isExpired();

    public Entity getEntity() {
        return mc.theWorld == null ? null : mc.theWorld.getEntityByID(entityId);
    }
}
