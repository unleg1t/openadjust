package com.adjustclient.module.impl.combat.builder.feature.blockin;

import com.adjustclient.event.EventHandler;
import com.adjustclient.event.MovementInputEvent;
import com.adjustclient.module.impl.combat.builder.*;
import com.adjustclient.module.impl.combat.builder.feature.BuilderFeature;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.setting.NumberSetting;
import com.adjustclient.util.*;
import java.util.*;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.*;

public final class BlockInFeature extends BuilderFeature {
    public final BooleanSetting optimizeMovement;
    public final NumberSetting waitToPlace;
    public BlockPos anchor;

    public BlockInFeature(BuilderModule owner, String bindId, String bindDescription) {
        super(owner, "Block In", bindId, bindDescription, true);
        optimizeMovement = new BooleanSetting(settings, "Optimize movement", false);
        waitToPlace = new NumberSetting(settings, "Wait to place", 0, 0, 3, 1);
    }

    public void updateBuild() {
        anchor = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        owner.placer.target = null;
        BuilderBlockFilter next = selectImmediatePlacement();
        if (next == null) next = selectEnclosurePlacement();
        if (next != null) {
            owner.placer.target = new BuildTarget(List.of(next), new ArrayList<>(List.of(next)),
                    waitToPlace.getValue().intValue(), false, true, true, false);
        } else if (!hold.isEnabled() || hold.isHidden()) {
            anchor = null;
            deactivate();
        }
    }

    @EventHandler
    public void onInput(MovementInputEvent event) {
        if (!active || !owner.isEnabled()) return;
        if (anchor == null || !optimizeMovement.isEnabled() || optimizeMovement.isHidden()
                || !mc.thePlayer.onGround) return;
        Vec3 center = new Vec3(anchor.getX() + .5D, anchor.getY(), anchor.getZ() + .5D);
        Vec3 player = mc.thePlayer.getPositionVector();
        if (isCentered(player)) {
            event.back = event.forward = event.left = event.right = false;
            return;
        }
        float difference = MathHelper.wrapAngleTo180_float(RotationUtil.getYawBetween(center, player).x - mc.thePlayer.rotationYaw);
        event.back = Math.abs(difference) > 134.5F;
        event.forward = Math.abs(difference) < 45.5F;
        event.left = Math.abs(MathHelper.wrapAngleTo180_float(difference + 90F)) < 45.5F;
        event.right = Math.abs(MathHelper.wrapAngleTo180_float(difference - 90F)) < 45.5F;
    }

    public boolean hasExposedNeighbor(BlockPos position) {
        BlockPos above = anchor.up();
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos neighbor = position.offset(facing);
            if (!BlockUtil.isNotReplaceable(neighbor)
                    && !neighbor.equals(anchor) && !neighbor.equals(above)) return true;
        }
        return false;
    }

    public boolean isCentered(Vec3 position) {
        return position.xCoord >= anchor.getX() + .3D && position.xCoord <= anchor.getX() + .7D
                && position.zCoord >= anchor.getZ() + .3D && position.zCoord <= anchor.getZ() + .7D;
    }

    public BuilderBlockFilter selectPlacement(List<BlockPos> positions, Vec3 eyes, boolean preferSoft) {
        double reachSquared = mc.playerController.isInCreativeMode() ? 25D : 20.25D;
        positions.sort(Comparator.comparingDouble(position -> {
            Vec2f angles = RotationUtil.getYawBetween(new Vec3(position).addVector(.5D, .5D, .5D), mc.thePlayer.getPositionEyes(1F));
            float yaw = mc.thePlayer.rotationYaw;
            float corrected = yaw + MathHelper.wrapAngleTo180_float(angles.x - yaw);
            return Math.abs(MathHelper.wrapAngleTo180_float(yaw - corrected)) + Math.abs(mc.thePlayer.rotationPitch - angles.y);
        }));
        for (BlockPos position : positions) {
            if (DistanceUtil.distanceToBlock(eyes, position) >= reachSquared
                    || new AxisAlignedBB(position).intersectsWith(mc.thePlayer.getEntityBoundingBox())
                    || !hasCollidableNeighbor(position)) continue;
            BuilderBlockFilter candidate = new BuilderBlockFilter(position, EnumFacing.NORTH, null, -1);
            candidate.preferSoft = preferSoft;
            candidate.cachedPlacement = owner.placer.findPlacement(candidate, 4F);
            if (candidate.cachedPlacement != null) return candidate;
        }
        return null;
    }

    public boolean hasCollidableNeighbor(BlockPos position) {
        for (EnumFacing facing : EnumFacing.values()) {
            if (BlockUtil.hasCollision(position.offset(facing))) return true;
        }
        return false;
    }

    @Override
    public void deactivate() {
        anchor = null;
        super.deactivate();
    }

    public BuilderBlockFilter selectImmediatePlacement() {
        BlockPos below = anchor.up(2);
        if (BlockUtil.isNotReplaceable(below)) return null;
        if (!new AxisAlignedBB(below).intersectsWith(mc.thePlayer.getEntityBoundingBox())) {
            BuilderBlockFilter candidate = new BuilderBlockFilter(below, EnumFacing.NORTH, null, -1);
            candidate.cachedPlacement = owner.placer.findPlacement(candidate, 4F);
            if (candidate.cachedPlacement != null) return candidate;
        }
        Vec3 eyes = mc.thePlayer.getPositionEyes(1F);
        double reach = mc.playerController.isInCreativeMode() ? 5D : 4.5D;
        Set<BlockPos> visited = new HashSet<>();
        List<BlockPos> candidates = new ArrayList<>();
        for (int y = MathHelper.floor_double(eyes.yCoord); y <= MathHelper.floor_double(eyes.yCoord + reach); ++y) {
            for (int x = MathHelper.floor_double(eyes.xCoord - reach); x <= MathHelper.floor_double(eyes.xCoord + reach); ++x) {
                for (int z = MathHelper.floor_double(eyes.zCoord - reach); z <= MathHelper.floor_double(eyes.zCoord + reach); ++z) {
                    BlockPos support = new BlockPos(x, y, z);
                    if (!BlockUtil.isNotReplaceable(support) || DistanceUtil.distanceToBlock(eyes, support) > reach * reach) continue;
                    for (EnumFacing facing : EnumFacing.values()) {
                        BlockPos next = support.offset(facing);
                        if (next.getY() <= MathHelper.floor_double(eyes.yCoord)
                                || BlockUtil.isNotReplaceable(next)
                                || new AxisAlignedBB(next).intersectsWith(mc.thePlayer.getEntityBoundingBox())) continue;
                        if (visited.add(next)) candidates.add(next);
                    }
                }
            }
        }
        candidates.sort(Comparator.comparingDouble(position -> below.distanceSq(position)));
        for (BlockPos position : candidates) {
            BuilderBlockFilter candidate = new BuilderBlockFilter(position, EnumFacing.NORTH, null, -1);
            candidate.preferSoft = true;
            candidate.cachedPlacement = owner.placer.findPlacement(candidate, 4F);
            if (candidate.cachedPlacement != null) return candidate;
        }
        return null;
    }

    public BuilderBlockFilter selectEnclosurePlacement() {
        List<BlockPos> positions = new ArrayList<>();
        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            for (BlockPos base : new BlockPos[] {anchor, anchor.up()}) {
                BlockPos position = base.offset(facing);
                if (!BlockUtil.isNotReplaceable(position)) positions.add(position);
            }
        }
        if (positions.isEmpty()) return null;
        Vec3 player = mc.thePlayer.getPositionVector();
        var enemies = com.adjustclient.util.anticheat.ServerTracker.INSTANCE.entityTracker.getEntities(66,
                com.adjustclient.util.anticheat.entity.TrackedFakePlayer.class);
        enemies.sort(Comparator.comparingDouble(enemy -> enemy.getEntity().getDistanceSqToEntity(mc.thePlayer)));
        if (!enemies.isEmpty() && enemies.get(0).getEntity().getDistanceSqToEntity(mc.thePlayer) < 100D) {
            Vec3 enemyPosition = enemies.get(0).getEntity().getPositionVector();
            List<BlockPos> nearest = new ArrayList<>(positions);
            nearest.sort(Comparator.comparingDouble(position -> enemyPosition.squareDistanceTo(new Vec3(position).addVector(.5D, .5D, .5D))));
            BuilderBlockFilter placement = selectPlacement(nearest.subList(0, Math.min(3, nearest.size())), player, false);
            if (placement != null) return placement;
        }
        BuilderBlockFilter placement = selectPlacement(positions, player, false);
        if (placement != null) return placement;
        List<BlockPos> frontier = new ArrayList<>(positions);
        Set<BlockPos> visited = new HashSet<>(positions);
        for (int depth = 0; depth < 5 && !frontier.isEmpty(); ++depth) {
            List<BlockPos> next = new ArrayList<>();
            for (BlockPos position : frontier) {
                for (EnumFacing facing : EnumFacing.values()) {
                    BlockPos neighbor = position.offset(facing);
                    if (!BlockUtil.isNotReplaceable(neighbor) && visited.add(neighbor) && hasExposedNeighbor(neighbor)) next.add(neighbor);
                }
            }
            placement = selectPlacement(next, player, true);
            if (placement != null) return placement;
            frontier.clear();
            frontier.addAll(next);
        }
        return null;
    }
}
