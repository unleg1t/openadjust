package com.adjustclient.util.anticheat.entity;

import net.minecraft.entity.Entity;

public interface Projectile {
    boolean isDangerous();
    float getImpactDistanceFactor();
}
