package com.adjustclient.module.impl.movement.scaffold;

import com.adjustclient.util.BlockPlacement;
import com.adjustclient.util.BlockUtil;
import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.util.MovementUtil;
import com.adjustclient.util.PositionSnapshot;
import com.adjustclient.util.Simulation;
import com.adjustclient.util.Vec2f;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Set;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import com.adjustclient.Adjust;
import com.adjustclient.module.impl.movement.MovementFixModule;
import com.adjustclient.module.impl.movement.speed.SpeedModule;
import com.adjustclient.util.RotationUtil;
import com.adjustclient.util.WorldUtil;

public final class PlacementPredictor {

    private final ScaffoldModule module;
    /** Placement accepted by the last successful {@link #predict} call. */
    public PlacementTarget lastResult;
    private int field12;
    private int field16;

    public PlacementPredictor(ScaffoldModule module) {
        this.module = module;
    }

    public Integer predict(EntityPlayer player, Vec2f rotation) {
        this.lastResult = null;                                         // *(this + 8) = 0
        PlacementFinder geometry = this.module.placementFinder;
        ScaffoldSettings settings = this.module.settings;
        RotationSolver solver = geometry.partner;                        // the geometry helper's partner solver

        for (int pass = 0; pass < 2; pass++) {
            boolean secondPass = pass != 0;
            Simulation master = new Simulation(player);
            master.includeSpeedModule();

            for (int tick = 0; tick < 5; tick++) {
                // the player as it will be 'tick' ticks from now
                EntityPlayer current;
                if (tick == 0) {
                    current = player;
                } else {
                    master.player.tick();
                    current = master.player;
                }

                Simulation pathSimulation = new Simulation(current);
                pathSimulation.includeSpeedModule();
                List<PositionSnapshot> path = new ArrayList<>(5);
                path.add(snapshot(pathSimulation.player));
                for (int i = 0; i < 4; i++) {
                    pathSimulation.player.tick();
                    path.add(snapshot(pathSimulation.player));
                }

                PositionSnapshot second = path.get(1);
                PositionSnapshot first = path.get(0);
                geometry.yaw = RotationUtil.getYawBetween(second, first).x;

                NoAscendMode noAscendMode = settings.noAscendMode.getValue();
                double heightAbove = current.posY - (double) this.module.baseY;      // v389
                boolean lowOnly;          // v388: only support positions at floor(posY) - 1 are considered
                boolean noAscendActive;   // v387
                boolean airborneOk;       // v386
                boolean buffering;        // v385
                boolean downward;         // v384
                if (this.module.towering) {
                    lowOnly = settings.mode.getValue() instanceof HypixelScaffoldMode;   // Hypixel mode
                    noAscendActive = false;
                    airborneOk = false;
                    buffering = false;
                    downward = false;
                } else if (!this.module.noAscendActive) {
                    lowOnly = false;
                    noAscendActive = false;
                    airborneOk = false;
                    buffering = false;
                    downward = false;
                } else if (this.module.field130) {
                    lowOnly = false;
                    noAscendActive = true;
                    airborneOk = false;
                    buffering = false;
                    downward = true;
                } else if (!this.module.isAboveBuildLimit(current)) {
                    boolean falling;
                    switch (NoAscendSwitchMap.$SwitchMap$NoAscendMode[noAscendMode.ordinal()]) {
                        case 1:   // FLAT
                            falling = false;
                            break;
                        case 2:   // BUFFER
                            falling = current.fallDistance > 0.0F;
                            break;
                        default:
                            throw new IllegalStateException();
                    }
                    boolean belowNextLevel = false;
                    if (noAscendMode == NoAscendMode.BUFFER && falling) {
                        double nextLevel = (double) (this.module.baseY + 1);
                        belowNextLevel = nextLevel > current.posY + current.motionY && !(current.posY < nextLevel);
                    }
                    lowOnly = belowNextLevel;
                    noAscendActive = true;
                    airborneOk = false;
                    buffering = falling;
                    downward = !falling;
                } else {
                    lowOnly = false;
                    noAscendActive = true;
                    airborneOk = true;
                    buffering = true;
                    downward = false;
                }

                // ---- candidate blocks around/below the player
                Vec3 position = new Vec3(current.posX, downward ? (double) this.module.baseY : current.posY, current.posZ); // v335
                BlockPos playerBlock = new BlockPos(position);                            // v334
                AxisAlignedBB playerBox = WorldUtil.playerBoundsAt(position, 1.8F);
                Set<BlockPos> supports = new LinkedHashSet<>();                           // v332: accepted neighbour positions
                PlacementTarget pendingTarget = this.module.lastPlacement;                      // last PlacementTarget (h)
                BlockPos referencePos;
                if (second.yCoord - first.yCoord >= -0.5) {
                    referencePos = null;
                } else if (pendingTarget == null || pendingTarget.placedPos == null) {
                    referencePos = null;
                } else {
                    referencePos = (double) pendingTarget.placedPos.getY() > first.yCoord ? null : pendingTarget.placedPos;
                }
                List<BlockPos> free = new ArrayList<>();           // v329
                List<BlockPos> preferred = new ArrayList<>();      // v330: positions that get a distance bonus

                // 8 x 3 x 8 box below the player: dx in [-4, 3], dy in [-3, -1], dz in [-4, 3]
                for (int dx = -4; dx < 4; dx++) {
                    for (int dy = -3; dy < 0; dy++) {
                        for (int dz = -4; dz < 4; dz++) {
                            BlockPos candidate = new BlockPos((int) Math.floor(position.xCoord + dx),
                                    (int) Math.floor(position.yCoord + dy), (int) Math.floor(position.zCoord + dz));
                            if (!BlockUtil.hasCollision(candidate)) {
                                continue;
                            }
                            free.add(candidate);
                            for (EnumFacing facing : EnumFacing.VALUES) {
                                BlockPos neighbour = candidate.offset(facing);
                                if (lowOnly && neighbour.getY() != (int) position.yCoord - 1) {
                                    continue;
                                }
                                if (facing == EnumFacing.UP) {
                                    // the first predicted position must be airborne and the neighbour must lie in the
                                    // block column of the player
                                    if (path.get(0).onGround) {          // ArrayList.getFirst
                                        continue;
                                    }
                                    if (neighbour.getX() != playerBlock.getX() || neighbour.getZ() != playerBlock.getZ()) {
                                        continue;
                                    }
                                } else if (MovementUtil.isJumping() && !MovementUtil.isMoving()
                                        && MovementUtil.getSpeed() < 0.035 && current.motionY > -0.2) {
                                    continue;
                                }
                                // direction from the neighbour's centre to the simulated position
                                double cx = neighbour.getX() + 0.5;
                                double cy = neighbour.getY() + 0.5;
                                double cz = neighbour.getZ() + 0.5;
                                double ddx = position.xCoord - cx;
                                double ddz = position.zCoord - cz;
                                double horizontal = net.minecraft.util.MathHelper.sqrt_double((cx - position.xCoord) * (cx - position.xCoord)
                                        + (cz - position.zCoord) * (cz - position.zCoord));
                                float angle = (float) (Math.atan2(ddz, ddx) * 180.0 / Math.PI) - 90.0F;   // (the pitch atan2 result is unused)
                                // pick the predicted position that corresponds to the horizontal distance to the neighbour
                                double factor = RotationUtil.isDiagonalYaw(angle) ? 1.5 : 1.0;
                                int index = (int) (horizontal * factor + 0.7);
                                index = Math.min(index, path.size() - 1);
                                PositionSnapshot expected = path.get(index);
                                if ((double) neighbour.getY() > expected.yCoord) {
                                    if (referencePos == null || !candidate.equals(referencePos)) {
                                        continue;
                                    }
                                }
                                // the support cell must not intersect the player's box
                                AxisAlignedBB cell = new AxisAlignedBB(neighbour.getX(), neighbour.getY(), neighbour.getZ(),
                                        neighbour.getX() + 1.0, neighbour.getY() + 1.0, neighbour.getZ() + 1.0);
                                if (cell.intersectsWith(playerBox)) {
                                    continue;
                                }
                                com.adjustclient.hypixel.HypixelGameState handler = Adjust.gameStateTracker.state;
                                if (handler instanceof com.adjustclient.hypixel.game.bedwars.BedWarsGameState
                                        && ((com.adjustclient.hypixel.game.bedwars.BedWarsGameState) handler).knownPositions.contains(
                                                new BlockPos(neighbour.getX(), neighbour.getY(), neighbour.getZ()))) {
                                    continue;
                                }
                                supports.add(neighbour);
                                if (referencePos != null && candidate.equals(referencePos)) {
                                    preferred.add(candidate);
                                } else if (pendingTarget != null && pendingTarget.facing != EnumFacing.UP
                                        && facing == EnumFacing.UP && candidate.equals(pendingTarget.placedPos)) {
                                    preferred.add(candidate);
                                }
                            }
                        }
                    }
                }

                List<BlockPos> sorted = supports.stream()
                        .sorted(Comparator.comparingDouble(
                                geometry.preferredDistanceFunction(position, preferred)))
                        .toList();

                // ---- stable-yaw path: let the rotation solver look at the predicted path
                if (settings.isStableYaw()                                       // p.g
                        && MovementFixModule.INSTANCE.isEnabled()                         // another module (flags & 2)
                        && settings.isRotationsEnabled()                         // p.d
                        && MinecraftInstance.mc.thePlayer.onGround
                        && !MovementUtil.isJumping()
                        && !this.module.towering
                        && !noAscendActive) {
                    PlacementTarget solved = solver.solveAlongPath(current, sorted, new ArrayList<Vec3>(path), rotation,
                            secondPass, tick);
                    if (solved == null) {
                        continue; // next tick
                    }
                    return this.accept(solved, tick);
                }

                // ---- per candidate: nearest first
                solver.yawOffset = null;
                OptionalDouble minimum = free.stream()
                        .mapToDouble(geometry.distanceFunction(position)).min();
                double minimumDistance = minimum.isPresent() ? minimum.getAsDouble() : 0.0;

                for (BlockPos pos : sorted) {
                    double distance = geometry.distanceTo(position, pos, false);
                    if (minimumDistance < distance) {
                        continue;
                    }
                    BlockPlacement placement = geometry.findBestPlacement(current, pos, rotation, null);
                    if (placement == null) {
                        continue;
                    }
                    float playerYaw = current.rotationYaw;
                    PlacementTarget target = new PlacementTarget(placement.pos, placement.facing, placement.hitVec,
                            placement.angles, (double) MovementUtil.getDirection(playerYaw));

                    EnumFacing facing = target.facing;
                    boolean markPending = false;
                    if (facing == EnumFacing.UP) {                              // UP
                        if (!buffering) {
                            ScaffoldMode scaffoldMode = settings.mode.getValue();
                            if (scaffoldMode.isAboveBuildLimit(pos.getY())) {
                                continue;
                            }
                        }
                        if (noAscendActive && !lowOnly) {
                            if (heightAbove < 1.0 || heightAbove >= 2.0) {
                                continue;
                            }
                            if (this.module.field130) {
                                continue;
                            }
                            if (!buffering) {
                                continue;
                            }
                            if (current.L()) {
                                if (!airborneOk && noAscendMode == NoAscendMode.BUFFER) {
                                    AxisAlignedBB cell = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(),
                                            pos.getX() + 1.0, pos.getY() + 1.0, pos.getZ() + 1.0);
                                    Vec3 next = new Vec3(current.posX + MinecraftInstance.mc.thePlayer.motionX,
                                            current.posY + MinecraftInstance.mc.thePlayer.motionY,
                                            current.posZ + MinecraftInstance.mc.thePlayer.motionZ);
                                    if (!cell.intersectsWith(WorldUtil.playerBoundsAt(next, 1.8F))) {
                                        continue;
                                    }
                                }
                                markPending = true;
                            }
                        } else if (lowOnly && current.L()) {
                            markPending = true;
                        }
                    } else if (!noAscendActive || downward) {
                        if (lowOnly && current.L()) {
                            markPending = true;
                        }
                    } else {
                        BlockPos below = new BlockPos(current.posX, (double) (this.module.baseY - 1), current.posZ);
                        IBlockState state = MinecraftInstance.mc.theWorld.getBlockState(below);
                        if (!com.adjustclient.util.BlockUtil.canReplace(state.getBlock(), MinecraftInstance.mc.theWorld, below)) {
                            break;
                        }
                        if (lowOnly && current.L()) {
                            markPending = true;
                        }
                    }

                    if (markPending) {
                        this.module.field130 = true;
                        boolean special = noAscendMode == NoAscendMode.BUFFER;
                        if (special && !airborneOk && !SpeedModule.INSTANCE.isEnabled()) {
                            // only every second prediction is accepted
                            this.module.field116++;
                            if (this.module.field116 >= 2) {
                                this.module.field116 = 0;
                                continue;
                            }
                        }
                    }
                    return this.accept(target, tick);
                }
            }
            if (!geometry.isStableYawActive()) {
                return null;
            }
        }
        return null;
    }

    private Integer accept(PlacementTarget target, int tick) {
        this.module.ticksSincePlace = 0;
        this.module.targetYaw = target.rotation.x;
        this.module.targetPitch = target.rotation.y;
        this.lastResult = target;
        return Integer.valueOf(tick);
    }

    private static PositionSnapshot snapshot(EntityPlayer simulated) {
        return new PositionSnapshot(simulated.onGround, simulated.posX + 0.0, simulated.posY + 0.0, simulated.posZ + 0.0);
    }
}
