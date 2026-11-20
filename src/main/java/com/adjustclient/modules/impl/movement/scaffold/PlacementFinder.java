package com.adjustclient.modules.impl.movement.scaffold;

import com.adjustclient.core.rotation.handler.RotationHandler;
import com.adjustclient.utilities.BlockPlacement;
import com.adjustclient.utilities.BlockUtil;
import com.adjustclient.utilities.MinecraftInstance;
import com.adjustclient.utilities.MovementUtil;
import com.adjustclient.utilities.Vec2f;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.function.UnaryOperator;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public final class PlacementFinder implements MinecraftInstance {

    private final ScaffoldModule module;
    public final RotationSolver partner;
    public float yaw;

    public PlacementFinder(ScaffoldModule module) {
        this.module = module;
        this.partner = new RotationSolver(module, this);
    }

    public boolean isStableYawActive() {
        return this.module.settings.isStableYaw() && this.partner.yawOffset != null;
    }

    public List<RotationCandidate> findPlacementCandidates(Entity entity, BlockPos pos) {
        World world = Minecraft.getMinecraft().theWorld;
        AxisAlignedBB box = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1.0, pos.getY() + 1.0, pos.getZ() + 1.0);
        if (!world.checkNoEntityCollision(box, Minecraft.getMinecraft().thePlayer)) {
            return new ArrayList<>();
        }
        if (BlockUtil.getCollisionBox(pos) != null) {
            return new ArrayList<>();
        }

        List<RotationCandidate> candidates = new ArrayList<>();
        for (EnumFacing facing : EnumFacing.VALUES) {
            BlockPos neighbour = pos.offset(facing);
            IBlockState state = world.getBlockState(neighbour);
            Block block = state.getBlock();
            if (!BlockUtil.canReplace(block, world, neighbour)) {
                AxisAlignedBB neighbourBox = block.getCollisionBoundingBox(world, neighbour, state);
                if (neighbourBox != null) {
                    candidates.addAll(this.sampleFace(entity, block, neighbourBox, facing));
                }
            }
        }
        return candidates;
    }

    public PlacementTarget recompute(PlacementTarget previous, UnaryOperator<Vec2f> rotationFn) {
        if (previous == null) {
            return null;
        }
        if (!this.useRaycast()) {
            return previous;
        }
        EntityPlayerSP player = mc.thePlayer;
        final Vec2f current = new Vec2f(player.rotationYaw, player.rotationPitch);
        UnaryOperator<Vec2f> filter = rotationFn != null ? rotationFn : ignored -> current;

        BlockPos target = previous.placedPos;
        if (this.isStableYawActive() && mc.thePlayer.onGround && !MovementUtil.isJumping()
                && !this.module.settings.mode.getValue().towering && !this.module.noAscendActive) {
            PlacementTarget result = this.partner.solve(mc.thePlayer, List.of(target), current, false, 0, filter);
            if (result != null) {
                return result;
            }
            return this.partner.solve(mc.thePlayer, List.of(target), current, true, 0, filter);
        }

        BlockPlacement placement = this.findBestPlacement(mc.thePlayer, target, current, filter);
        if (placement == null) {
            return null;
        }
        return new PlacementTarget(placement.pos, placement.facing, placement.hitVec,
                placement.angles, previous.yaw);
    }

    public double distanceTo(Vec3 from, BlockPos pos, boolean horizontalOnly) {
        Vec3 centre = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        double radians = Math.toRadians(this.yaw);
        Vec3 shifted = centre.addVector(-Math.sin(radians) * 0.2, 0.0, Math.cos(radians) * 0.2);
        if (!horizontalOnly) {
            return shifted.distanceTo(from);
        }
        double dx = from.xCoord - shifted.xCoord;
        double dz = from.zCoord - shifted.zCoord;
        return MathHelper.sqrt_double(dx * dx + dz * dz);
    }

    public BlockPlacement raycastPlacement(Entity entity, Vec2f rotation, Collection<BlockPos> targets,
                                           UnaryOperator<Vec2f> rotationFn) {
        Vec2f rot = rotationFn == null ? rotation : rotationFn.apply(rotation);

        EntityPlayerSP player = mc.thePlayer;
        float playerYaw = player.rotationYaw;
        float playerPitch = player.rotationPitch;
        float yawDelta = MathHelper.wrapAngleTo180_float(rot.x - playerYaw);

        float f = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
        float gcd = f * f * f * 8.0F;

        float mousePitch = (float) ((double) (((rot.y - playerPitch) + playerPitch) - playerPitch) / 0.15 / gcd);
        double newPitch = playerPitch + (double) (mousePitch * gcd) * 0.15;
        float pitch;
        if (RotationHandler.INSTANCE.field19) {
            pitch = (float) newPitch;
        } else if (newPitch < -90.0) {
            pitch = -90.0F;
        } else {
            pitch = newPitch > 90.0 ? 90.0F : (float) newPitch;
        }
        float mouseYaw = (float) ((double) ((yawDelta + playerYaw) - playerYaw) / 0.15 / gcd);
        float yaw = (float) (playerYaw + (double) (mouseYaw * gcd) * 0.15);

        float reach = mc.playerController.getBlockReachDistance();

        Vec3 eyes = entity.getPositionEyes(1.0F);
        Vec3 look = entity.getVectorForRotation(pitch, yaw);
        Vec3 end = eyes.addVector(look.xCoord * reach, look.yCoord * reach, look.zCoord * reach);
        MovingObjectPosition mop = mc.theWorld.rayTraceBlocks(eyes, end, false, false, true);

        if (mop == null || mop.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            return null;
        }
        BlockPos hitPos = mop.getBlockPos();
        BlockPos adjacent = hitPos.offset(mop.sideHit);
        if (!targets.contains(adjacent)) {
            return null;
        }
        IBlockState state = mc.theWorld.getBlockState(hitPos);
        Block block = state.getBlock();
        if (BlockUtil.canReplace(block, mc.theWorld, hitPos)) {
            return null;
        }
        if (BlockUtil.getCollisionBox(hitPos) == null) {
            return null;
        }
        BlockPlacement placement = new BlockPlacement(hitPos, mop.sideHit, mop.hitVec, rot);
        if (placement.isInvalidHitVec()) {
            return null;
        }
        return placement;
    }

    private List<RotationCandidate> sampleFace(Entity entity, Block block, AxisAlignedBB box, EnumFacing facing) {
        double minX = box.minX;
        double minY = box.minY;
        double minZ = box.minZ;
        double maxX = box.maxX;
        double maxY = box.maxY;
        double maxZ = box.maxZ;

        double random1 = Math.random() * 0.045 + 0.9;
        double random2 = Math.random() * 0.045 + 0.9;

        EnumFacing side = facing.getOpposite();

        double xHi = (maxX - minX) * random1 + minX;
        double xLo = 1.0 - random1 + minX;
        boolean xFixed = false;
        if (side == EnumFacing.EAST) {
            xFixed = true;
            xHi = maxX;
            xLo = maxX;
        }
        if (side == EnumFacing.WEST) {
            xFixed = true;
            xHi = minX;
            xLo = minX;
        }
        double yHi = (maxY - minY) * random2 + minY;
        double yLo = 1.0 - random2 + minY;
        boolean yFixed = false;
        if (side == EnumFacing.UP) {
            yFixed = true;
            yHi = maxY;
            yLo = maxY;
        }
        if (side == EnumFacing.DOWN) {
            yFixed = true;
            yHi = minY;
            yLo = minY;
        }
        double zHi = random1 * (maxZ - minZ) + minZ;
        double zLo = 1.0 - random1 + minZ;
        boolean zFixed = false;
        if (side == EnumFacing.SOUTH) {
            zFixed = true;
            zHi = maxZ;
            zLo = maxZ;
        }
        if (side == EnumFacing.NORTH) {
            zFixed = true;
            zHi = minZ;
            zLo = minZ;
        }

        int freeAxes = (zFixed ? 0 : 1) + (yFixed ? 0 : 1) + (xFixed ? 0 : 1);
        int xSteps = xFixed ? 1 : 8;
        int ySteps = yFixed ? 1 : 8;
        int zSteps = zFixed ? 1 : 8;
        double centreX = (maxX + minX) / 2.0;
        double centreY = (maxY + minY) / 2.0;
        double centreZ = (maxZ + minZ) / 2.0;

        BlockPos blockPos = new BlockPos(MathHelper.floor_double(minX), MathHelper.floor_double(minY),
                MathHelper.floor_double(minZ));

        Vec3 eyes = entity.getPositionEyes(1.0F);

        List<RotationCandidate> result = new ArrayList<>();
        for (int i = 0; i < xSteps; i++) {
            double x = xFixed ? xLo : (double) i * (xHi - xLo) / (double) (xSteps - 1) + xLo;
            for (int j = 0; j < ySteps; j++) {
                double y = yFixed ? yLo : (double) j * (yHi - yLo) / (double) (ySteps - 1) + yLo;
                for (int k = 0; k < zSteps; k++) {
                    double z = zFixed ? zLo : (zHi - zLo) * (double) k / (double) (zSteps - 1) + zLo;

                    if (freeAxes > 0) {
                        boolean nearCentre = true;
                        if (!xFixed) {
                            nearCentre = Math.abs(x - centreX) < 0.2;
                        }
                        if (!yFixed) {
                            nearCentre &= Math.abs(y - centreY) < 0.2;
                        }
                        if (!zFixed) {
                            nearCentre &= Math.abs(z - centreZ) < 0.2;
                        }
                        if (nearCentre) {
                            continue;
                        }
                    }

                    double dx = x - eyes.xCoord;
                    double dy = y - eyes.yCoord;
                    double dz = z - eyes.zCoord;
                    double horizontal = MathHelper.sqrt_double(dx * dx + dz * dz);
                    float yawDegrees = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
                    double pitchDegrees = Math.atan2(dy, horizontal) * 180.0 / Math.PI;

                    Vec3 hitVec = new Vec3(x - (double) blockPos.getX(), y - (double) blockPos.getY(), z - (double) blockPos.getZ());
                    result.add(new RotationCandidate(hitVec, blockPos, new Vec2f(yawDegrees, (float) -pitchDegrees), block));
                }
            }
        }
        return result;
    }

    public BlockPlacement findBestPlacement(Entity entity, BlockPos target, final Vec2f current,
                                            UnaryOperator<Vec2f> rotationFn) {
        List<RotationCandidate> candidates = this.findPlacementCandidates(entity, target);
        candidates.sort(Comparator.comparingDouble((RotationCandidate candidate) -> {
            float yawDelta = MathHelper.wrapAngleTo180_float(current.x - candidate.rotation.x);
            float pitchDelta = Math.abs(current.y - candidate.rotation.y);
            float absYawDelta = Math.abs(yawDelta);
            return pitchDelta * pitchDelta + absYawDelta * absYawDelta;
        }));
        for (RotationCandidate candidate : candidates) {
            BlockPlacement placement = this.raycastPlacement(entity, candidate.rotation, List.of(target), rotationFn);
            if (placement != null) {
                return placement;
            }
        }
        return null;
    }

    public boolean useRaycast() {
        if (this.module.settings.isRaycast()) {
            return true;
        }
        return this.module.settings.mode.getValue().forceRaycast();
    }

    public ToDoubleFunction<BlockPos> distanceFunction(final Vec3 eyes) {
        return pos -> this.distanceTo(eyes, pos, false);
    }

    public ToDoubleFunction<BlockPos> preferredDistanceFunction(final Vec3 eyes, final List<BlockPos> preferred) {
        return pos -> {
            double distance = this.distanceTo(eyes, pos, true);
            return preferred.contains(pos) ? distance * 0.01 : distance;
        };
    }

    public static final ToDoubleFunction<RotationCandidate> MOVEMENT_YAW_DIFFERENCE = candidate ->
            Math.abs(MathHelper.wrapAngleTo180_float(candidate.rotation.x - MovementUtil.getDirection()));
}
