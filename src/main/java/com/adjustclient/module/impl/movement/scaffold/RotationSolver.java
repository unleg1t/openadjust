package com.adjustclient.module.impl.movement.scaffold;

import com.adjustclient.util.BlockPlacement;
import com.adjustclient.util.BlockUtil;
import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.util.MovementUtil;
import com.adjustclient.util.Vec2f;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatComparator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldSettings;
import com.adjustclient.rotation.handler.RenderRotationTracker;
import com.adjustclient.util.RotationUtil;

public final class RotationSolver {

    private final ScaffoldModule module;
    private final PlacementFinder geometry;
    /**
     * Remembered difference between the yaw that was used for the last placement and the base yaw ("stable yaw"); null
     * until a placement was solved in strict mode.
     */
    Float yawOffset;

    public RotationSolver(ScaffoldModule module, PlacementFinder geometry) {
        this.module = module;
        this.geometry = geometry;
    }

    public PlacementTarget solve(EntityPlayer player, Collection<BlockPos> positions, Vec2f current, boolean strict,
                                 int maxBlocks, UnaryOperator<Vec2f> rotationFn) {
        // ---- 1. candidate positions: passable blocks right below the player's feet that are not blocked by entities
        int belowY = (int) Math.floor(MinecraftInstance.mc.thePlayer.posY) - 1;
        List<BlockPos> candidates = new ArrayList<>();
        for (BlockPos pos : positions) {
            if (pos.getY() != belowY) {
                continue;
            }
            if (BlockUtil.getCollisionBox(pos) != null) {
                continue;
            }
            IBlockState state = MinecraftInstance.mc.theWorld.getBlockState(pos);
            if (!BlockUtil.canReplace(state.getBlock(), MinecraftInstance.mc.theWorld, pos)) {
                continue;
            }
            AxisAlignedBB cell = blockBox(pos);
            if (MinecraftInstance.mc.theWorld.checkNoEntityCollision(cell, MinecraftInstance.mc.thePlayer)) {
                candidates.add(pos);
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }

        // ---- 2. candidate yaws: the base yaw rotated in 45 degree steps
        // base yaw = the rotation manager's current target rotation, else the player's yaw
        float f1 = RenderRotationTracker.INSTANCE.rotation != null
                ? RenderRotationTracker.INSTANCE.rotation.x
                : MinecraftInstance.mc.thePlayer.rotationYaw;
        List<Float> yaws = new ArrayList<>(8);
        yaws.add(f1 + 0.0F);
        yaws.add(f1 + 45.0F);
        yaws.add(f1 + 90.0F);
        yaws.add(f1 + 135.0F);
        yaws.add(f1 + 180.0F);
        yaws.add(f1 + 225.0F);
        yaws.add(f1 + 270.0F);
        yaws.add(f1 + 315.0F);

        float moveYaw = MovementUtil.getDirection();
        boolean moveDiagonal = RotationUtil.isDiagonalYaw(moveYaw);
        boolean f1Diagonal = RotationUtil.isDiagonalYaw(f1);
        float preferredYaw = this.yawOffset != null ? f1 + this.yawOffset : current.x;
        // deviation of the movement direction from the nearest multiple of 90 degrees
        float axisDeviation = MathHelper.wrapAngleTo180_float(moveYaw - Math.round(moveYaw / 90.0F) * 90.0F);
        float backward = moveYaw + 180.0F;
        float backwardDelta = MathHelper.wrapAngleTo180_float(backward - f1);
        float absDeviation = Math.abs(axisDeviation);
        // yaw to aim at when moving diagonally (nearest "odd" 45 degree direction of the backwards direction)
        float diagonalTarget = absDeviation <= 1.0F
                ? preferredYaw
                : Math.round((backward - 45.0F) / 90.0F) * 90.0F + 45.0F;
        // yaw (multiple of 45 degrees relative to f1) closest to the backwards direction
        float nearest45 = Math.round(backwardDelta / 45.0F) * 45.0F + f1;
        boolean notF1Diagonal = !f1Diagonal;

        final float preferredYawF = preferredYaw;
        final float nearest45F = nearest45;
        final float f1F = f1;
        final boolean moveDiagonalF = moveDiagonal;
        final float diagonalTargetF = diagonalTarget;
        Comparator<Float> order = Comparator
                .<Float>comparingDouble(yaw -> {
                    if (strict && this.yawOffset != null) {
                        return Math.abs(MathHelper.wrapAngleTo180_float(preferredYawF - yaw));
                    }
                    return 0.0;
                })
                .thenComparingInt(yaw -> {
                    if (moveDiagonalF && Math.abs(MathHelper.wrapAngleTo180_float(yaw - nearest45F)) < 0.01F) {
                        return -1;
                    }
                    float relative = (yaw - f1F) % 360.0F;
                    if (relative < 0.0F) {
                        relative += 360.0F;
                    }
                    boolean oddMultiple = angularDistance(relative, 45.0F) <= 22.5F
                            || angularDistance(relative, 135.0F) <= 22.5F
                            || angularDistance(relative, 225.0F) <= 22.5F
                            || angularDistance(relative, 315.0F) <= 22.5F;
                    return oddMultiple == notF1Diagonal ? 0 : 1;
                })
                .thenComparingDouble(yaw -> Math.abs(MathHelper.wrapAngleTo180_float(diagonalTargetF - yaw)))
                .thenComparingDouble(yaw -> Math.abs(MathHelper.wrapAngleTo180_float(preferredYawF - yaw)));
        yaws.sort(order);

        // ---- 3. clickable faces next to the candidates: (collision box of the neighbouring block, face pointing at the candidate)
        List<FaceBox> faces = new ArrayList<>();
        for (BlockPos candidate : candidates) {
            for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                BlockPos neighbour = candidate.offset(facing);
                IBlockState state = MinecraftInstance.mc.theWorld.getBlockState(neighbour);
                Block block = state.getBlock();
                AxisAlignedBB box = block.getCollisionBoundingBox(MinecraftInstance.mc.theWorld, neighbour, state);
                if (box == null) {
                    continue;
                }
                faces.add(new FaceBox(box, facing.getOpposite()));
            }
        }

        // ---- 4. adjust the yaw list to the movement direction
        if (absDeviation > 7.5F) {
            // moving diagonally
            if (strict && this.yawOffset != null) {
                yaws.removeIf(yaw -> Math.abs(MathHelper.wrapAngleTo180_float(yaw - nearest45F)) < 0.01F);
                yaws.add(0, nearest45);
            }
        } else {
            yaws.removeIf(yaw -> Math.abs(MathHelper.wrapAngleTo180_float(yaw - nearest45F)) < 0.01F);
        }

        float yawDelta = MathHelper.wrapAngleTo180_float(preferredYaw - f1);
        List<FaceBox> facesForPitch = null;
        if (this.yawOffset != null && !strict) {
            // collapse the list to a single preferred yaw
            float chosen;
            if (!moveDiagonal) {
                float snapped = Math.round(yawDelta / 45.0F) * 45.0F + f1;
                boolean useSnapped = false;
                if (absDeviation > 7.5F
                        || Math.abs(MathHelper.wrapAngleTo180_float(snapped - nearest45)) >= 0.01F) {
                    useSnapped = RotationUtil.isDiagonalYaw(snapped - f1) == notF1Diagonal;
                }
                chosen = useSnapped ? snapped : yaws.get(0);
            } else {
                chosen = nearest45;
            }
            yaws.clear();
            yaws.add(chosen);
        }

        // ---- 5. choose the placement
        PlacementTarget result = null;
        float chosenYaw = 0.0F;
        boolean fromCurrent = false;
        boolean tryCandidates = false;
        facesForPitch = new ArrayList<>(faces);

        if (strict && this.yawOffset != null) {
            // keep the current yaw if it is still within 45 degrees of the preferred one
            float diff = MathHelper.wrapAngleTo180_float(preferredYaw - current.x);
            if (45.0F > Math.abs(diff)) {
                result = this.solvePitch(player, candidates, facesForPitch, current, rotationFn, current.x);
                if (result != null) {
                    fromCurrent = true;
                    chosenYaw = current.x;
                } else {
                    tryCandidates = true;
                }
            } else {
                tryCandidates = true;
            }
        }

        if (result == null && tryCandidates) {
            List<RotationCandidate> rotations = new ArrayList<>();
            for (BlockPos candidate : candidates) {
                rotations.addAll(this.geometry.findPlacementCandidates(player, candidate));
            }
            final float preferredYawCandidate = preferredYaw;
            rotations.removeIf(c -> Math.abs(MathHelper.wrapAngleTo180_float(preferredYawCandidate - c.rotation.x)) >= 45.0F);
            rotations.sort(Comparator
                    .<RotationCandidate>comparingDouble(c -> Math.abs(MathHelper.wrapAngleTo180_float(current.x - c.rotation.x)))
                    .thenComparingDouble(c -> Math.abs(current.y - c.rotation.y)));
            for (RotationCandidate candidate : rotations) {
                BlockPlacement ray = this.geometry.raycastPlacement(player, candidate.rotation, candidates, rotationFn);
                if (ray != null && ray.facing != EnumFacing.UP && ray.facing != EnumFacing.DOWN) {
                    float playerYaw = player.rotationYaw;
                    result = new PlacementTarget(ray.pos, ray.facing, ray.hitVec, ray.angles,
                            (double) MovementUtil.getDirection(playerYaw));
                    fromCurrent = true;
                    chosenYaw = candidate.rotation.x;
                    break;
                }
            }
        }

        if (result == null) {
            // try every yaw of the (sorted) list
            for (Float yaw : yaws) {
                result = this.solvePitch(player, candidates, facesForPitch, current, rotationFn, yaw);
                if (result != null) {
                    fromCurrent = false;
                    chosenYaw = yaw;
                    break;
                }
            }
        }
        if (result == null) {
            return null;
        }

        // ---- 6. post processing
        if (rotationFn == null && strict && this.yawOffset != null) {
            if (maxBlocks > this.module.simulateRotationTicks(result.rotation) + this.module.settings.getExtend()) {
                return null;
            }
        }
        float difference = MathHelper.wrapAngleTo180_float(preferredYaw - chosenYaw);
        boolean farFromPreferred = Math.abs(difference) > 0.01F && 45.0F > Math.abs(difference);
        if (this.yawOffset == null) {
            farFromPreferred = false;
        }
        if (farFromPreferred || fromCurrent) {
            return result;
        }
        if (!moveDiagonal && this.yawOffset != null
                && Math.abs(MathHelper.wrapAngleTo180_float(chosenYaw - nearest45)) < 0.01F) {
            return result;
        }
        // remember the offset of the chosen yaw to the base yaw
        this.yawOffset = MathHelper.wrapAngleTo180_float(chosenYaw - f1);
        return result;
    }

    /**
     * Finds where along {@code path} (predicted positions of the player) the player would leave the ground, collects the
     * candidate blocks that cover that spot and solves a placement for them with {@link #solve}.
     * <p>
     * Called from {@link PlacementPredictor#a} with {@code strict} = "second pass".
     *
     * @param blocks list of candidate blocks (immutable list)
     * @param path   predicted positions, one per tick ({@code Vec3}-like objects)
     */
    public PlacementTarget solveAlongPath(EntityPlayer player, List<BlockPos> blocks, List<Vec3> path, Vec2f current,
                                          boolean strict, int maxBlocks) {
        int floorY = (int) Math.floor(MinecraftInstance.mc.thePlayer.posY);
        int belowY = floorY - 1;

        // first predicted position without ground below it
        AxisAlignedBB gap = null;
        for (Vec3 position : path) {
            AxisAlignedBB footprint = new AxisAlignedBB(position.xCoord - 0.3F, position.yCoord, position.zCoord - 0.3F,
                    position.xCoord + 0.3F, position.yCoord + 1.8F, position.zCoord + 0.3F);
            gap = new AxisAlignedBB(footprint.minX, (double) belowY + 0.99, footprint.minZ,
                    footprint.maxX, (double) belowY + 1.0, footprint.maxZ);
            if (!BlockUtil.isColliding(gap)) {
                break;
            }
            gap = null; // (the loop only ends normally if every position still has ground below it)
        }
        if (gap == null) {
            return null;
        }

        // blocks whose cell overlaps the gap
        List<BlockPos> nearby = new ArrayList<>();
        for (BlockPos pos : blocks) {
            AxisAlignedBB cell = blockBox(pos);
            if (gap.intersectsWith(cell)) {
                nearby.add(pos);
            }
        }
        if (nearby.isEmpty()) {
            for (BlockPos pos : blocks) {
                if (pos.getY() != belowY) {
                    continue;
                }
                IBlockState state = MinecraftInstance.mc.theWorld.getBlockState(pos);
                Block block = state.getBlock();
                if (block.getCollisionBoundingBox(MinecraftInstance.mc.theWorld, pos, state) != null) {
                    continue;
                }
                if (!BlockUtil.canReplace(block, MinecraftInstance.mc.theWorld, pos)) {
                    continue;
                }
                for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                    BlockPos shifted = pos.offset(facing);
                    AxisAlignedBB cell = blockBox(shifted);
                    if (!gap.intersectsWith(cell)) {                              // the six comparisons against the gap box
                        continue;
                    }
                    IBlockState shiftedState = MinecraftInstance.mc.theWorld.getBlockState(shifted);
                    if (BlockUtil.canReplace(shiftedState.getBlock(), MinecraftInstance.mc.theWorld, shifted)) {
                        nearby.add(pos);
                        break;
                    }
                }
            }
        }
        return this.solve(player, nearby, current, strict, maxBlocks, null);
    }

    /**
     * Pitch solver for a fixed yaw: for every clickable face the pitch interval whose ray (at that yaw) hits the face from
     * the player's eye is computed; candidate pitches are tried closest-to-{@code current.y} first and the first one
     * that yields a valid raycast result is returned.
     */
    private PlacementTarget solvePitch(EntityPlayer player, Collection<BlockPos> positions, List<FaceBox> faces,
                                       Vec2f current, UnaryOperator<Vec2f> rotationFn, float yaw) {
        float yawDelta = MathHelper.wrapAngleTo180_float(yaw - MinecraftInstance.mc.thePlayer.rotationYaw);
        float basePitch = MathHelper.clamp_float(current.y, 0.0F, 90.0F);
        // 5 blocks in creative, 4.5 otherwise
        double reach = MinecraftInstance.mc.playerController.getCurrentGameType() == WorldSettings.GameType.CREATIVE ? 5.0 : 4.5;
        float lookYaw = yawDelta + MinecraftInstance.mc.thePlayer.rotationYaw;
        double playerX = player.posX;
        double eyeY = player.posY + player.getEyeHeight();
        double playerZ = player.posZ;
        Vec3 look = player.getVectorForRotation(0.0F, lookYaw);
        double dx = reach * look.xCoord;
        double dy = reach * look.yCoord;
        double dz = reach * look.zCoord;

        FloatArrayList pitches = new FloatArrayList(new float[]{basePitch});
        for (FaceBox face : faces) {
            AxisAlignedBB box = face.boundingBox();   // record component: boundingBox
            double centerY = (box.minY + box.maxY) * 0.5;
            Vec3 start = new Vec3(playerX, centerY, playerZ);
            Vec3 end = new Vec3(playerX + dx, centerY + dy, playerZ + dz);
            MovingObjectPosition hit = box.calculateIntercept(start, end);
            if (hit == null || hit.sideHit != face.facing()) {   // record component: facing
                continue;
            }
            float distF = (float) Math.sqrt((hit.hitVec.xCoord - playerX) * (hit.hitVec.xCoord - playerX)
                    + (hit.hitVec.zCoord - playerZ) * (hit.hitVec.zCoord - playerZ));
            if (0.0F >= distF || distF >= (float) reach) {
                continue;
            }
            double dist = distF;
            float lo = Math.max(0.0F, (float) (Math.atan2(eyeY - box.maxY, dist) * 57.29577951308232D));
            float upperByBox = (float) (Math.atan2(eyeY - box.minY, dist) * 57.29577951308232D);
            float upperByReach = (float) (Math.acos(dist / reach) * 57.29577951308232D);
            float hi = Math.min(upperByBox, upperByReach);
            if (lo >= hi) {
                continue;
            }
            float margin = Math.min((hi - lo) * 0.1F, 0.05F);
            float hiInner = hi - margin;
            float loInner = lo + margin;
            // the pitch closest to the current one that lies inside the interval
            float first;
            if (loInner > basePitch) {
                first = loInner;
            } else if (basePitch > hiInner) {
                first = hiInner;
            } else {
                first = basePitch;
            }
            pitches.add(first);
            // plus 9 evenly spaced samples of the interval
            float span = hiInner - loInner;
            pitches.add(span * 0.0F / 8.0F + loInner);
            pitches.add(span / 8.0F + loInner);
            pitches.add(span * 2.0F / 8.0F + loInner);
            pitches.add(span * 3.0F / 8.0F + loInner);
            pitches.add(span * 4.0F / 8.0F + loInner);
            pitches.add(span * 5.0F / 8.0F + loInner);
            pitches.add(span * 6.0F / 8.0F + loInner);
            pitches.add(span * 7.0F / 8.0F + loInner);
            pitches.add(span * 8.0F / 8.0F + loInner);
        }

        pitches.sort((FloatComparator) (a, b) -> Float.compare(Math.abs(a - basePitch), Math.abs(b - basePitch)));

        for (int i = 0; i < pitches.size(); i++) {
            Vec2f rotation = new Vec2f(lookYaw, pitches.getFloat(i));
            BlockPlacement ray = this.geometry.raycastPlacement(player, rotation, positions, rotationFn);
            if (ray != null && ray.facing != EnumFacing.UP && ray.facing != EnumFacing.DOWN) {
                float playerYaw = player.rotationYaw;
                return new PlacementTarget(ray.pos, ray.facing, ray.hitVec, ray.angles,
                        (double) MovementUtil.getDirection(playerYaw));
            }
        }
        return null;
    }

    private static AxisAlignedBB blockBox(BlockPos pos) {
        return new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0, pos.getY() + 1.0, pos.getZ() + 1.0);
    }

    /** Smaller of {@code |a - b|} and {@code 360 - |a - b|}. */
    private static float angularDistance(float a, float b) {
        float d = Math.abs(a - b);
        return Math.min(d, 360.0F - d);
    }
}
