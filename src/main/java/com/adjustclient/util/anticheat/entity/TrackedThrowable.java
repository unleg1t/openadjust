package com.adjustclient.util.anticheat.entity;

import com.adjustclient.util.anticheat.ServerTracker;
import com.adjustclient.util.simulation.ProjectileSimulation;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.projectile.*;
import net.minecraft.util.*;
import java.util.*;

public final class TrackedThrowable extends TrackedProjectile<EntityThrowable> {
    public Boolean dangerous;
    public MovingObjectPosition impact;
    public Vec3 previousPosition;
    public Float shooterEyeHeight;
    public boolean simulated;
    public Object field64;
    public TrackedThrowable(EntityThrowable entity) { super(entity); }

    public boolean isDangerous() {
        if (removed || shooter != null && shooter.isUser()) return false;
        if (dangerous != null) return dangerous;
        simulate();
        if (impact == null) return dangerous = false;
        var player = Minecraft.getMinecraft().thePlayer;
        if (impact.entityHit != null && impact.entityHit == player) return dangerous = true;
        if (entity instanceof EntityEnderPearl) {
            return dangerous = impact.getBlockPos() != null
                    && player.getPositionVector().distanceTo(new Vec3(impact.getBlockPos())) < 12.0;
        }
        return dangerous = false;
    }
    public float getImpactDistanceFactor() {
        if (simulated && entity instanceof EntityEnderPearl) {
            if (impact != null && impact.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
                return (float) Math.min(1.0, Minecraft.getMinecraft().thePlayer.getPositionVector()
                        .distanceTo(new Vec3(impact.getBlockPos())) / 12.0);
            return 0.0F;
        }
        if (removed || shooter != null && shooter.isUser()) return 1.0F;
        return isDangerous() ? 0.0F : 1.0F;
    }
    public boolean matchesType(int mask) { return (mask & 0x100) != 0; }

    public void initializeLaunchPosition(EntityLivingBase candidate) {
        shooterEyeHeight = candidate == null ? 1.53F : candidate.getEyeHeight();
        float yaw = entity.rotationYaw / 180.0F * (float) Math.PI;
        previousPosition = new Vec3(entity.posX + MathHelper.cos(yaw) * 0.16F,
                entity.posY - shooterEyeHeight + 0.1000000014901161,
                entity.posZ + MathHelper.sin(yaw) * 0.16F);
    }

    public double getLaunchDistance(EntityLivingBase candidate) {
        double x, y, z;
        if (candidate.isUser()) {
            x = MathHelper.floor_double(candidate.posX * 32.0) / 32.0;
            y = MathHelper.floor_double(candidate.posY * 32.0) / 32.0;
            z = MathHelper.floor_double(candidate.posZ * 32.0) / 32.0;
        } else {
            x = candidate.serverPosX / 32.0;
            y = candidate.serverPosY / 32.0;
            z = candidate.serverPosZ / 32.0;
        }
        float yaw = candidate.rotationYaw / 180.0F * (float) Math.PI;
        x = MathHelper.floor_double((x - MathHelper.cos(yaw) * 0.16F) * 32.0) / 32.0;
        y = MathHelper.floor_double((y + candidate.getEyeHeight() - 0.1000000014901161) * 32.0) / 32.0;
        z = MathHelper.floor_double((z - MathHelper.sin(yaw) * 0.16F) * 32.0) / 32.0;
        return Math.abs(entity.posX - x) + Math.abs(entity.posY - y) + Math.abs(entity.posZ - z);
    }

    public void update() {
        if (entity.ticksExisted == 0) {
            initializeLaunchPosition(null);
            List<TrackedLivingEntity> candidates = ServerTracker.INSTANCE.entityTracker.getEntities(226, TrackedLivingEntity.class);
            if (candidates.isEmpty()) return;
            Map<EntityLivingBase, Double> distances = new HashMap<>();
            for (TrackedLivingEntity tracked : candidates) {
                EntityLivingBase candidate = tracked.getEntity();
                double distance = getLaunchDistance(candidate);
                double movement = Math.abs(candidate.posX - candidate.prevPosX)
                        + Math.abs(candidate.posY - candidate.prevPosY) + Math.abs(candidate.posZ - candidate.prevPosZ);
                if (distance <= movement * 2.75 + 1.5) distances.put(candidate, distance);
            }
            if (distances.isEmpty()) return;
            EntityLivingBase candidate = distances.keySet().stream().min(Comparator.comparingDouble(value -> {
                return distances.get(value).doubleValue();
            })).get();
            setShooter(candidate);
            initializeLaunchPosition(candidate);
        }
        super.update();
    }

    public String getDisplayName() {
        Objects.requireNonNull(entity);
        if (entity instanceof EntityEgg) return StatCollector.translateToLocal("item.egg.name");
        if (entity instanceof EntityEnderPearl) return StatCollector.translateToLocal("item.enderPearl.name");
        return entity.getName();
    }
    public String getShooterName() { return shooter == null ? null : shooter.isUser() ? "You" : shooter.getName(); }

    public void simulate() {
        if (previousPosition == null) return;
        simulated = true;
        ProjectileSimulation simulation = new ProjectileSimulation();
        simulation.setThrowableHeading(Minecraft.getMinecraft().theWorld, shooterEyeHeight,
                previousPosition.xCoord, previousPosition.yCoord, previousPosition.zCoord,
                -entity.rotationYaw, -entity.rotationPitch);
        do {
            simulation.onUpdate();
        } while (simulation.collision == null && simulation.ticksInAir < 201);
        impact = simulation.collision;
    }
}
