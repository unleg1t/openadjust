package com.adjustclient.modules.impl.combat.builder.feature.headhitter;

import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.MovementInputEvent;
import com.adjustclient.modules.impl.combat.builder.*;
import com.adjustclient.modules.impl.combat.builder.feature.BuilderFeature;
import com.adjustclient.utilities.*;
import java.util.*;
import net.minecraft.util.*;
import net.minecraft.util.EnumFacing;

public final class HeadHitterFeature extends BuilderFeature {
    public static HeadHitterFeature INSTANCE;
    public boolean checkedEarlyJump;
    public boolean sprinting;
    public HeadHitterPlan plan;
    public BuildTarget target;
    public BlockPos lockedPosition;
    public EnumFacing lockedFacing;
    public Float movementYaw;

    public HeadHitterFeature(BuilderModule owner, String bindId, String bindDescription) {
        super(owner, "Head Hitter", bindId, bindDescription, true);
        INSTANCE = this;
    }

    @Override public void activate() {
        sprinting = false;
        plan = null;
        lockedPosition = null;
        lockedFacing = null;
        movementYaw = null;
        checkedEarlyJump = false;
        if (owner.placer.target == target) owner.placer.target = null;
        target = null;
    }

    public void steer(MovementInputEvent event) {
        if (movementYaw == null) return;
        float best = Float.MAX_VALUE, bestForward = 0F, bestStrafe = 0F;
        for (float forward = -1F; forward <= 1F; ++forward) {
            for (float strafe = -1F; strafe <= 1F; ++strafe) {
                if (forward == 0F && strafe == 0F) continue;
                float direction = MovementUtil.getDirection(mc.thePlayer.rotationYaw, forward, strafe);
                float difference = Math.abs(MathHelper.wrapAngleTo180_float(movementYaw - direction));
                if (difference < best || difference == best && forward > bestForward) {
                    best = difference;
                    bestForward = forward;
                    bestStrafe = strafe;
                }
            }
        }
        event.back = bestForward < 0F;
        event.forward = bestForward > 0F;
        event.left = bestStrafe > 0F;
        event.right = bestStrafe < 0F;
    }

    @Override public void deactivate() {
        if (!active) return;
        active = false;
        sprinting = false;
        plan = null;
        lockedPosition = null;
        lockedFacing = null;
        movementYaw = null;
        checkedEarlyJump = false;
        if (owner.placer.target == target) owner.placer.target = null;
        target = null;
        owner.updateSuffix();
    }

    @EventHandler public void onInput(MovementInputEvent event) {
        if (!active || !owner.isEnabled()) return;
        if (mc.thePlayer == null || mc.theWorld == null || mc.currentScreen != null
                || (!event.forward && !mc.gameSettings.keyBindForward.isKeyDown()) || !mc.thePlayer.isSprinting()) {
            activate();
            return;
        }
        sprinting = true;
        event.jump = true;
        if (mc.thePlayer.onGround) {
            steer(event);
            plan = null;
            lockedPosition = null;
            lockedFacing = null;
            movementYaw = null;
            checkedEarlyJump = false;
            if (owner.placer.target == target) owner.placer.target = null;
            target = null;
            return;
        }
        if (plan != null) { steer(event); return; }
        int airTicks = mc.thePlayer.offGroundTicks;
        if (!checkedEarlyJump && (airTicks == 1 || airTicks == 2)) {
            checkedEarlyJump = true;
            float[] movement = event.getMovement();
            HeadHitterPlan candidate = predictPlan(movement);
            if (candidate != null && hasRoofSupport(candidate.roof())) {
                plan = candidate;
                movementYaw = MovementUtil.getDirection(mc.thePlayer.rotationYaw, movement[0], movement[1]);
            }
        } else if (airTicks >= 3) {
            float[] movement = event.getMovement();
            HeadHitterPlan candidate = predictPlan(movement);
            if (candidate != null) {
                plan = candidate;
                movementYaw = MovementUtil.getDirection(mc.thePlayer.rotationYaw, movement[0], movement[1]);
            }
        }
    }

    public void updateBuild() {
        if (!sprinting || plan == null || mc.thePlayer == null || mc.theWorld == null) {
            if (owner.placer.target == target) owner.placer.target = null;
            target = null;
            return;
        }
        if (lockedPosition != null && BlockUtil.isNotReplaceable(lockedPosition)) {
            lockedPosition = null;
            lockedFacing = null;
        }
        List<HeadHitterSupport> supports = findSupports(plan);
        if (lockedPosition != null && lockedFacing != null) {
            supports.removeIf(support -> {
                return support.positions().size() >= 2
                        && !support.positions().get(1).equals(lockedPosition.offset(lockedFacing));
            });
        }
        if (supports.isEmpty()) {
            if (owner.placer.target == target) owner.placer.target = null;
            target = null;
            return;
        }
        if (retainTarget(owner.placer, supports)) return;
        BuildTarget previous = owner.placer.target;
        List<HeadHitterPlacement> placements = new ArrayList<>(supports.size());
        for (HeadHitterSupport support : supports) {
            BlockPos position = support.lastPosition();
            BuilderBlockFilter filter = new BuilderBlockFilter(position, net.minecraft.util.EnumFacing.NORTH, null, -1);
            BuildTarget candidate = new BuildTarget(List.of(filter), new ArrayList<>(List.of(filter)), 0, false, true, false, false);
            owner.placer.target = candidate;
            BlockPlacement hit = owner.placer.findPlacement(filter, 4F);
            if (hit == null) continue;
            filter.cachedPlacement = hit;
            float yaw = MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - hit.angles.x);
            float pitch = mc.thePlayer.rotationPitch - hit.angles.y;
            placements.add(new HeadHitterPlacement(support, yaw * yaw + pitch * pitch, candidate));
        }
        owner.placer.target = previous;
        if (placements.isEmpty()) {
            if (owner.placer.target == target) owner.placer.target = null;
            target = null;
            return;
        }
        Comparator<HeadHitterPlacement> ordering = Comparator.comparingInt(value -> value.support().positions().size());
        ordering = ordering.thenComparing(Comparator.comparingDouble((HeadHitterPlacement value) -> value.support().clearance()).reversed());
        ordering = ordering.thenComparingDouble(value -> value.rotationDifference());
        ordering = ordering.thenComparingInt(value -> value.support().lastPosition().getX());
        ordering = ordering.thenComparingInt(value -> value.support().lastPosition().getY());
        ordering = ordering.thenComparingInt(value -> value.support().lastPosition().getZ());
        placements.sort(ordering);
        HeadHitterPlacement selected = placements.getFirst();
        owner.placer.target = target = selected.target();
        if (lockedPosition == null && selected.support().positions().size() >= 2) {
            BlockPos second = selected.support().positions().get(1);
            for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                if (plan.roof().offset(facing).equals(second)) {
                    lockedPosition = plan.roof();
                    lockedFacing = facing;
                    break;
                }
            }
        }
    }

    public List<HeadHitterSupport> findSupports(HeadHitterPlan plan) {
        if (BlockUtil.isNotReplaceable(plan.roof())) return new ArrayList<>();
        List<BlockPos> path = new ArrayList<>(4);
        path.add(plan.roof());
        Set<BlockPos> visited = new HashSet<>();
        visited.add(plan.roof());
        List<HeadHitterSupport> result = new ArrayList<>();
        searchSupports(plan, plan.roof(), path, visited, result);
        return result;
    }

    public boolean hasRoofSupport(BlockPos roof) {
        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            BlockPos neighbor = roof.offset(facing);
            if (!BlockUtil.hasCollision(neighbor) || !BlockUtil.hasCollision(neighbor.up())) continue;
            for (EnumFacing second : EnumFacing.values()) {
                if (second.getAxis() == facing.getAxis()) continue;
                if (BlockUtil.hasCollision(neighbor.offset(second))
                        || BlockUtil.hasCollision(neighbor.up().offset(second))) return true;
            }
        }
        return false;
    }

    public HeadHitterPlan predictPlan(float[] movement) {
        Simulation simulation = new Simulation(mc.thePlayer);
        simulation.includeSpeedModule();
        simulation.setRotation(movement, true);
        SimulationPlayer player = simulation.player;
        List<AxisAlignedBB> boxes = new ArrayList<>(29);
        boxes.add(player.getEntityBoundingBox());
        boolean airborne = !player.onGround;
        boolean landed = false;
        for (int tick = 0; tick < 20; ++tick) {
            player.tick();
            boxes.add(player.getEntityBoundingBox());
            if (player.onGround && airborne) { landed = true; break; }
            if (!player.onGround) airborne = true;
        }
        if (!landed) return null;
        BlockPos landing = new BlockPos(player.getPositionVector());
        AxisAlignedBB landingBox = player.getEntityBoundingBox();
        player.tick();
        boxes.add(player.getEntityBoundingBox());
        BlockPos roof = new BlockPos(player.posX, landing.getY() + 2, player.posZ);
        for (int tick = 0; tick < 7; ++tick) {
            player.tick();
            boxes.add(player.getEntityBoundingBox());
        }
        List<AxisAlignedBB> movementBoxes = List.copyOf(boxes);
        HeadHitterPlan original = new HeadHitterPlan(landing, roof, movementBoxes);
        if (BlockUtil.isNotReplaceable(roof) || !findSupports(original).isEmpty()) return original;
        List<HeadHitterCandidate> candidates = new ArrayList<>();
        for (int x = MathHelper.floor_double(landingBox.minX); x <= MathHelper.floor_double(landingBox.maxX - .000001D); ++x) {
            double overlapX = Math.min(landingBox.maxX, x + 1D) - Math.max(landingBox.minX, x);
            if (overlapX < .15D) continue;
            for (int z = MathHelper.floor_double(landingBox.minZ); z <= MathHelper.floor_double(landingBox.maxZ - .000001D); ++z) {
                if (x == roof.getX() && z == roof.getZ()) continue;
                double overlapZ = Math.min(landingBox.maxZ, z + 1D) - Math.max(landingBox.minZ, z);
                if (overlapZ < .15D) continue;
                HeadHitterPlan alternative = new HeadHitterPlan(landing, new BlockPos(x, roof.getY(), z), movementBoxes);
                List<HeadHitterSupport> supports = findSupports(alternative);
                for (HeadHitterSupport support : supports) candidates.add(new HeadHitterCandidate(alternative, support, overlapX * overlapZ));
            }
        }
        Comparator<HeadHitterCandidate> ordering = Comparator.comparingInt(value -> value.support().positions().size());
        ordering = ordering.thenComparing(Comparator.comparingDouble((HeadHitterCandidate value) -> value.support().clearance()).reversed());
        ordering = ordering.thenComparing(Comparator.comparingDouble((HeadHitterCandidate value) -> value.overlap()).reversed());
        ordering = ordering.thenComparingInt(value -> value.plan().roof().getX());
        ordering = ordering.thenComparingInt(value -> value.plan().roof().getZ());
        candidates.sort(ordering);
        return candidates.isEmpty() ? original : candidates.getFirst().plan();
    }

    public boolean retainTarget(BuilderPlacer placer, List<HeadHitterSupport> supports) {
        if (target == null || placer.target != target || target.pending.isEmpty()) return false;
        BuilderBlockFilter filter = target.pending.getFirst();
        if (BlockUtil.isNotReplaceable(filter.position)) return false;
        for (HeadHitterSupport support : supports) {
            if (support.lastPosition().equals(filter.position)) {
                BlockPlacement placement = placer.findPlacement(filter, 4F);
                if (placement == null) return false;
                filter.cachedPlacement = placement;
                return true;
            }
        }
        return false;
    }

    public double clearance(List<BlockPos> positions, List<AxisAlignedBB> movement) {
        if (positions.size() == 1) return 0D;
        double minimum = Double.POSITIVE_INFINITY;
        for (int i = 1; i < positions.size(); ++i) {
            AxisAlignedBB block = new AxisAlignedBB(positions.get(i));
            for (AxisAlignedBB box : movement) {
                double x = Math.max(0D, Math.max(box.minX - block.maxX, block.minX - box.maxX));
                double y = Math.max(0D, Math.max(box.minY - block.maxY, block.minY - box.maxY));
                double z = Math.max(0D, Math.max(box.minZ - block.maxZ, block.minZ - box.maxZ));
                minimum = Math.min(minimum, x * x + y * y + z * z);
            }
        }
        return minimum;
    }

    public void searchSupports(HeadHitterPlan plan, BlockPos position, List<BlockPos> path,
            Set<BlockPos> visited, List<HeadHitterSupport> result) {
        for (EnumFacing facing : EnumFacing.values()) {
            if (BlockUtil.hasCollision(position.offset(facing))) {
                result.add(new HeadHitterSupport(List.copyOf(path), clearance(path, plan.movement())));
                break;
            }
        }
        if (path.size() >= 4) return;
        EnumFacing vertical = null;
        for (int i = 1; i < path.size(); ++i) {
            int delta = path.get(i).getY() - path.get(i - 1).getY();
            if (delta != 0) { vertical = delta > 0 ? EnumFacing.UP : EnumFacing.DOWN; break; }
        }
        for (EnumFacing facing : EnumFacing.values()) {
            if (facing.getAxis() == EnumFacing.Axis.Y && vertical != null && facing != vertical) continue;
            BlockPos next = position.offset(facing);
            if (next.getY() < plan.landing().getY() || next.getY() > plan.roof().getY()
                    || visited.contains(next) || BlockUtil.isNotReplaceable(next)) continue;
            AxisAlignedBB block = new AxisAlignedBB(next);
            boolean intersects = false;
            for (AxisAlignedBB box : plan.movement()) {
                if (box.expand(.05D, .05D, .05D).intersectsWith(block)) { intersects = true; break; }
            }
            if (intersects) continue;
            visited.add(next);
            path.add(next);
            searchSupports(plan, next, path, visited, result);
            path.removeLast();
            visited.remove(next);
        }
    }
}

