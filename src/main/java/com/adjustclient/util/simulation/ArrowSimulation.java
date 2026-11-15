package com.adjustclient.util.simulation;

import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public final class ArrowSimulation extends ProjectileSimulation {
    @Override
    public MovingObjectPosition rayTraceBlocks(Vec3 start, Vec3 end) {
        return world.rayTraceBlocks(start, end, false, true, false);
    }

    public void initialize(World world, float eyeHeight, double x, double y, double z, float yaw, float pitch, float velocity) {
        this.world = world;
        collision = null;
        source = null;
        thrower = null;
        ticksInAir = 0;
        posX = x;
        posY = y + eyeHeight;
        posZ = z;
        width = height = 0.5F;
        rotationYaw = yaw;
        rotationPitch = pitch;
        posX -= MathHelper.cos(yaw / 180.0F * (float)Math.PI) * 0.16F;
        posY -= 0.1000000014901161;
        posZ -= MathHelper.sin(yaw / 180.0F * (float)Math.PI) * 0.16F;
        boundingBox = new AxisAlignedBB(posX - 0.25, posY, posZ - 0.25, posX + 0.25, posY + 0.5, posZ + 0.25);
        motionX = -MathHelper.sin(yaw / 180.0F * (float)Math.PI) * MathHelper.cos(pitch / 180.0F * (float)Math.PI);
        motionZ = MathHelper.cos(yaw / 180.0F * (float)Math.PI) * MathHelper.cos(pitch / 180.0F * (float)Math.PI);
        motionY = -MathHelper.sin(pitch / 180.0F * (float)Math.PI);
        setThrowableHeading(motionX, motionY, motionZ, velocity * 1.5F, 0.0F);
    }

    public void initialize(EntityArrow entity) {
        world = entity.worldObj;
        collision = null;
        posX = entity.posX;
        posY = entity.posY;
        posZ = entity.posZ;
        width = entity.width;
        height = entity.height;
        float halfWidth = width / 2.0F;
        boundingBox = new AxisAlignedBB(posX - halfWidth, posY, posZ - halfWidth, posX + halfWidth, posY + height, posZ + halfWidth);
        motionX = entity.motionX;
        motionY = entity.motionY;
        motionZ = entity.motionZ;
        source = entity.shootingEntity;
        thrower = entity;
        ticksInAir = entity.ticksInAir;
    }
}
