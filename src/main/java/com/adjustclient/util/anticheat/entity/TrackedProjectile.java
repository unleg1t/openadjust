package com.adjustclient.util.anticheat.entity;

import com.adjustclient.event.ProjectileShooterEvent;
import com.adjustclient.module.impl.player.autohypixel.feature.bedwars.tracker.BedWarsTracker;
import com.adjustclient.server.ServerType;
import com.adjustclient.util.anticheat.ServerTracker;
import net.minecraft.entity.Entity;

public abstract class TrackedProjectile<T extends Entity> extends TrackedEntity<T> implements Projectile {
    public Entity shooter;
    protected TrackedProjectile(T entity) { super(entity); }
    public Entity getShooter() { return shooter; }
    public void setShooter(Entity shooter) {
        this.shooter = shooter;
        var tracker = BedWarsTracker.INSTANCE;
        if (tracker != null && tracker.isActive() && tracker.isFeatureEnabled()) {
            ProjectileShooterEvent event = new ProjectileShooterEvent(this, shooter);
            tracker.onProjectileSpawn(event);
        }
    }
}
