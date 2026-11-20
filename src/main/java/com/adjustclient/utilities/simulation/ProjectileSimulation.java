package com.adjustclient.utilities.simulation;

import com.adjustclient.utilities.EntitySimulation;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class ProjectileSimulation extends EntitySimulation implements IProjectile {
    public Entity source;
    public Entity thrower;
    public MovingObjectPosition collision;
    public float partialTicks = 1.0F;
    public int ticksInAir;
    public Entity target;

    public MovingObjectPosition rayTraceBlocks(Vec3 start, Vec3 end) {
        int endX = MathHelper.floor_double(end.xCoord), endY = MathHelper.floor_double(end.yCoord), endZ = MathHelper.floor_double(end.zCoord);
        int x = MathHelper.floor_double(start.xCoord), y = MathHelper.floor_double(start.yCoord), z = MathHelper.floor_double(start.zCoord);
        BlockPos pos = new BlockPos(x, y, z);
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block.canCollideCheck(state, false)) {
            MovingObjectPosition hit = block.collisionRayTrace(world, pos, start, end);
            if (hit != null) return hit;
        }
        for (int remaining = 200; remaining-- >= 0;) {
            if (x == endX && y == endY && z == endZ) return null;
            boolean crossX = endX != x, crossY = endY != y, crossZ = endZ != z;
            double bx = endX > x ? x + 1.0 : x, by = endY > y ? y + 1.0 : y, bz = endZ > z ? z + 1.0 : z;
            double dx = end.xCoord - start.xCoord, dy = end.yCoord - start.yCoord, dz = end.zCoord - start.zCoord;
            double tx = crossX ? (bx - start.xCoord) / dx : 999.0;
            double ty = crossY ? (by - start.yCoord) / dy : 999.0;
            double tz = crossZ ? (bz - start.zCoord) / dz : 999.0;
            if (tx == -0.0) tx = -0.0001;
            if (ty == -0.0) ty = -0.0001;
            if (tz == -0.0) tz = -0.0001;
            EnumFacing side;
            if (tx < ty && tx < tz) {
                side = endX > x ? EnumFacing.WEST : EnumFacing.EAST;
                start = new Vec3(bx, start.yCoord + dy * tx, start.zCoord + dz * tx);
            } else if (ty < tz) {
                side = endY > y ? EnumFacing.DOWN : EnumFacing.UP;
                start = new Vec3(start.xCoord + dx * ty, by, start.zCoord + dz * ty);
            } else {
                side = endZ > z ? EnumFacing.NORTH : EnumFacing.SOUTH;
                start = new Vec3(start.xCoord + dx * tz, start.yCoord + dy * tz, bz);
            }
            x = MathHelper.floor_double(start.xCoord) - (side == EnumFacing.EAST ? 1 : 0);
            y = MathHelper.floor_double(start.yCoord) - (side == EnumFacing.UP ? 1 : 0);
            z = MathHelper.floor_double(start.zCoord) - (side == EnumFacing.SOUTH ? 1 : 0);
            pos = new BlockPos(x, y, z);
            state = world.getBlockState(pos);
            block = state.getBlock();
            if (block.canCollideCheck(state, false)) {
                MovingObjectPosition hit = block.collisionRayTrace(world, pos, start, end);
                if (hit != null) return hit;
            }
        }
        return null;
    }

    public void setThrowableHeading(World world, float eyeHeight, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        collision = null;
        source = null;
        thrower = null;
        posX = x;
        posY = y;
        posZ = z;
        boundingBox = new AxisAlignedBB(x - width / 2.0F, y, z - width / 2.0F, x + width / 2.0F, y + height, z + width / 2.0F);
        width = 0.25F;
        height = 0.25F;
        posY = y + eyeHeight;
        rotationYaw = yaw;
        rotationPitch = pitch;
        boundingBox = new AxisAlignedBB(x - 0.125, posY, z - 0.125, x + 0.125, posY + 0.25, z + 0.125);
        posX -= MathHelper.cos(yaw / 180.0F * (float)Math.PI) * 0.16F;
        posY -= 0.1000000014901161;
        posZ -= MathHelper.sin(yaw / 180.0F * (float)Math.PI) * 0.16F;
        boundingBox = new AxisAlignedBB(posX - width / 2.0F, posY, posZ - width / 2.0F, posX + width / 2.0F, posY + height, posZ + width / 2.0F);
        motionX = -MathHelper.sin(yaw / 180.0F * (float)Math.PI) * MathHelper.cos(pitch / 180.0F * (float)Math.PI) * 0.4F;
        motionZ = MathHelper.cos(yaw / 180.0F * (float)Math.PI) * MathHelper.cos(pitch / 180.0F * (float)Math.PI) * 0.4F;
        float pitchOffset = this instanceof PotionSimulation ? -20.0F : 0.0F;
        motionY = -MathHelper.sin((pitch + pitchOffset) / 180.0F * (float)Math.PI) * 0.4F;
        setThrowableHeading(motionX, motionY, motionZ, this instanceof PotionSimulation ? 0.5F : 1.5F, 0.0F);
        ticksInAir = 0;
    }

    public void setHeadingFromThrower(World world, EntityLivingBase thrower, double x, double y, double z, float yaw, float pitch) {
        float eyeHeight = thrower == null ? 1.53F : thrower.getEyeHeight();
        setThrowableHeading(world, eyeHeight, x, y, z, yaw, pitch);
        source = thrower;
    }

    public float getWaterDrag() { return 0.8F; }

    @Override
    public void setThrowableHeading(double x, double y, double z, float velocity, float inaccuracy) {
        double length = Math.sqrt(x * x + y * y + z * z);
        motionX = (x / length + inaccuracy * 0.0) * velocity;
        motionY = (y / length + inaccuracy * 0.0) * velocity;
        motionZ = (z / length + inaccuracy * 0.0) * velocity;
        float horizontal = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
        rotationYaw = (float)(MathHelper.atan2(motionX, motionZ) * 180.0 / Math.PI);
        rotationPitch = (float)(MathHelper.atan2(motionY, horizontal) * 180.0 / Math.PI);
    }

    public List<Entity> getCollisionEntities() {
        return world.getEntitiesWithinAABBExcludingEntity(thrower, boundingBox.addCoord(motionX, motionY, motionZ).expand(1.0, 1.0, 1.0));
    }

    public Entity getIgnoredEntity() { return null; }

    public void advance(boolean water) {
        posX += motionX;
        posY += motionY;
        posZ += motionZ;
        double drag = water ? (this instanceof ArrowSimulation ? (double)0.6F : (double)getWaterDrag()) : (double)0.99F;
        double gravity = this instanceof ArrowSimulation || this instanceof PotionSimulation ? (double)0.05F : (double)0.03F;
        motionX *= drag;
        motionY = motionY * drag - gravity;
        motionZ *= drag;
        float halfWidth = width / 2.0F;
        boundingBox = new AxisAlignedBB(posX - halfWidth, posY, posZ - halfWidth, posX + halfWidth, posY + height, posZ + halfWidth);
    }

    public void initialize(EntityThrowable entity) {
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
        source = entity.getThrower();
        thrower = entity;
        ticksInAir = entity.ticksInAir;
    }

    public boolean onUpdate() {
        boolean water = getBlockState(thrower);
        ++ticksInAir;
        Vec3 start = new Vec3(posX, posY, posZ);
        Vec3 end = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);
        MovingObjectPosition blockHit = rayTraceBlocks(start, end);
        if (blockHit != null) end = blockHit.hitVec;
        Entity closestEntity = null;
        MovingObjectPosition closestIntersection = null;
        double closestDistance = 0.0;
        Entity ignored = this instanceof PotionSimulation ? ((PotionSimulation)this).target : getIgnoredEntity();
        for (Entity candidate : getCollisionEntities()) {
            if (!candidate.canBeCollidedWith()) continue;
            if ((candidate == source || candidate == ignored) && ticksInAir < 5) continue;
            AxisAlignedBB box = candidate.getEntityBoundingBox().offset(
                    candidate.lastTickPosX + (candidate.posX - candidate.lastTickPosX) * partialTicks - candidate.posX,
                    candidate.lastTickPosY + (candidate.posY - candidate.lastTickPosY) * partialTicks - candidate.posY,
                    candidate.lastTickPosZ + (candidate.posZ - candidate.lastTickPosZ) * partialTicks - candidate.posZ)
                    .expand(0.300000011920929, 0.300000011920929, 0.300000011920929);
            MovingObjectPosition intercept = box.calculateIntercept(start, end);
            if (intercept != null) {
                double distance = start.squareDistanceTo(intercept.hitVec);
                if (distance < closestDistance || closestDistance == 0.0) {
                    closestEntity = candidate;
                    closestIntersection = intercept;
                    closestDistance = distance;
                }
            }
        }
        if (closestEntity != null) {
            collision = new MovingObjectPosition(closestEntity, closestIntersection.hitVec);
            collision.sideHit = closestIntersection.sideHit;
            return true;
        }
        if (blockHit != null) {
            collision = blockHit;
            return true;
        }
        advance(water);
        return false;
    }
}
