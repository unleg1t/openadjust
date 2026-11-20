package com.adjustclient.modules.impl.render.trajectories;

import com.adjustclient.modules.impl.render.trajectories.predictor.*;
import com.adjustclient.modules.impl.render.trajectories.settings.TrajectoryOwnerSettings;
import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.utilities.Vec2f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.*;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public final class TrajectoryRenderer {
    public Vec2f rotation;
    public final Entity entity;
    public final ProjectilePredictor predictor;
    public final TrajectoryOwnerSettings settings;
    public final ProjectileType type;
    public TrajectoryPath path;
    public Animation animation;
    public double x0;
    public double y0;
    public double z0;
    public double x1;
    public double y1;
    public double z1;
    public double x2;
    public double y2;
    public double z2;
    public int ticks;
    public final boolean projectile;
    public boolean initialized;

    public TrajectoryRenderer(Entity entity, ProjectilePredictor predictor, ProjectileType type,
                              TrajectoryOwnerSettings settings, boolean projectile) {
        this.entity = entity;
        this.predictor = predictor;
        this.type = type;
        this.settings = settings;
        this.projectile = projectile;
        update();
    }

    public void update() {
        if (projectile) refresh();
        else update(new Vec2f(entity.rotationYaw, entity.rotationPitch), 1.0F);
    }

    public Vec3 interpolate(float partialTicks) {
        return new Vec3(entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks,
                entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks,
                entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks);
    }

    public void update(Vec2f rotation, float partialTicks) {
        this.rotation = rotation;
        EntityPlayer player = (EntityPlayer) entity;
        Vec3 origin = interpolate(partialTicks);
        if (predictor instanceof FireballPredictor)
            path = ((FireballPredictor) predictor).predictHeld(player, origin, rotation, partialTicks);
        else path = predictor.predict(player, origin, rotation, partialTicks);
    }

    public void alpha() {
        if (entity instanceof EntityFireball) {
            EntityFireball fireball = (EntityFireball) entity;
            if (x2 != fireball.accelerationX || y2 != fireball.accelerationY || z2 != fireball.accelerationZ) {
                refresh();
                return;
            }
        }
        if (!initialized || ticks != entity.ticksExisted || x0 != entity.posX || y0 != entity.posY || z0 != entity.posZ
                || x1 != entity.motionX || y1 != entity.motionY || z1 != entity.motionZ) refresh();
    }

    public void refresh() {
        if (entity instanceof EntityFireball) {
            EntityFireball fireball = (EntityFireball) entity;
            path = ((FireballPredictor) predictor).predict(fireball);
            x2 = fireball.accelerationX;
            y2 = fireball.accelerationY;
            z2 = fireball.accelerationZ;
        } else if (entity instanceof EntityArrow) {
            path = ((HeldProjectilePredictor) predictor).predict((EntityArrow) entity);
        } else path = ((ThrowableProjectilePredictor) predictor).predict((EntityThrowable) entity);
        ticks = entity.ticksExisted;
        x0 = entity.posX;
        y0 = entity.posY;
        z0 = entity.posZ;
        x1 = entity.motionX;
        y1 = entity.motionY;
        z1 = entity.motionZ;
        initialized = true;
    }

    public boolean hasMoved() {
        MovingObjectPosition hit = path.hit;
        if (hit == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY) return false;
        Entity target = hit.entityHit;
        return target.lastTickPosX != target.posX || target.lastTickPosY != target.posY || target.lastTickPosZ != target.posZ;
    }
}
