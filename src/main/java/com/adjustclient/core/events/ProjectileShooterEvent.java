package com.adjustclient.core.events;

public final class ProjectileShooterEvent extends Event {
    public com.adjustclient.utilities.anticheat.entity.TrackedProjectile projectile;
    public net.minecraft.entity.Entity shooter;

    public ProjectileShooterEvent(com.adjustclient.utilities.anticheat.entity.TrackedProjectile projectile, net.minecraft.entity.Entity shooter) {
        this.projectile = projectile;
        this.shooter = shooter;
    }
}
