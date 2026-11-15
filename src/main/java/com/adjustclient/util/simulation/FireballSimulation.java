package com.adjustclient.util.simulation;

import com.adjustclient.util.EntitySimulation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class FireballSimulation extends EntitySimulation {
    public EntityFireball entity;
    public MovingObjectPosition collision;
    public EntityLivingBase shooter;
    public double accelerationX;
    public double accelerationY;
    public double accelerationZ;
    public float partialTicks = 1.0F;
    public int ticksInAir;
    public Entity ignoredEntity;
    public int field124;

    public void initializeFromFireball(EntityFireball fireball) {
        entity = fireball;
        shooter = fireball.shootingEntity;
        world = fireball.worldObj;
        collision = null;
        accelerationX = fireball.accelerationX;
        accelerationY = fireball.accelerationY;
        accelerationZ = fireball.accelerationZ;
        motionX = fireball.motionX;
        motionY = fireball.motionY;
        motionZ = fireball.motionZ;
        posX = fireball.posX;
        posY = fireball.posY;
        posZ = fireball.posZ;
        rotationYaw = fireball.rotationYaw;
        rotationPitch = fireball.rotationPitch;
        width = fireball.width;
        height = fireball.height;
        float halfWidth = width / 2.0F;
        boundingBox = new AxisAlignedBB(posX - halfWidth, posY, posZ - halfWidth, posX + halfWidth, posY + height, posZ + halfWidth);
        ticksInAir = fireball.ticksInAir;
    }

    public void initialize(EntityLivingBase shooter, double x, double y, double z, float yaw, float pitch) {
        world = shooter.worldObj;
        entity = null;
        collision = null;
        posX = x;
        posY = y + shooter.getEyeHeight();
        posZ = z;
        rotationYaw = yaw;
        rotationPitch = pitch;
        width = height = 1.0F;
        boundingBox = new AxisAlignedBB(x - 0.5, posY, z - 0.5, x + 0.5, posY + 1.0, z + 0.5);
        float yawRadians = -yaw * 0.017453292F - (float)Math.PI;
        float pitchRadians = -pitch * 0.017453292F;
        float horizontal = -MathHelper.cos(pitchRadians);
        motionX = MathHelper.sin(yawRadians) * horizontal;
        motionY = MathHelper.sin(pitchRadians);
        motionZ = MathHelper.cos(yawRadians) * horizontal;
        accelerationX = motionX * 0.1;
        accelerationY = motionY * 0.1;
        accelerationZ = motionZ * 0.1;
        this.shooter = shooter;
        ticksInAir = 0;
    }

    public boolean onUpdate() {
        boolean water = getBlockState(entity);
        ++ticksInAir;
        Vec3 start = new Vec3(posX, posY, posZ);
        Vec3 end = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);
        MovingObjectPosition hit = world.rayTraceBlocks(start, end, false, false, false);
        if (hit != null) end = hit.hitVec;
        Entity closest = null;
        MovingObjectPosition closestIntersection = null;
        double nearestDistance = 0.0;
        for (Entity candidate : world.getEntitiesWithinAABBExcludingEntity(entity,
                boundingBox.addCoord(motionX, motionY, motionZ).expand(1.0, 1.0, 1.0))) {
            if (!candidate.canBeCollidedWith()) continue;
            if (candidate.isEntityEqual(shooter) && ticksInAir < 25) continue;
            AxisAlignedBB box = candidate.getEntityBoundingBox().offset(
                    candidate.lastTickPosX + (candidate.posX - candidate.lastTickPosX) * partialTicks - candidate.posX,
                    candidate.lastTickPosY + (candidate.posY - candidate.lastTickPosY) * partialTicks - candidate.posY,
                    candidate.lastTickPosZ + (candidate.posZ - candidate.lastTickPosZ) * partialTicks - candidate.posZ)
                    .expand(0.300000011920929, 0.300000011920929, 0.300000011920929);
            MovingObjectPosition intersection = box.calculateIntercept(start, end);
            if (intersection == null) continue;
            double distance = start.squareDistanceTo(intersection.hitVec);
            if (distance < nearestDistance || nearestDistance == 0.0) {
                nearestDistance = distance;
                closest = candidate;
                closestIntersection = intersection;
            }
        }
        if (closest != null) {
            collision = new MovingObjectPosition(closest, closestIntersection.hitVec);
            collision.sideHit = closestIntersection.sideHit;
            return true;
        }
        if (hit != null) {
            collision = hit;
            return true;
        }
        posX += motionX;
        posY += motionY;
        posZ += motionZ;
        double drag = water ? (double)0.8F : (double)0.95F;
        motionX = (motionX + accelerationX) * drag;
        motionY = (motionY + accelerationY) * drag;
        motionZ = (motionZ + accelerationZ) * drag;
        float halfWidth = width / 2.0F;
        boundingBox = new AxisAlignedBB(posX - halfWidth, posY, posZ - halfWidth, posX + halfWidth, posY + height, posZ + halfWidth);
        return false;
    }
}
