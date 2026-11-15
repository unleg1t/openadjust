package com.adjustclient.event;

public final class ProjectileShooterEvent extends Event {
    public com.adjustclient.util.anticheat.entity.TrackedProjectile projectile;
    public net.minecraft.entity.Entity shooter;

    public ProjectileShooterEvent(com.adjustclient.util.anticheat.entity.TrackedProjectile projectile, net.minecraft.entity.Entity shooter) {
        this.projectile = projectile;
        this.shooter = shooter;
    }
}
