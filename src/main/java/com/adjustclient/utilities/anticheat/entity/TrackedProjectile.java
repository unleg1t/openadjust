package com.adjustclient.utilities.anticheat.entity;

import com.adjustclient.core.events.ProjectileShooterEvent;
import com.adjustclient.modules.impl.player.autohypixel.feature.bedwars.tracker.BedWarsTracker;
import com.adjustclient.utilities.server.ServerType;
import com.adjustclient.utilities.anticheat.ServerTracker;
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
