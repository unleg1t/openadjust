package com.adjustclient.game.entity.tracker;

import com.adjustclient.Adjust;
import com.adjustclient.event.*;
import com.adjustclient.util.Timer;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import java.util.Objects;
import com.adjustclient.module.impl.combat.backtrack.BackTrackModule;
import com.adjustclient.module.impl.render.tweaks.TweaksModule;

public final class ServerPositionTracker implements Listener {
    public static ServerPositionTracker INSTANCE;
    public final Int2ObjectOpenHashMap<ServerPositionEntry> entries = new Int2ObjectOpenHashMap<>();
    public final Int2ObjectOpenHashMap<Timer> recentlyDestroyed = new Int2ObjectOpenHashMap<>();
    public boolean trackingActive = true;
    public Object field16;

    @EventHandler
    public void onLivingUpdate(com.adjustclient.event.EntityPreUpdateEvent event) {
        Entity entity = event.entity;
        if (!(entity instanceof EntityOtherPlayerMP)) return;
        ServerPositionEntry e = get(entity.getEntityId());
        if (e == null) return;
        e.previousX = e.x; e.previousY = e.y; e.previousZ = e.z;
        e.previousRenderYawOffset = e.renderYawOffset;
        e.previousHeadYaw = e.headYaw;
        e.previousYaw = e.yaw; e.previousPitch = e.pitch;
        e.previousChasingX = e.chasingX;
        e.previousChasingY = e.chasingY;
        e.previousChasingZ = e.chasingZ;
        double dx = e.x - e.chasingX;
        double dz = e.z - e.chasingZ;
        double dy = e.y - e.chasingY;
        if (dx > 10.0 || dx < -10.0) e.chasingX = e.previousChasingX = e.x;
        if (dz > 10.0 || dz < -10.0) e.chasingZ = e.previousChasingZ = e.z;
        if (dy > 10.0 || dy < -10.0) e.chasingY = e.previousChasingY = e.y;
        e.chasingX += dx * 0.25;
        e.chasingZ += dz * 0.25;
        e.chasingY += dy * 0.25;
        interpolate(entity, e);
        dx = e.x - e.previousX;
        dz = e.z - e.previousZ;
        float bodyYaw = e.renderYawOffset;
        if (dx * dx + dz * dz > 0.0025000002) {
            bodyYaw = (float) ((float) (MathHelper.atan2(dz, dx) * 180.0) / 3.1415927) - 90.0F;
        }
        if (((EntityLivingBase) entity).swingProgress > 0.0F) bodyYaw = e.yaw;
        e.renderYawOffset += MathHelper.wrapAngleTo180_float(bodyYaw - e.renderYawOffset) * 0.3F;
        float difference = MathHelper.wrapAngleTo180_float(e.yaw - e.renderYawOffset);
        difference = MathHelper.clamp_float(difference, -75.0F, 75.0F);
        e.renderYawOffset = e.yaw - difference;
        if (difference * difference > 2500.0F) e.renderYawOffset += difference * 0.2F;
        while (e.yaw - e.previousYaw < -180.0F) e.previousYaw -= 360.0F;
        while (e.yaw - e.previousYaw >= 180.0F) e.previousYaw += 360.0F;
        while (e.renderYawOffset - e.previousRenderYawOffset < -180.0F) e.previousRenderYawOffset -= 360.0F;
        while (e.renderYawOffset - e.previousRenderYawOffset >= 180.0F) e.previousRenderYawOffset += 360.0F;
        while (e.pitch - e.previousPitch < -180.0F) e.previousPitch -= 360.0F;
        while (e.pitch - e.previousPitch >= 180.0F) e.previousPitch += 360.0F;
        while (e.headYaw - e.previousHeadYaw < -180.0F) e.previousHeadYaw -= 360.0F;
        while (e.headYaw - e.previousHeadYaw >= 180.0F) e.previousHeadYaw += 360.0F;
    }

    @EventHandler
    public void onRenderPosition(CapePositionEvent event) {
        Entity entity = event.player;
        ServerPositionEntry e = findTrackedPosition(entity);
        if (e == null) return;
        event.prevChasingPosX = e.previousChasingX;
        event.prevChasingPosY = e.previousChasingY;
        event.prevChasingPosZ = e.previousChasingZ;
        event.chasingPosX = e.chasingX;
        event.chasingPosY = e.chasingY;
        event.chasingPosZ = e.chasingZ;
        event.prevPosX = e.previousX;
        event.prevPosY = e.previousY;
        event.prevPosZ = e.previousZ;
        event.posX = e.x;
        event.posY = e.y;
        event.posZ = e.z;
        event.prevRenderYawOffset = e.previousRenderYawOffset;
        event.renderYawOffset = e.renderYawOffset;
    }

    public ServerPositionEntry findTrackedPosition(Entity entity) {
        Objects.requireNonNull(Adjust.entityTrackerService);
        boolean suppressed = BackTrackModule.INSTANCE.target != null;
        if (!suppressed && !trackingActive) resyncPositions();
        trackingActive = !suppressed;
        if (suppressed || entity.ridingEntity != null) return null;
        ServerPositionEntry entry = get(entity.getEntityId());
        if (entry == null || entry.entity == entity) return entry;
        synchronized (entries) {
            if (get(entity.getEntityId()) == entry) entries.remove(entity.getEntityId());
        }
        return null;
    }

    public void interpolate(Entity entity, ServerPositionEntry e) {
        if (e.interpolationSteps > 0) {
            double steps = e.interpolationSteps;
            double x = e.x + (e.targetX - e.x) / steps;
            double y = e.y + (e.targetY - e.y) / steps;
            double z = e.z + (e.targetZ - e.z) / steps;
            double yawDifference = e.targetYaw - e.yaw;
            while (yawDifference < -180.0) yawDifference += 360.0;
            while (yawDifference >= 180.0) yawDifference -= 360.0;
            e.yaw = (float) (e.yaw + yawDifference / steps);
            e.pitch = (float) (e.pitch + (e.targetPitch - e.pitch) / steps);
            e.interpolationSteps--;
            e.x = x; e.y = y; e.z = z;
            float halfWidth = e.entity.width / 2.0F;
            e.boundingBox = new AxisAlignedBB(x - halfWidth, y, z - halfWidth,
                    x + halfWidth, y + e.entity.height, z + halfWidth);
            e.yaw %= 360.0F;
            e.pitch %= 360.0F;
        }
        if (TweaksModule.INSTANCE.smoothRotationYaw.module.isEnabled()
                && TweaksModule.INSTANCE.smoothRotationYaw.enabledSetting()) e.headYaw = e.yaw;
    }

    public void resyncPositions() {
        synchronized (entries) {
            for (ServerPositionEntry e : entries.values()) {
                EntityOtherPlayerMP entity = (EntityOtherPlayerMP) e.entity;
                e.previousX = entity.lastTickPosX; e.previousY = entity.lastTickPosY; e.previousZ = entity.lastTickPosZ;
                e.x = entity.posX; e.y = entity.posY; e.z = entity.posZ;
                float halfWidth = entity.width / 2.0F;
                e.boundingBox = new AxisAlignedBB(e.x - halfWidth, e.y, e.z - halfWidth,
                        e.x + halfWidth, e.y + entity.height, e.z + halfWidth);
                e.previousYaw = entity.prevRotationYaw; e.yaw = entity.rotationYaw;
                e.previousPitch = entity.prevRotationPitch; e.pitch = entity.rotationPitch;
                e.previousRenderYawOffset = entity.prevRenderYawOffset; e.renderYawOffset = entity.renderYawOffset;
                e.previousChasingX = entity.prevChasingPosX; e.previousChasingY = entity.prevChasingPosY; e.previousChasingZ = entity.prevChasingPosZ;
                e.chasingX = entity.chasingPosX; e.chasingY = entity.chasingPosY; e.chasingZ = entity.chasingPosZ;
                e.targetX = e.serverX / 32.0; e.targetY = e.serverY / 32.0; e.targetZ = e.serverZ / 32.0;
                e.interpolationSteps = 3;
            }
        }
    }

    public ServerPositionEntry getOrCreate(EntityOtherPlayerMP entity) {
        synchronized (entries) {
            ServerPositionEntry e = get(entity.getEntityId());
            if (e != null && e.entity == entity) return e;
            e = new ServerPositionEntry();
            e.entity = entity;
            e.boundingBox = entity.getEntityBoundingBox();
            e.yaw = entity.rotationYaw;
            e.x = entity.posX; e.y = entity.posY; e.z = entity.posZ;
            e.previousX = entity.lastTickPosX; e.previousY = entity.lastTickPosY; e.previousZ = entity.lastTickPosZ;
            e.targetX = entity.otherPlayerMPX; e.targetY = entity.otherPlayerMPY; e.targetZ = entity.otherPlayerMPZ;
            e.targetYaw = entity.otherPlayerMPYaw; e.targetPitch = entity.otherPlayerMPPitch;
            e.previousChasingX = entity.prevChasingPosX; e.previousChasingY = entity.prevChasingPosY; e.previousChasingZ = entity.prevChasingPosZ;
            e.chasingX = entity.chasingPosX; e.chasingY = entity.chasingPosY; e.chasingZ = entity.chasingPosZ;
            e.pitch = entity.rotationPitch;
            e.previousYaw = entity.prevRotationYaw; e.previousPitch = entity.prevRotationPitch;
            e.renderYawOffset = entity.renderYawOffset; e.previousRenderYawOffset = entity.prevRenderYawOffset;
            e.headYaw = entity.rotationYawHead; e.previousHeadYaw = entity.prevRotationYawHead;
            e.serverX = entity.serverPosX; e.serverY = entity.serverPosY; e.serverZ = entity.serverPosZ;
            e.interpolationSteps = entity.otherPlayerMPPosRotationIncrements;
            entries.put(entity.getEntityId(), e);
            return e;
        }
    }

    public boolean isTracked(int entityId) {
        Timer timer;
        synchronized (recentlyDestroyed) { timer = recentlyDestroyed.get(entityId); }
        return timer != null && !timer.hasElapsed(1500L);
    }

    public ServerPositionEntry get(int entityId) { return entries.get(entityId); }

    public void clearRecent(int entityId) {
        synchronized (recentlyDestroyed) { recentlyDestroyed.remove(entityId); }
    }

    @EventHandler
    public void onPacket(DirectionalPacketEvent event) {
        if (event.isCancelled() || event.outgoing) return;
        Packet<?> packet = event.packet;
        if (packet instanceof S0CPacketSpawnPlayer p) clearRecent(p.getEntityID());
        else if (packet instanceof S0EPacketSpawnObject p) clearRecent(p.getEntityID());
        else if (packet instanceof S2CPacketSpawnGlobalEntity p) clearRecent(p.func_149052_c());
        else if (packet instanceof S11PacketSpawnExperienceOrb p) clearRecent(p.getEntityID());
        else if (packet instanceof S0FPacketSpawnMob p) clearRecent(p.getEntityID());
        else if (packet instanceof S10PacketSpawnPainting p) clearRecent(p.getEntityID());
        Objects.requireNonNull(packet);
        Minecraft mc = Minecraft.getMinecraft();
        Runnable task = null;
        if (packet instanceof S14PacketEntity p) {
            task = () -> {
                if (mc.theWorld == null) return;
                Entity entity = p.getEntity(mc.theWorld);
                if (!(entity instanceof EntityOtherPlayerMP player)) return;
                synchronized (entries) {
                    ServerPositionEntry e = getOrCreate(player);
                    e.serverX += p.func_149062_c();
                    e.serverY += p.func_149061_d();
                    e.serverZ += p.func_149064_e();
                    e.targetX = e.serverX / 32.0;
                    e.targetY = e.serverY / 32.0;
                    e.targetZ = e.serverZ / 32.0;
                    e.targetYaw = p.func_149060_h() ? (float) (360 * p.func_149066_f()) / 256.0F : e.yaw;
                    e.targetPitch = p.func_149060_h() ? (float) (360 * p.func_149063_g()) / 256.0F : e.pitch;
                    e.interpolationSteps = 3;
                }
            };
        } else if (packet instanceof S18PacketEntityTeleport p) {
            task = () -> {
                if (mc.theWorld == null) return;
                Entity entity = mc.theWorld.getEntityByID(p.getEntityId());
                if (!(entity instanceof EntityOtherPlayerMP player)) return;
                synchronized (entries) {
                    ServerPositionEntry e = getOrCreate(player);
                    e.serverX = p.getX(); e.serverY = p.getY(); e.serverZ = p.getZ();
                    double x = e.serverX / 32.0, y = e.serverY / 32.0, z = e.serverZ / 32.0;
                    if (Math.abs(e.x - x) < 0.03125 && Math.abs(e.y - y) < 0.015625 && Math.abs(e.z - z) < 0.03125) {
                        e.targetX = e.x; e.targetY = e.y; e.targetZ = e.z;
                    } else {
                        e.targetX = x; e.targetY = y; e.targetZ = z;
                    }
                    e.targetYaw = (float) (360 * p.getYaw()) / 256.0F;
                    e.targetPitch = (float) (360 * p.getPitch()) / 256.0F;
                    e.interpolationSteps = 3;
                }
            };
        } else if (packet instanceof S19PacketEntityHeadLook p) {
            task = () -> {
                if (mc.theWorld == null) return;
                Entity entity = p.getEntity(mc.theWorld);
                if (!(entity instanceof EntityOtherPlayerMP player)) return;
                if (TweaksModule.INSTANCE.smoothRotationYaw.module.isEnabled()
                        && TweaksModule.INSTANCE.smoothRotationYaw.enabledSetting()) return;
                synchronized (entries) {
                    getOrCreate(player).headYaw = (float) (360 * p.getYaw()) / 256.0F;
                }
            };
        } else if (packet instanceof S13PacketDestroyEntities p) {
            task = () -> {
                synchronized (recentlyDestroyed) {
                    for (int id : p.getEntityIDs()) {
                        synchronized (recentlyDestroyed) {
                            recentlyDestroyed.int2ObjectEntrySet().removeIf(
                                    entry -> entry.getValue().hasElapsed(1500L));
                            recentlyDestroyed.put(id, new Timer());
                        }
                    }
                }
                synchronized (entries) {
                    for (int id : p.getEntityIDs()) entries.remove(id);
                }
            };
        } else if (packet instanceof S0DPacketCollectItem p) {
            task = () -> {
                synchronized (entries) { entries.remove(p.getCollectedItemEntityID()); }
            };
        }
        if (task != null) {
            if (mc.isCallingFromMinecraftThread()) task.run();
            else mc.addScheduledTask(task);
        }
    }
}
