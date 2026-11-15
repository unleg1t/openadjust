package com.adjustclient.util.anticheat.entity;

import com.adjustclient.render.RenderUtil;
import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Vec3;
import com.adjustclient.util.Vec2f;
import org.joml.Vector4d;

public abstract class TrackedEntity<T extends Entity> {
    public T entity;
    public Class<?> entityClass;
    public com.adjustclient.util.Timer removalTimer;
    public Team team;
    public Animation movementAnimation;
    public Float previewData;
    public int entityId;
    public int retentionMillis = 15000;
    public boolean removed;
    public boolean stale;
    public boolean animationReturning;
    public boolean seenInFrustum;

    protected TrackedEntity(T entity) {
        this.entity = entity;
        this.entityClass = entity.getClass();
        this.entityId = entity.getEntityId();
        this.removalTimer = new com.adjustclient.util.Timer();
    }

    public T getEntity() { return entity; }

    public Vector4d getRenderPosition(net.minecraft.client.gui.ScaledResolution context, float partialTicks) {
        return RenderUtil.projectEntityBounds(entity, context, !isRemoved(), partialTicks);
    }

    public boolean isRemoved() { return removed; }

    public void update() {
        Vec3 previous = new Vec3(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ);
        Vec3 position = new Vec3(entity.posX, entity.posY, entity.posZ);
        if (previous.distanceTo(position) > 0.01 && movementAnimation == null) {
            movementAnimation = new Animation(Easing.EASE_IN_OUT_CUBIC, 540L);
            movementAnimation.value = 1.0F;
        }
        if (movementAnimation != null) {
            movementAnimation.update(animationReturning ? 1.0F : 0.43F);
            if (movementAnimation.finished) {
                if (animationReturning) {
                    movementAnimation = null;
                    animationReturning = false;
                } else animationReturning = true;
            }
        }
        if (isRemoved() && !seenInFrustum && RenderUtil.isInFrustum(entity)) seenInFrustum = true;
        previewData = null;
        if (entity instanceof EntityPlayer) team = ((EntityPlayer) entity).getTeam();
        updateState();
    }

    protected void updateState() { }

    public String getName() { return getDisplayName(); }

    public abstract String getDisplayName();
    public abstract boolean matchesType(int filterMask);

    public boolean matches(int filterMask) {
        if (entity.isUser() && (filterMask & 0x80) == 0) return false;
        return !isRemoved() || (filterMask & 1) != 0;
    }
}
