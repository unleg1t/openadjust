package com.adjustclient.utilities;

import com.adjustclient.utilities.rotation.AimPoint;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import org.apache.commons.lang3.tuple.Pair;
import com.adjustclient.utilities.hypixel.HypixelPlacementLimiter;
import com.adjustclient.modules.impl.movement.MovementFixModule;
import com.adjustclient.modules.impl.combat.builder.BuilderPlacer;

public final class RotationUtil {
    public static Entity ignoredRaytraceEntity;
    private static final List<Vec2f> entityInterceptRotations = new ArrayList<>();
    private static final List<Vec3> entityInterceptPoints = new ArrayList<>();
    public static float getClosestYaw(float targetYaw) {
        float yaw = Minecraft.getMinecraft().thePlayer.rotationYaw;
        List<Float> candidates = new ArrayList<>();
        for (float candidate : new float[] {yaw, yaw + 180.0F, yaw + 45.0F,
                yaw - 45.0F, yaw + 135.0F, yaw - 135.0F}) candidates.add(candidate);
        candidates.sort(Comparator.comparingDouble(candidate -> Math.abs(MathHelper.wrapAngleTo180_float(candidate - targetYaw))));
        return candidates.get(0);
    }

    public static Vec2f getRotations(Entity target) {
        return getRotations(new Vec3(target.posX, target.posY + target.getEyeHeight(), target.posZ));
    }
    public static Vec2f getRotations(Vec3 target) {
        Entity player = Minecraft.getMinecraft().thePlayer;
        net.minecraft.client.entity.EntityPlayerSP local = Minecraft.getMinecraft().thePlayer;
        float eyeHeight = local.isPlayerSleeping() ? 0.2F : 1.62F;
        if (!local.isPlayerSleeping() && local.movementInput != null && local.movementInput.sneak) eyeHeight -= 0.08F;
        Vec2f angles = getYawBetween(target, new Vec3(player.posX, player.posY + eyeHeight, player.posZ));
        return new Vec2f(player.rotationYaw + MathHelper.wrapAngleTo180_float(angles.x - player.rotationYaw), angles.y);
    }
    public static boolean isInFov(int fov, Entity target) {
        if (fov >= 180) return false;
        Minecraft mc = Minecraft.getMinecraft();
        Vec2f current = com.adjustclient.core.rotation.handler.RenderRotationTracker.INSTANCE.rotation;
        if (current == null) current = new Vec2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
        double best = Double.MAX_VALUE;
        double eyeHeight = target.getEyeHeight();
        for (double height : new double[] {0.0D, eyeHeight, eyeHeight / 2.0D}) {
            Vec2f rotation = getRotations(new Vec3(target.posX, target.posY + height, target.posZ));
            double difference = angularDistance(current, rotation);
            if (best > difference) best = difference;
        }
        return best > fov;
    }
    public static boolean isInteractable(BlockPos pos) {
        net.minecraft.block.state.IBlockState state = Minecraft.getMinecraft().theWorld.getBlockState(pos);
        if (BuilderPlacer.placementRotationActive) return false;
        Block block = state.getBlock();
        if (!BlockUtil.isInteractable(block)) return false;
        net.minecraft.client.entity.EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        return player == null || player.movementInput == null || !player.movementInput.sneak || player.isPlayerSleeping();
    }
    public static boolean isPlacementTarget(MovingObjectPosition hit, BlockPos target) {
        return BlockUtil.getPlacePos(hit.getBlockPos(), hit.sideHit).equals(target);
    }
    public static float getRotationDistance(Vec2f rotation, float yaw, float pitch) {
        float yawDifference = MathHelper.wrapAngleTo180_float(rotation.x - yaw);
        float pitchDifference = rotation.y - pitch;
        return MathHelper.sqrt_float(yawDifference * yawDifference + pitchDifference * pitchDifference);
    }
    public static boolean isAllowedPlacementHit(PlacementFacingMode mode, BlockPos target, MovingObjectPosition hit) {
        if (hit.sideHit == EnumFacing.UP && mode == PlacementFacingMode.PREVENT_UP) return false;
        return BlockUtil.getPlacePos(hit.getBlockPos(), hit.sideHit).equals(target);
    }
    public static Pair<HitVec, MovingObjectPosition> findHitRotation(List<HitVec> candidates, Block block, BlockPos targetPos) {
        Minecraft mc = Minecraft.getMinecraft();
        double reach = mc.playerController.getBlockReachDistance();
        net.minecraft.client.entity.EntityPlayerSP player = mc.thePlayer;
        float eyeHeight = player.isPlayerSleeping() ? 0.2F : 1.62F;
        if (!player.isPlayerSleeping() && player.movementInput != null && player.movementInput.sneak) eyeHeight -= 0.08F;
        Vec3 eyes = new Vec3(player.posX, player.posY + eyeHeight, player.posZ);
        for (HitVec candidate : candidates) {
            Vec2f rotation = candidate.angles;
            Vec3 look = mc.thePlayer.getVectorForRotation(rotation.y, rotation.x);
            Vec3 end = eyes.addVector(look.xCoord * reach, look.yCoord * reach, look.zCoord * reach);
            MovingObjectPosition hit = block.collisionRayTrace(mc.theWorld, targetPos, eyes, end);
            if (hit != null && hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
                    && hit.getBlockPos().equals(targetPos)) return Pair.of(candidate, hit);
        }
        return null;
    }
    public static float getYawTo(float currentYaw, double fromX, double fromZ, double toX, double toZ) {
        float targetYaw = (float) (Math.atan2(toZ - fromZ, toX - fromX) * 180.0D / Math.PI) - 90.0F;
        return currentYaw + MathHelper.wrapAngleTo180_float(targetYaw - currentYaw);
    }
    public static Pair<HitVec, MovingObjectPosition> findFilteredHitRotation(List<HitVec> candidates, Entity entity, boolean preferNonUp, Predicate<MovingObjectPosition> filter, boolean useCandidateBlock) {
        double reach = Minecraft.getMinecraft().playerController.getBlockReachDistance();
        Pair<HitVec, MovingObjectPosition> fallback = null;
        for (HitVec candidate : candidates) {
            Vec2f rotation = candidate.angles;
            MovingObjectPosition hit;
            if (useCandidateBlock && candidate.blockPos != null) {
                hit = WorldUtil.rayTraceBlock(candidate.blockPos, entity, reach, rotation.x, rotation.y);
            } else {
                hit = WorldUtil.rayTrace(entity, reach, rotation.x, rotation.y);
            }
            if (hit == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) continue;
            if (isInteractable(hit.getBlockPos()) || !filter.test(hit)) continue;
            if (hit.sideHit != EnumFacing.UP || !preferNonUp) return Pair.of(candidate, hit);
            if (fallback == null) fallback = Pair.of(candidate, hit);
        }
        return fallback;
    }
    public static AimPoint getAimPoint(Entity target, Vec3 eyes, Vec2f current, boolean throughWalls, boolean ignoreOtherEntities, double range) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!mc.isCallingFromMinecraftThread()) throw new IllegalStateException("findEntityIntercept() called outside of game thread");
        // This solver deliberately uses player-sized bounds, even for non-player targets.
        AxisAlignedBB box = new AxisAlignedBB(target.posX - 0.300000011920929D, target.posY,
                target.posZ - 0.300000011920929D, target.posX + 0.300000011920929D,
                target.posY + 1.799999952316284D, target.posZ + 0.300000011920929D);
        Vec3 predicted = new Vec3(target.posX + MathHelper.clamp_double(target.posX - target.lastTickPosX, -0.25D, 0.25D) * 1.600000023841858D,
                target.posY, target.posZ + MathHelper.clamp_double(target.posZ - target.lastTickPosZ, -0.25D, 0.25D) * 1.600000023841858D);
        double horizontalStep = (box.maxX - box.minX) * RandomUtil.jitter(1.0D, 0.04D) / 6.0D;
        double verticalStep = (box.maxY - box.minY) * RandomUtil.jitter(1.0D, 0.06D) / 4.0D;
        entityInterceptRotations.clear();
        for (double x = box.minX; x < box.maxX; x += horizontalStep) {
            for (double z = box.minZ; z < box.maxZ; z += horizontalStep) {
                for (double y = box.minY; y < box.maxY; y += verticalStep) {
                    Vec3 point = new Vec3(x, y, z);
                    if (point.distanceTo(eyes) <= range) entityInterceptRotations.add(getYawBetween(point, eyes));
                }
                entityInterceptRotations.add(getYawBetween(new Vec3(x, RandomUtil.jitter(eyes.yCoord, 0.01D), z), eyes));
            }
        }
        Vec2f predictedAngles = getYawBetween(predicted, eyes);
        if (((com.adjustclient.modules.Module) MovementFixModule.INSTANCE).isEnabled())
            predictedAngles.x = getClosestYaw(predictedAngles.x);
        float yawDifference = MathHelper.wrapAngleTo180_float(predictedAngles.x - current.x);
        final float preferredPitch = MathHelper.clamp_float(RandomUtil.jitter(current.y,
                Math.abs(yawDifference) / 180.0F * 16.0F + 2.3F), -90.0F, 90.0F);
        entityInterceptRotations.sort(Comparator.comparingDouble(rotation -> {
            float yaw = MathHelper.wrapAngleTo180_float(predictedAngles.x - rotation.x);
            float pitch = preferredPitch - rotation.y;
            return Math.sqrt(pitch * pitch + yaw * yaw);
        }));
        if (ignoreOtherEntities) ignoredRaytraceEntity = target;
        AimPoint result = null;
        for (Vec2f rotation : entityInterceptRotations) {
            MovingObjectPosition hit = WorldUtil.pickEntities(mc.thePlayer, throughWalls, true, rotation.x,
                    rotation.y, range, mc.playerController.getBlockReachDistance(), 0.0D);
            if (hit != null && hit.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && hit.entityHit == target) {
                result = new AimPoint(hit.hitVec, hit.hitVec, rotation, false);
                break;
            }
        }
        ignoredRaytraceEntity = null;
        return result;
    }
    public static Vec3 getAimVec(Entity target, Vec3 position, Vec3 point, boolean clampHeight, double hFactor, double vFactor) {
        AxisAlignedBB box = target.getEntityBoundingBox().offset(position.xCoord - target.posX,
                position.yCoord - target.posY, position.zCoord - target.posZ);
        double halfWidth = Math.min((box.maxX - box.minX) / 2.0D, 0.300000011920929D);
        double centerX = (box.minX + box.maxX) / 2.0D;
        double centerZ = (box.minZ + box.maxZ) / 2.0D;
        box = new AxisAlignedBB(centerX - halfWidth, box.minY, centerZ - halfWidth,
                centerX + halfWidth, box.maxY, centerZ + halfWidth);
        double eyeY = Math.clamp(position.yCoord + target.getEyeHeight(), box.minY, box.maxY);
        double x = Math.clamp(point.xCoord, box.minX, box.maxX);
        double y = Math.clamp(point.yCoord, box.minY, box.maxY);
        double z = Math.clamp(point.zCoord, box.minZ, box.maxZ);
        if (clampHeight) y = Math.clamp(y, box.minY + (box.maxY - box.minY) * 0.05D,
                box.minY + (box.maxY - box.minY) * 0.9D);
        return new Vec3(centerX + (x - centerX) * hFactor,
                eyeY + (y - eyeY) * vFactor, centerZ + (z - centerZ) * hFactor);
    }
    public static List<HitVec> getSortedHitVecs(Entity entity, AxisAlignedBB box, EnumFacing facing,
            double horizontalFactor, double verticalFactor, float accuracy) {
        List<HitVec> candidates = getHitVecs(entity, box, facing, horizontalFactor, verticalFactor, accuracy);
        Entity player = Minecraft.getMinecraft().thePlayer;
        Vec2f current = new Vec2f(player.rotationYaw, player.rotationPitch);
        candidates.sort(Comparator.comparingDouble(candidate -> {
            float yaw = Math.abs(MathHelper.wrapAngleTo180_float(current.x - candidate.angles.x));
            float pitch = current.y - candidate.angles.y;
            return (double) pitch * pitch + (double) yaw * yaw;
        }));
        return candidates;
    }
    public static AimPoint findEntityIntercept(Entity target, Vec3 eyes, boolean clampHeight, boolean throughWalls, boolean ignoreOtherEntities,
            double range, double horizontalFactor, double verticalFactor) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!mc.isCallingFromMinecraftThread()) throw new IllegalStateException("findEntityIntercept() called outside of game thread");
        Vec3 position = new Vec3(target.posX, target.posY, target.posZ);
        // This overload obtains its bounds from WorldUtil, rather than the fixed
        // player box used by the rotation-jitter solver above.
        AxisAlignedBB preferredBox = WorldUtil.entityBoundsAt(target, position);
        double centerX = (preferredBox.minX + preferredBox.maxX) / 2.0D;
        double centerZ = (preferredBox.minZ + preferredBox.maxZ) / 2.0D;
        double eyeY = Math.clamp(position.yCoord + target.getEyeHeight(), preferredBox.minY, preferredBox.maxY);
        double clampedX = Math.clamp(eyes.xCoord, preferredBox.minX, preferredBox.maxX);
        double clampedY = Math.clamp(eyes.yCoord, preferredBox.minY, preferredBox.maxY);
        double clampedZ = Math.clamp(eyes.zCoord, preferredBox.minZ, preferredBox.maxZ);
        if (clampHeight) clampedY = Math.clamp(clampedY, preferredBox.minY + (preferredBox.maxY - preferredBox.minY) * 0.05D,
                preferredBox.minY + (preferredBox.maxY - preferredBox.minY) * 0.9D);
        Vec3 preferred = new Vec3(centerX + (clampedX - centerX) * horizontalFactor,
                eyeY + (clampedY - eyeY) * verticalFactor, centerZ + (clampedZ - centerZ) * horizontalFactor);
        if (preferred.distanceTo(eyes) > range) return null;
        AxisAlignedBB box = WorldUtil.entityBoundsAt(target, position);
        double horizontalStep = (box.maxX - box.minX) / 6.0D;
        double verticalStep = (box.maxY - box.minY) / 4.0D;
        entityInterceptPoints.clear();
        entityInterceptPoints.add(preferred);
        for (double x = box.minX; x < box.maxX; x += horizontalStep) {
            for (double z = box.minZ; z < box.maxZ; z += horizontalStep) {
                for (double y = box.minY; y < box.maxY; y += verticalStep) {
                    Vec3 point = new Vec3(x, y, z);
                    if (point.distanceTo(eyes) <= range) entityInterceptPoints.add(point);
                }
                entityInterceptPoints.add(new Vec3(x, eyes.yCoord, z));
            }
        }
        entityInterceptPoints.sort(Comparator.comparingDouble(point -> preferred.squareDistanceTo(point)));
        if (ignoreOtherEntities) ignoredRaytraceEntity = target;
        Vec3 point = entityInterceptPoints.get(0);
        Vec2f angles = getYawBetween(point, eyes);
        MovingObjectPosition hit = WorldUtil.pickEntities(mc.thePlayer, throughWalls, true,
                angles.x, angles.y, range, mc.playerController.getBlockReachDistance(), 0.0D);
        AimPoint result = null;
        if (hit != null && hit.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && hit.entityHit == target) {
            result = new AimPoint(hit.hitVec, point, angles, true);
        } else if (hit != null && (hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
                || !ignoreOtherEntities && hit.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY)) {
            for (int i = 1; i < entityInterceptPoints.size(); i++) {
                point = entityInterceptPoints.get(i);
                angles = getYawBetween(point, eyes);
                hit = WorldUtil.pickFromPlayer(throughWalls, angles.x, angles.y, range);
                if (hit != null && hit.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && hit.entityHit == target) {
                    result = new AimPoint(hit.hitVec, point, angles, false);
                    break;
                }
            }
        }
        ignoredRaytraceEntity = null;
        return result;
    }
    public static BlockPlacement findPlacement(Entity entity, BlockPos target, Vec2f rotation, PlacementFacingMode mode,
            boolean useCandidateBlock, double horizontalFactor, double verticalFactor, float accuracy) {
        MovingObjectPosition initial = WorldUtil.rayTrace(entity, 4.5D, rotation.x, rotation.y);
        if (initial != null && initial.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
                && isPlacementTarget(initial, target) && !isInteractable(initial.getBlockPos())) {
            return new BlockPlacement(initial.getBlockPos(), initial.sideHit, initial.hitVec, rotation);
        }
        List<HitVec> candidates = new ArrayList<>();
        for (EnumFacing facing : EnumFacing.values()) {
            if (isInteractable(target)) continue;
            BlockPos neighbor = target.offset(facing);
            AxisAlignedBB box = BlockUtil.getCollisionBox(neighbor);
            if (box == null) continue;
            List<HitVec> sampled = getHitVecs(entity, box, facing, (float) horizontalFactor, (float) verticalFactor, accuracy);
            for (HitVec candidate : sampled) candidate.blockPos = neighbor;
            candidates.addAll(sampled);
        }
        candidates.sort(Comparator.comparingDouble(candidate -> {
            float yaw = MathHelper.wrapAngleTo180_float(rotation.x - candidate.angles.x);
            double pitch = rotation.y - candidate.angles.y;
            double score = yaw * yaw + pitch * pitch;
            for (double coordinate : new double[] {candidate.xCoord, candidate.yCoord, candidate.zCoord}) {
                double value = Math.abs(coordinate);
                if (value != 0.0D && value != 1.0D) score *= 1.0D + (1.0D - Math.abs(0.5D - value) * 2.0D) * 0.05D;
            }
            return score;
        }));
        final PlacementFacingMode facingMode = PlacementFacingMode.DEFAULT;
        Predicate<MovingObjectPosition> filter = hit -> {
            return isAllowedPlacementHit(facingMode, target, hit);
        };
        Pair<HitVec, MovingObjectPosition> found = findFilteredHitRotation(candidates, entity, false, filter, useCandidateBlock);
        if (found == null) return null;
        MovingObjectPosition hit = found.getRight();
        return new BlockPlacement(hit.getBlockPos(), hit.sideHit, hit.hitVec, found.getLeft().angles);
    }
    public static void forceMouseOverEntity(Entity entity) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.pointedEntity == entity) return;
        MovingObjectPosition hit = WorldUtil.pickFromEntities(mc.thePlayer, true, false,
                java.util.Collections.singletonList(entity), mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, 6.0D, 0.0D, 0.0D);
        if (hit != null && hit.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && hit.entityHit == entity) {
            WorldUtil.setMouseOver(hit);
            return;
        }
        double x = RandomUtil.jitter(entity.posX, 0.1D);
        double y = entity.posY + entity.getEyeHeight()
                - (entity.getEyeHeight() / 2.05F) * RandomUtil.nextDouble(0.0D, 1.0D);
        double z = RandomUtil.jitter(entity.posZ, 0.1D);
        mc.pointedEntity = entity;
        mc.objectMouseOver = new MovingObjectPosition(entity, new Vec3(x, y, z));
        com.adjustclient.utilities.anticheat.ServerTracker.INSTANCE.mouseOverOverridden = true;
    }
    public static List<HitVec> getHitVecs(Entity entity, AxisAlignedBB box, EnumFacing facing,
            double horizontalFactor, double verticalFactor, float accuracy) {
        Vec3 center = new Vec3((box.minX + box.maxX) / 2.0D, (box.minY + box.maxY) / 2.0D, (box.minZ + box.maxZ) / 2.0D);
        double inset = RandomUtil.jitter(HypixelPlacementLimiter.TIMING_MARGIN, 0.01D);
        double horizontal = horizontalFactor - inset;
        double vertical = verticalFactor - inset;
        double xRadius = (box.maxX - box.minX) * horizontal;
        double yRadius = (box.maxY - box.minY) * vertical;
        double zRadius = (box.maxZ - box.minZ) * horizontal;
        double xStep = xRadius / accuracy;
        double yStep = yRadius / accuracy;
        double zStep = zRadius / accuracy;
        List<HitVec> result = new ArrayList<>();
        for (double x = -xRadius; x < xRadius; x += xStep) {
            for (double y = -yRadius; y < yRadius; y += yStep) {
                for (double z = -zRadius; z < zRadius; z += zStep) {
                    Vec3 point = center.addVector(x, y, z);
                    Vec3 eyes;
                    if (entity instanceof net.minecraft.client.entity.EntityPlayerSP) {
                        net.minecraft.client.entity.EntityPlayerSP player = (net.minecraft.client.entity.EntityPlayerSP) entity;
                        float eyeHeight = player.isPlayerSleeping() ? 0.2F : 1.62F;
                        if (!player.isPlayerSleeping() && player.movementInput != null && player.movementInput.sneak) eyeHeight -= 0.08F;
                        eyes = new Vec3(player.posX, player.posY + eyeHeight, player.posZ);
                    } else {
                        eyes = entity.getPositionEyes(1.0F);
                    }
                    Vec2f angles = getYawBetween(point, eyes);
                    result.add(new HitVec(x, y, z, angles, facing, null, null));
                }
            }
        }
        return result;
    }
    public static boolean isDiagonalYaw(float yaw) {
        float normalized = yaw % 360.0F;
        if (normalized < 0.0F) normalized += 360.0F;
        for (float diagonal : new float[] {45.0F, 135.0F, 225.0F, 315.0F}) {
            float delta = Math.abs(normalized - diagonal);
            if (Math.min(delta, 360.0F - delta) <= 22.5F) return true;
        }
        return false;
    }
    public static Vec2f quantizeToMouseSensitivity(Vec2f rotation, Vec2f previous) {
        float sensitivity = Minecraft.getMinecraft().gameSettings.mouseSensitivity;
        double factor = sensitivity * 0.6F + 0.2F;
        double step = factor * factor * factor * 8.0D * 0.1500000059604645D;
        float yawDelta = (float) (Math.round((rotation.x - previous.x) / step) * step);
        float pitchDelta = (float) (Math.round((rotation.y - previous.y) / step) * step);
        return new Vec2f(previous.x + yawDelta, previous.y + pitchDelta);
    }
    public static double angularDistance(Vec2f first, Vec2f second) {
        double yaw = MathHelper.wrapAngleTo180_float(first.x - second.x);
        double pitch = first.y - second.y;
        return Math.sqrt(yaw * yaw + pitch * pitch);
    }
    public static Vec2f getYawBetween(Vec3 target, Vec3 origin) {
        double x = target.xCoord - origin.xCoord;
        double y = target.yCoord - origin.yCoord;
        double z = target.zCoord - origin.zCoord;
        double horizontal = Math.sqrt(x * x + z * z);
        return new Vec2f((float) (Math.atan2(z, x) * 180.0D / Math.PI - 90.0D),
                (float) -(Math.atan2(y, horizontal) * 180.0D / Math.PI));
    }
}
