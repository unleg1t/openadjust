package com.adjustclient.util.anticheat.entity;

import com.adjustclient.util.anticheat.ServerTracker;
import com.adjustclient.util.simulation.FireballSimulation;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TrackedFireball extends TrackedProjectile<EntityFireball> {
    public Boolean dangerous;
    public Vec3 impactPosition;
    public boolean simulated;
    public Object field56;
    public TrackedFireball(EntityFireball entity) { super(entity); }

    public boolean isDangerous() {
        if (removed) return false;
        if (dangerous == null) {
            simulate();
            dangerous = (shooter == null || !shooter.isUser()) && impactPosition != null
                    && Minecraft.getMinecraft().thePlayer.getPositionVector().distanceTo(impactPosition) < 8.0;
        }
        return dangerous;
    }
    public float getImpactDistanceFactor() {
        simulate();
        return impactPosition == null ? 0.0F : (float) Math.min(1.0,
                Minecraft.getMinecraft().thePlayer.getPositionVector().distanceTo(impactPosition) / 8.0);
    }
    public boolean matchesType(int mask) { return (mask & 0x100) != 0; }
    public double getLaunchDistance(Entity candidate) {
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
        y += candidate.isSneaking() ? 1.53125 : 1.59375;
        return Math.abs(entity.posX - x) + Math.abs(entity.posY - y) + Math.abs(entity.posZ - z);
    }
    public void update() {
        dangerous = null;
        simulated = false;
        if (entity.ticksExisted == 0) {
            List<TrackedLivingEntity> candidates = ServerTracker.INSTANCE.entityTracker.getEntities(226, TrackedLivingEntity.class);
            if (candidates.isEmpty()) return;
            Map<EntityLivingBase, Double> distances = new HashMap<>();
            for (TrackedLivingEntity tracked : candidates) {
                EntityLivingBase candidate = tracked.getEntity();
                double distance = getLaunchDistance(candidate);
                double movement = Math.abs(candidate.posX - candidate.lastTickPosX)
                        + Math.abs(candidate.posY - candidate.lastTickPosY) + Math.abs(candidate.posZ - candidate.lastTickPosZ);
                if (distance <= movement * 2.75 + 2.5) distances.put(candidate, distance);
            }
            if (distances.isEmpty()) return;
            setShooter(distances.keySet().stream().min(Comparator.comparingDouble(candidate -> {
                return distances.get(candidate).doubleValue();
            })).get());
        }
        super.update();
    }
    public String getDisplayName() { return "Fireball"; }
    public String getShooterName() { return shooter == null ? null : shooter.isUser() ? "You" : shooter.getName(); }
    public void simulate() {
        if (simulated) return;
        simulated = true;
        FireballSimulation simulation = new FireballSimulation();
        simulation.initializeFromFireball(entity);
        for (int tick = 0; tick < 400; ++tick) if (simulation.onUpdate()) break;
        var hit = simulation.collision;
        if (hit == null) return;
        if (hit.entityHit != null) impactPosition = hit.entityHit.getPositionVector();
        else if (hit.hitVec != null) impactPosition = new Vec3(hit.hitVec.xCoord, hit.hitVec.yCoord, hit.hitVec.zCoord);
    }
}
