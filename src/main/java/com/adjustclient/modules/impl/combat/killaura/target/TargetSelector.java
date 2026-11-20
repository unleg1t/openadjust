package com.adjustclient.modules.impl.combat.killaura.target;

import com.adjustclient.Adjust;
import com.adjustclient.modules.impl.combat.killaura.*;
import com.adjustclient.utilities.*;
import com.adjustclient.utilities.anticheat.ServerTracker;
import com.adjustclient.utilities.anticheat.entity.*;
import com.adjustclient.utilities.rotation.AimPoint;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Vec3;

public final class TargetSelector implements MinecraftInstance {
    public final KillAuraModule module;
    public final Int2ObjectOpenHashMap<TargetInfo> targets = new Int2ObjectOpenHashMap<>();
    public TargetInfo attackTarget;
    public TargetInfo rotationTarget;
    public TargetInfo previousTarget;
    public TargetInfo previousPreviousTarget;
    public TargetInfo oldestTarget;
    public TargetInfo lastAttacked;
    public TargetInfo previousRotationTarget;
    public final List<SwitchTarget> switchTargets = new ArrayList<>();
    public double nearestDistance = Double.MAX_VALUE;
    public TargetSelector(KillAuraModule module) { this.module = module; }

    public void recordAttack(TargetInfo target) {
        target.attackTimer.reset();
        lastAttacked = target;
        if (module.settings.mode.getValue() == KillAuraMode.SWITCH) getSwitchTarget(target).attacked = true;
    }
    public void update(List<Float> entities) {
        previousRotationTarget = rotationTarget;
        attackTarget = null;
        rotationTarget = null;
        nearestDistance = Double.MAX_VALUE;
        Vec3 eyes = mc.thePlayer.getPositionEyes(1.0F);
        TargetInfo target = chooseTarget(entities, eyes, false);
        if (target != null) { attackTarget = target; rotationTarget = target; return; }
        attackTarget = null;
        rotationTarget = chooseTarget(entities, eyes, true);
    }
    public TargetInfo chooseSwitchTarget(List<TargetInfo> candidates) {
        long delay = module.settings.getSwitchDelay();
        candidates.sort(Comparator.comparingDouble(target -> {
            Vec2f rotation = RotationUtil.getRotations(target.entity.getEntity());
            return Math.abs(net.minecraft.util.MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - rotation.x));
        }));
        for (TargetInfo candidate : candidates) {
            SwitchTarget entry = getSwitchTarget(candidate);
            if (delay > 50 && !entry.timer.hasElapsed(delay / (candidates.size() < 3 ? 2 : 1))) return candidate;
            if (!entry.attacked) return candidate;
        }
        SwitchTarget previous = previousPreviousTarget == null ? null : getSwitchTarget(previousPreviousTarget);
        switchTargets.clear();
        if (candidates.size() >= 3 && previous != null) switchTargets.add(previous);
        return chooseSwitchTarget(candidates);
    }
    public List<TrackedLivingEntity> collectTargets(Vec3 eyes, boolean includeRotationRange) {
        KillAuraSettings settings = module.settings;
        int fov = settings.rangeFieldOfView.getValue().intValue();
        int mask = settings.getTargetMask() | 0x400;
        List<TrackedLivingEntity> candidates = ServerTracker.INSTANCE.entityTracker.getEntities(mask, TrackedLivingEntity.class);
        Iterator<TrackedLivingEntity> iterator = candidates.iterator();
        while (iterator.hasNext()) {
            TrackedLivingEntity target = iterator.next();
            EntityLivingBase entity = target.getEntity();
            if (entity.deathTime >= 1 || entity.getHealth() <= 0.0F || target.removed || entity.ticksExisted < 3
                    || target.isRecentlyTracked()) {
                iterator.remove();
                continue;
            }
            int effectiveFov = fov;
            if (settings.rotationsVisibility.getValue() != RotationVisibility.SILENT && settings.rotationsIncreaseFov.isEnabled()
                    && previousRotationTarget != null && previousRotationTarget.entity.entityId == target.entityId) effectiveFov = 180;
            if (RotationUtil.isInFov(effectiveFov, entity)) { iterator.remove(); continue; }
            double distance = DistanceUtil.distanceToEntity(eyes, entity);
            if (nearestDistance > distance) nearestDistance = distance;
            double range = settings.getAttackRange(entity);
            if (includeRotationRange) range = Math.max(range, settings.getRotateRange());
            if (distance > range + 0.15D) iterator.remove();
        }
        return candidates;
    }
    public TargetInfo chooseTarget(List<Float> yawSamples, Vec3 eyes, boolean rotationOnly) {
        List<TrackedLivingEntity> entities = collectTargets(eyes, false);
        if (entities.isEmpty()) entities = collectTargets(eyes, true);
        if (entities.isEmpty()) return null;
        if (entities.size() != 1) {
            entities.sort(Comparator.comparingDouble(target -> {
                Vec2f rotation = RotationUtil.getYawBetween(target.getEntity().getPositionVector(), eyes);
                return Math.abs(net.minecraft.util.MathHelper.wrapAngleTo180_float(rotation.x - yawSamples.getLast()));
            }));
        }
        entities.sort(Comparator.comparingDouble(target -> score(target)));
        if (entities.isEmpty()) return null;
        KillAuraSettings settings = module.settings;
        if (settings.mode.getValue() == KillAuraMode.SINGLE) filterSingleTargets(entities);
        List<TargetInfo> candidates = new ArrayList<>();
        for (TrackedLivingEntity entity : entities) {
            double range = rotationOnly ? settings.getRotateRange() : settings.getAttackRange(entity.getEntity());
            TargetInfo target = selectAimPoint(entity, eyes, range);
            if (target != null) candidates.add(target);
        }
        if (candidates.isEmpty()) return null;
        if (candidates.size() >= 2 && settings.mode.getValue() == KillAuraMode.SWITCH) return chooseSwitchTarget(candidates);
        return candidates.get(0);
    }
    public SwitchTarget getSwitchTarget(TargetInfo target) {
        for (SwitchTarget entry : switchTargets) if (entry.entityId == target.entity.entityId) return entry;
        SwitchTarget entry = new SwitchTarget(target.entity.entityId);
        switchTargets.add(entry);
        return entry;
    }
    public static void filterSingleTargets(List<TrackedLivingEntity> entities) {
        TrackedLivingEntity first = entities.get(0);
        entities.removeIf(target -> isOutsideSingleFov(target));
        if (entities.isEmpty()) entities.add(first);
    }
    public static boolean isOutsideSingleFov(TrackedLivingEntity target) {
        Vec2f rotation = RotationUtil.getRotations(target.getEntity());
        return Math.abs(net.minecraft.util.MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - rotation.x)) > 34.5F;
    }
    public double score(TrackedLivingEntity target) {
        var priority = module.settings.priority.getValue();
        double value = priority.score(target);
        if (priority.usesSecondaryPriority()) {
            for (var option : module.settings.priority.getOptions())
                if (option != priority && option.usesSecondaryPriority()) value += option.score(target) * 0.001D;
        }
        if (!(target instanceof TrackedFakePlayer)) return value * 2.5D;
        if (Adjust.INSTANCE.tagManager.getTag(target.getEntity().getName()) != com.adjustclient.core.tag.PlayerTag.WHITELIST) return value;
        return value * (target.combatStats.canKillWithHeldItem(0) ? 0.4D : 0.5D);
    }
    public TargetInfo selectAimPoint(TrackedLivingEntity target, Vec3 eyes, double range) {
        KillAuraSettings settings = module.settings;
        AimPoint aim = switch (settings.rotationsVisibility.getValue()) {
            case SILENT -> RotationUtil.getAimPoint(target.getEntity(), eyes,
                    new Vec2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch),
                    settings.rangeThroughWalls.isEnabled(), settings.rangeThroughEntities.isEnabled(), range);
            case LOCK_ON, LINEAR -> RotationUtil.findEntityIntercept(target.getEntity(), eyes,
                    settings.rotationsMultipointPreferCenterMass.isEnabled(), settings.rangeThroughWalls.isEnabled(),
                    settings.rangeThroughEntities.isEnabled(), range,
                    settings.getMultipointHorizontal() / 100.0D, settings.getMultipointVertical() / 100.0D);
        };
        if (aim == null) return null;
        TargetInfo info = targets.get(target.entityId);
        if (info == null || info.entity != target) { info = new TargetInfo(target); targets.put(target.entityId, info); }
        info.aimPoint = aim;
        return info;
    }
}
