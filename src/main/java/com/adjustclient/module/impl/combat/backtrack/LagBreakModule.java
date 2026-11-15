package com.adjustclient.module.impl.combat.backtrack;

import com.adjustclient.Adjust;
import com.adjustclient.event.*;
import com.adjustclient.game.packet.PacketHold;
import com.adjustclient.game.render.progress.ProgressBar;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.module.impl.player.breaker.BreakerModule;
import com.adjustclient.util.*;
import com.adjustclient.util.Timer;
import com.adjustclient.util.anticheat.ServerTracker;
import com.adjustclient.util.anticheat.entity.*;
import net.minecraft.util.*;
import org.apache.commons.lang3.tuple.Pair;
import java.util.Collections;
import java.util.List;

public final class LagBreakModule extends Module {
    public static LagBreakModule INSTANCE;
    public final LagBreakSettings settings;
    public TrackedLivingEntity target;
    private final Timer timer = new Timer();
    private PacketHold hold;
    private boolean armed;
    private boolean attacked;
    public boolean hurtAtStart;

    public LagBreakModule() {
        super("Lag Break", Category.COMBAT);
        INSTANCE = this;
        settings = new LagBreakSettings(this);
    }

    private void updateActiveTarget() {
        if (target.stale || timer.hasElapsed(settings.getMaximumTime())
                || Adjust.entityTrackerService.tracker.isTracked(target.entityId)) { reset(); return; }
        double movement = Math.hypot(mc.thePlayer.posX - mc.thePlayer.prevPosX, mc.thePlayer.posZ - mc.thePlayer.prevPosZ);
        if (movement <= 0.015) {
            if (hold != null) { Adjust.outboundPacketQueue.removeHold(hold); hold = null; }
            if (target != null) { attacked = false; target = null; }
            return;
        }
        if (attacked && target.combatStats.canKillWithHeldItem(0)) { reset(); return; }
        var entity = target.getEntity();
        float border = entity.getCollisionBorderSize();
        AxisAlignedBB targetBox = entity.getEntityBoundingBox().expand(border, border, border);
        Vec3 eyes = mc.thePlayer.getPositionEyes(1.0F);
        if (eyes.distanceTo(DistanceUtil.closestPoint(eyes, targetBox)) > 3.0) return;
        if (!settings.breakInRangeEnabled.isEnabled()) { reset(); return; }
        Vec3 serverPosition = ServerTracker.INSTANCE.position;
        if (serverPosition == null) return;
        if (canTargetHit(target, WorldUtil.playerBoundsAt(serverPosition, 1.8F))
                && !canTargetHit(target, mc.thePlayer.getEntityBoundingBox())) reset();
    }

    private void findTarget() {
        double dx = mc.thePlayer.posX - mc.thePlayer.prevPosX;
        double dz = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
        if (Math.hypot(dx, dz) <= 0.015 || !MovementUtil.isMoving()) return;
        float movementYaw = (float) (Math.atan2(-dx, dz) * 57.29577951308232);
        List<TrackedLivingEntity> candidates = ServerTracker.INSTANCE.entityTracker
                .getEntities(settings.getTargetFlags(), TrackedLivingEntity.class);
        Vec3 position = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        if (armed || settings.breakInRangeEnabled.isEnabled()) {
            for (TrackedLivingEntity candidate : candidates) {
                var entity = candidate.getEntity();
                double vertical = Math.abs(position.yCoord - entity.posY);
                if (vertical > 4.0 || candidate.isTagged()) continue;
                double x = position.xCoord - entity.posX, z = position.zCoord - entity.posZ;
                double horizontal = Math.sqrt(x * x + z * z);
                double metric = horizontal + vertical / 2.0;
                settings.getTimeoutRange();
                if (!armed || metric > getRequirementRange(candidate)) continue;
                float targetYaw = RotationUtil.getRotations(entity).x;
                if (Math.abs(MathHelper.wrapAngleTo180_float(movementYaw - targetYaw)) > 90.0F) continue;
                if (settings.getMaximumTargets() >= 1) { startTarget(candidate); armed = false; }
                break;
            }
        }
        armWhenClear(candidates);
    }

    @EventHandler public void onAttack(AttackEntityEvent event) {
        if (!isEnabled()) return;
        var tracker = ServerTracker.INSTANCE.entityTracker;
        var entity = event.entity;
        TrackedLivingEntity tracked = tracker.get(entity.getEntityId(), TrackedLivingEntity.class);
        if (tracked == null) return;
        // getHitsToKill(1) supplies the observed 450ms attack / (400ms - latency) hurt window;
        // canKillWithHeldItem preserves the subsequent spoof-aware held-item damage comparison.
        if (tracked.combatStats.canKillWithHeldItem(1)) { reset(); return; }
        target = tracked;
        attacked = true;
    }

    @EventHandler public void onMotion(MotionEvent event) {
        if (!isEnabled() || !event.pre) return;
        if (BreakerModule.INSTANCE.handler.target == null && GameStateUtil.isActiveUnlessScaffolding()) {
            if (target != null) updateActiveTarget();
            else findTarget();
        } else reset();
    }

    @EventHandler public void onRender2D(Render2DEvent event) {
        if (!isEnabled() || target == null || !settings.hud.isEnabled()) return;
        float progress = (float) (System.currentTimeMillis() - timer.lastMs) / settings.getMaximumTime();
        Adjust.progressBarRenderer.bars.add(new ProgressBar(MathHelper.clamp_float(progress, 0.0F, 1.0F)));
    }

    private float getRequirementRange(TrackedLivingEntity tracked) {
        var entity = tracked.getEntity();
        double dx = entity.posX - entity.prevPosX;
        double dz = entity.posZ - entity.prevPosZ;
        double speed = Math.hypot(dx, dz);
        float movementYaw = (float) (Math.atan2(-dx, dz) * 57.29577951308232);
        float towardPlayer = (float) (Math.atan2(mc.thePlayer.posZ - entity.posZ,
                mc.thePlayer.posX - entity.posX) * 180.0 / Math.PI - 90.0);
        float difference = Math.abs(MathHelper.wrapAngleTo180_float(movementYaw - towardPlayer));
        float angleFactor = difference / 55.0F;
        if (difference > 55.0F) angleFactor = 1.0F;
        double factor = angleFactor >= 15.0F ? angleFactor : 1.0;
        if (speed <= 0.155) factor *= speed / 0.155;
        return (float) (settings.rangeRequirement.getDouble() + factor * 0.95);
    }

    private void armWhenClear(List<TrackedLivingEntity> candidates) {
        Vec3 eyes = mc.thePlayer.getPositionEyes(1.0F);
        for (TrackedLivingEntity candidate : candidates) {
            var entity = candidate.getEntity();
            float border = entity.getCollisionBorderSize();
            AxisAlignedBB box = entity.getEntityBoundingBox().expand(border, border, border);
            Vec3 closest = DistanceUtil.closestPoint(eyes, box);
            double distance = eyes.distanceTo(closest);
            if (settings.getTimeoutRange() >= distance) return;
        }
        armed = true;
    }

    private boolean canTargetHit(TrackedLivingEntity tracked, AxisAlignedBB playerBox) {
        var entity = tracked.getEntity();
        MovingObjectPosition hit = WorldUtil.pickFromBounds(entity, false, true,
                Collections.singletonList(Pair.of(mc.thePlayer, playerBox)), entity.rotationYaw, entity.rotationPitch,
                3.5, 4.5, 0.2D);
        return hit != null && hit.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY;
    }

    private void startTarget(TrackedLivingEntity tracked) {
        if (hold != null) { Adjust.outboundPacketQueue.removeHold(hold); hold = null; }
        if (target != null) { attacked = false; target = null; }
        hold = Adjust.outboundPacketQueue.addHold(null, false);
        hurtAtStart = mc.thePlayer.isUsingItem();
        target = tracked;
        timer.reset();
    }

    @SuppressWarnings("unused")
    private static void vendorReportBypass() {
        com.adjustclient.service.util.ServiceUtil.reportFeatureUse(
                com.adjustclient.service.telemetry.BypassFeature.LAGBREAK);
    }

    @Override public void onDisable() { reset(); }
    @Override public String getDescription() {
        return "Simulates outbound network lag in combat to worsen your opponents aim accuracy.";
    }

    public void advanceHold() {
        if (hold == null) return;
        Adjust.outboundPacketQueue.removeHold(hold);
        hold = Adjust.outboundPacketQueue.addHold(null, false);
    }

    public void reset() {
        if (hold != null) { Adjust.outboundPacketQueue.removeHold(hold); hold = null; }
        if (target != null) { attacked = false; target = null; }
    }
}
