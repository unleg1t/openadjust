package com.adjustclient.module.impl.combat.backtrack;

import com.adjustclient.Adjust;
import com.adjustclient.event.*;
import com.adjustclient.game.packet.PacketHold;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.render.RenderUtil;
import com.adjustclient.util.*;
import com.adjustclient.util.anticheat.ServerTracker;
import com.adjustclient.util.anticheat.entity.TrackedLivingEntity;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.network.play.server.*;
import net.minecraft.util.*;
import java.awt.Color;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public final class BackTrackModule extends Module {
    public static BackTrackModule INSTANCE;
    public final BackTrackSettings settings;
    public TrackedLivingEntity target;
    private final List<DelayedPacket> delayed = Collections.synchronizedList(new LinkedList<>());

    public BackTrackModule() {
        super("Back Track", Category.COMBAT);
        INSTANCE = this;
        settings = new BackTrackSettings(this);
    }

    @EventHandler public void onTick(PreClientTickEvent event) {
        if (!isEnabled()) return;
        if (!GameStateUtil.isActiveUnlessScaffolding()
                || (target != null && (target.stale || Adjust.entityTrackerService.tracker.isTracked(target.entityId)))) {
            flushAll();
            return;
        }
        releaseExpired();
    }

    @EventHandler public void onRender3D(Render3DEvent event) {
        if (!isEnabled() || target == null || !settings.renderRealPosition.isEnabled()) return;
        var entry = Adjust.entityTrackerService.tracker.get(target.entityId);
        if (entry == null) return;
        var manager = mc.getRenderManager();
        var entity = target.getEntity();
        AxisAlignedBB original = entity.getEntityBoundingBox();
        double x = entry.previousX + (entry.x - entry.previousX) * event.partialTicks - manager.viewerPosX;
        double y = entry.previousY + (entry.y - entry.previousY) * event.partialTicks - manager.viewerPosY;
        double z = entry.previousZ + (entry.z - entry.previousZ) * event.partialTicks - manager.viewerPosZ;
        double halfX = (original.maxX - original.minX) * 0.5;
        double halfZ = (original.maxZ - original.minZ) * 0.5;
        AxisAlignedBB box = new AxisAlignedBB(x - halfX - 0.1000000014901161, y,
                z - halfZ - 0.1000000014901161, x + halfX + 0.1000000014901161,
                y + original.maxY - original.minY + (entity.isSneaking() ? -0.25 : 0.0) + 0.1000000014901161,
                z + halfZ + 0.1000000014901161);
        Color color = settings.renderColor.getValue();
        RenderUtil.begin3d();
        GlStateManager.color(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, 0.50196081F);
        RenderUtil.drawFilledBox(box);
        RenderUtil.end3d();
    }

    @Override public void onDisable() { flushAll(); }
    @Override public String getDescription() {
        return "Lets you hit players at their previous positions by simulating inbound network lag.";
    }
    private void releaseExpired() {
        synchronized (delayed) {
            if (delayed.isEmpty()) return;
            Iterator<DelayedPacket> iterator = delayed.iterator();
            while (iterator.hasNext()) {
                DelayedPacket packet = iterator.next();
                if (packet.timer.hasElapsed(settings.maximumTime.getLong())) {
                    Adjust.inboundPacketQueue.removeHold(packet.hold);
                    iterator.remove();
                }
            }
        }
    }

    @EventHandler public void onPacket(DirectionalPacketEvent event) {
        if (event.isCancelled() || !isEnabled() || event.outgoing) return;
        var packet = event.packet;
        if (target != null) {
            if (packet instanceof S14PacketEntity movement) {
                if (movement.entityId != target.entityId) return;
                var entry = Adjust.entityTrackerService.tracker.get(target.entityId);
                if (entry == null) return;
                updateTargetPosition(new Vec3((entry.serverX + movement.func_149062_c()) / 32.0,
                        (entry.serverY + movement.func_149061_d()) / 32.0,
                        (entry.serverZ + movement.func_149064_e()) / 32.0));
            } else if (packet instanceof S08PacketPlayerPosLook) {
                flushAll();
            } else if (packet instanceof S18PacketEntityTeleport teleport) {
                if (teleport.getEntityId() == target.entityId)
                    updateTargetPosition(new Vec3(teleport.getX() / 32.0, teleport.getY() / 32.0, teleport.getZ() / 32.0));
            } else if (packet instanceof S01PacketJoinGame) {
                flushAll();
            } else if (packet instanceof S13PacketDestroyEntities destroy) {
                for (int id : destroy.getEntityIDs()) { if (id == target.entityId) { flushAll(); break; } }
            } else if (settings.disableOnKnockback.isEnabled() && packet instanceof S12PacketEntityVelocity velocity
                    && velocity.getEntityID() == mc.thePlayer.getEntityId()) {
                flushAll();
            }
            return;
        }
        var tracker = ServerTracker.INSTANCE.entityTracker;
        if (tracker == null) return;
        if (packet instanceof S14PacketEntity movement) {
            TrackedLivingEntity tracked = tracker.get(movement.entityId, TrackedLivingEntity.class);
            if (tracked == null) return;
            var entity = tracked.getEntity();
            tryStart(tracked, new Vec3((entity.serverPosX + movement.func_149062_c()) / 32.0,
                    (entity.serverPosY + movement.func_149061_d()) / 32.0,
                    (entity.serverPosZ + movement.func_149064_e()) / 32.0));
        } else if (packet instanceof S18PacketEntityTeleport teleport) {
            TrackedLivingEntity tracked = tracker.get(teleport.getEntityId(), TrackedLivingEntity.class);
            if (tracked != null) tryStart(tracked, new Vec3(teleport.getX() / 32.0, teleport.getY() / 32.0, teleport.getZ() / 32.0));
        }
    }

    private boolean hasOtherTargetInRange(TrackedLivingEntity excluded) {
        var tracker = ServerTracker.INSTANCE.entityTracker;
        if (tracker == null) return false;
        List<TrackedLivingEntity> entities = tracker.getEntities(settings.getTargetFlags(), TrackedLivingEntity.class);
        float eyeHeight = mc.thePlayer.isPlayerSleeping() ? 0.2F : 1.62F;
        if (!mc.thePlayer.isPlayerSleeping() && mc.thePlayer.movementInput != null && mc.thePlayer.movementInput.sneak) eyeHeight -= 0.08F;
        Vec3 eyes = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + eyeHeight, mc.thePlayer.posZ);
        for (TrackedLivingEntity entity : entities) {
            if (entity == excluded) continue;
            if (settings.getTimeoutRange() >= DistanceUtil.distanceToEntity(eyes, entity.getEntity())) return true;
        }
        return false;
    }

    private void addPosition(AxisAlignedBB box) {
        PacketHold hold = Adjust.inboundPacketQueue.addHold(null, false);
        delayed.add(new DelayedPacket(hold, box));
    }

    private void updateTargetPosition(Vec3 position) {
        if (target.stale || Adjust.entityTrackerService.tracker.isTracked(target.entityId)) { flushAll(); return; }
        float border = target.getEntity().getCollisionBorderSize();
        AxisAlignedBB box = WorldUtil.playerBoundsAt(position, 1.8F).expand(border, border, border);
        float eyeHeight = mc.thePlayer.isPlayerSleeping() ? 0.2F : 1.62F;
        if (!mc.thePlayer.isPlayerSleeping() && mc.thePlayer.movementInput != null && mc.thePlayer.movementInput.sneak) eyeHeight -= 0.08F;
        Vec3 eyes = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + eyeHeight, mc.thePlayer.posZ);
        double distance = eyes.distanceTo(DistanceUtil.closestPoint(eyes, box));
        if (settings.getTimeoutRange() >= distance || distance > settings.rangeMaximum.getDouble()) { flushAll(); return; }
        synchronized (delayed) {
            if (!flushIfTargetOutOfReach()) addPosition(box);
            releaseOutOfRange();
        }
    }

    private void startTarget(TrackedLivingEntity tracked, AxisAlignedBB box) {
        synchronized (delayed) {
            while (!delayed.isEmpty()) {
                DelayedPacket previous = delayed.removeFirst();
                Adjust.inboundPacketQueue.removeHold(previous.hold);
            }
        }
        target = null;
        PacketHold hold = Adjust.inboundPacketQueue.addHold(null, false);
        delayed.add(new DelayedPacket(hold, box));
        target = tracked;
    }

    private void tryStart(TrackedLivingEntity tracked, Vec3 nextPosition) {
        if (!GameStateUtil.isActiveUnlessScaffolding() || tracked.stale || tracked.isRecentlyTracked()) return;
        var server = ServerTracker.INSTANCE;
        if (server.attackedLivingEntity == null || server.attackedLivingEntity.getEntityId() != tracked.entityId
                || server.ticksSinceAttack >= 21) return;
        int flags = settings.getTargetFlags();
        if (!tracked.matches(flags) || !tracked.matchesType(flags)) return;
        var entity = tracked.getEntity();
        float eyeHeight = mc.thePlayer.isPlayerSleeping() ? 0.2F : 1.62F;
        if (!mc.thePlayer.isPlayerSleeping() && mc.thePlayer.movementInput != null && mc.thePlayer.movementInput.sneak) eyeHeight -= 0.08F;
        Vec3 eyes = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + eyeHeight, mc.thePlayer.posZ);
        if (DistanceUtil.distanceToEntity(eyes, entity) > 3.0F) return;
        float border = entity.getCollisionBorderSize();
        AxisAlignedBB box = WorldUtil.playerBoundsAt(nextPosition, 1.8F).expand(border, border, border);
        double distance = eyes.distanceTo(DistanceUtil.closestPoint(eyes, box));
        if (distance > settings.getTimeoutRange() && !hasOtherTargetInRange(tracked)) startTarget(tracked, box);
    }

    private boolean flushIfTargetOutOfReach() {
        if (DistanceUtil.distanceToEntity(mc.thePlayer.getPositionEyes(1.0F), target.getEntity()) > 3.0F) {
            flushAll();
            return true;
        }
        return false;
    }

    private void releaseOutOfRange() {
        synchronized (delayed) {
            if (delayed.isEmpty()) return;
            int index = delayed.size() - 1;
            for (; index >= 0; --index) {
                DelayedPacket packet = delayed.get(index);
                float eyeHeight = mc.thePlayer.isPlayerSleeping() ? 0.2F : 1.62F;
        if (!mc.thePlayer.isPlayerSleeping() && mc.thePlayer.movementInput != null && mc.thePlayer.movementInput.sneak) eyeHeight -= 0.08F;
        Vec3 eyes = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + eyeHeight, mc.thePlayer.posZ);
                double distance = eyes.distanceTo(DistanceUtil.closestPoint(eyes, packet.box));
                if (settings.getTimeoutRange() >= distance) break;
            }
            for (; index >= 0; --index) {
                DelayedPacket packet = delayed.remove(index);
                Adjust.inboundPacketQueue.removeHold(packet.hold);
            }
        }
    }

    public void flushAll() {
        synchronized (delayed) {
            while (!delayed.isEmpty()) {
                DelayedPacket packet = delayed.removeFirst();
                Adjust.inboundPacketQueue.removeHold(packet.hold);
            }
        }
        target = null;
    }
}
