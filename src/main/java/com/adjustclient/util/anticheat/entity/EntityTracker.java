package com.adjustclient.util.anticheat.entity;

import com.adjustclient.module.impl.combat.antibot.AntiBotModule;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.*;
import net.minecraft.entity.item.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.projectile.*;
import net.minecraft.world.World;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;

public final class EntityTracker {
    public Int2ObjectOpenHashMap<TrackedEntity<?>> entities = new Int2ObjectOpenHashMap<>();
    public Object field8;

    public void update() {
        Minecraft mc = Minecraft.getMinecraft();
        Int2ObjectOpenHashMap<TrackedEntity<?>> next = new Int2ObjectOpenHashMap<>(entities);
        removeStale(next, mc.theWorld);
        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityArmorStand) continue;
            if (!(entity instanceof EntityLiving || entity instanceof EntityPlayer || entity instanceof EntityItem
                    || entity instanceof EntityFireball || entity instanceof EntityArrow || entity instanceof EntitySnowball
                    || entity instanceof EntityTNTPrimed || entity instanceof EntityEgg || entity instanceof EntityEnderPearl)) continue;
            if (AntiBotModule.INSTANCE.isEnabled() && AntiBotModule.isBot(entity.getEntityId())) continue;
            TrackedEntity<?> tracked = entities.get(entity.getEntityId());
            if (tracked == null || tracked.entityClass != entity.getClass()) {
                tracked = createTracked(entity);
                next.put(tracked.entityId, tracked);
            }
            ((TrackedEntity) tracked).entity = entity;
        }
        for (TrackedEntity<?> tracked : next.values()) tracked.update();
        entities = next;
    }

    public TrackedEntity<?> get(Entity entity) { return entity == null ? null : entities.get(entity.getEntityId()); }

    public void removeStale(Int2ObjectOpenHashMap<TrackedEntity<?>> next, World world) {
        List<TrackedEntity<?>> discarded = new ArrayList<>();
        for (TrackedEntity<?> tracked : next.values()) {
            Entity entity = tracked.getEntity();
            if (entity instanceof EntityArmorStand || AntiBotModule.INSTANCE.isEnabled() && AntiBotModule.isBot(entity.getEntityId())) {
                discarded.add(tracked);
                continue;
            }
            boolean present = false;
            for (Entity loaded : world.loadedEntityList) if (loaded == entity) { present = true; break; }
            if (present) { tracked.removed = false; continue; }
            boolean transientEntity = entity.isDead || entity.ticksExisted < 6;
            if (tracked.isRemoved() || transientEntity) {
                int timeout = tracked.seenInFrustum ? (int) (tracked.retentionMillis / 2.5F) : tracked.retentionMillis;
                if (tracked.removalTimer.hasElapsed(timeout) || transientEntity)
                    discarded.add(tracked);
            } else {
                tracked.removed = true;
                tracked.removalTimer.reset();
            }
        }
        for (TrackedEntity<?> tracked : discarded) { tracked.stale = true; next.remove(tracked.entityId); }
    }

    public static TrackedEntity<?> createTracked(Entity entity) {
        java.util.Objects.requireNonNull(entity);
        if (entity instanceof EntityIronGolem || entity instanceof net.minecraft.entity.monster.EntitySilverfish)
            return new TrackedPlayer((EntityLivingBase) entity);
        if (entity instanceof net.minecraft.entity.passive.EntityTameable || entity instanceof net.minecraft.entity.passive.EntityHorse)
            return new TrackedLocalPlayer((EntityLivingBase) entity);
        if (entity instanceof EntityThrowable) return new TrackedThrowable((EntityThrowable) entity);
        if (entity instanceof EntityArrow) return new TrackedOtherEntity((EntityArrow) entity);
        if (entity instanceof EntityFireball) return new TrackedFireball((EntityFireball) entity);
        if (entity instanceof EntityTNTPrimed) return new TrackedTnt((EntityTNTPrimed) entity);
        if (entity instanceof EntityItem) return new TrackedItem((EntityItem) entity);
        if (entity instanceof EntityPlayer) return new TrackedFakePlayer((EntityPlayer) entity);
        return new TrackedLivingEntity((EntityLivingBase) entity);
    }

    public TrackedFakePlayer getLocalPlayer() {
        TrackedEntity<?> tracked = get(Minecraft.getMinecraft().thePlayer);
        return tracked instanceof TrackedFakePlayer ? (TrackedFakePlayer) tracked : null;
    }

    public TrackedFakePlayer getByName(String name) {
        for (TrackedFakePlayer tracked : getEntities(162, TrackedFakePlayer.class))
            if (!tracked.stale && !tracked.removed && tracked.getEntity() != null
                    && tracked.getEntity().getGameProfile().getName().equals(name)) return tracked;
        return null;
    }

    public <T extends TrackedEntity<?>> T get(int entityId, Class<T> type) {
        TrackedEntity<?> tracked = entities.get(entityId);
        return type == null ? (T) tracked : type.isInstance(tracked) ? type.cast(tracked) : null;
    }

    public <T extends TrackedEntity<?>> List<T> getEntities(int mask, Class<T> type) {
        List<T> result = new ArrayList<>();
        for (TrackedEntity<?> tracked : entities.values())
            if ((type == null || type.isInstance(tracked)) && (mask == Integer.MAX_VALUE || tracked.matches(mask) && tracked.matchesType(mask)))
                result.add(type == null ? (T) tracked : type.cast(tracked));
        return result;
    }
}
