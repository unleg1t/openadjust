package com.adjustclient.module.impl.render.freecam.tool;

import com.adjustclient.Adjust;
import com.adjustclient.module.impl.exploit.pathfinder.PathBlockChecker;
import com.adjustclient.module.impl.render.freecam.FreecamClickType;
import com.adjustclient.util.BlockUtil;
import com.adjustclient.util.DistanceUtil;
import com.adjustclient.util.RotationUtil;
import com.adjustclient.util.Vec2f;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public final class DecimatorTool extends FreecamTool {
    public static boolean lookOnly;
    public final FreecamClickType clickType;
    public final Set<BlockPos> visited = new HashSet<>();
    public Object field24;

    public DecimatorTool(FreecamController controller, MovingObjectPosition hit, FreecamClickType clickType) {
        super(controller, hit);
        this.field12 = true;
        this.clickType = clickType;
    }

    public List<CreativePlaceTask> findBlocks(TeleportPath path, Vec3 position) {
        ArrayList<BlockPos> blocks = new ArrayList<>();
        for (int x = -6; x < 6; x++) {
            for (int y = -6; y < 6; y++) {
                for (int z = -6; z < 6; z++) {
                    BlockPos block = new BlockPos(position.xCoord + x, position.yCoord + y, position.zCoord + z);
                    if (mc.theWorld.getBlockState(block).getBlock() != Blocks.air) blocks.add(block);
                }
            }
        }
        blocks.sort(Comparator.comparingDouble(block ->
                MathHelper.sqrt_double(block.distanceSqToCenter(position.xCoord, position.yCoord, position.zCoord))));
        Vec3 eyes = position.addVector(0.0D, mc.thePlayer.getEyeHeight(), 0.0D);
        ArrayList<CreativePlaceTask> result = new ArrayList<>();
        for (BlockPos block : blocks) {
            AxisAlignedBB box = BlockUtil.getCollisionBox(block);
            if (box == null) continue;
            Vec3 closest = DistanceUtil.closestPoint(position, box);
            double reach = mc.playerController.isInCreativeMode() ? 5.0D : 4.5D;
            if (position.squareDistanceTo(closest) > reach || BlockUtil.isTorchOrSign(block)) continue;
            Vec3 center = new Vec3(block.getX() + (box.maxX - box.minX) / 2.0D,
                    block.getY() + (box.maxY - box.minY) / 2.0D,
                    block.getZ() + (box.maxZ - box.minZ) / 2.0D);
            Vec2f rotation = RotationUtil.getYawBetween(center, eyes);
            result.add(new CreativePlaceTask(block, rotation, position));
        }
        if (result.isEmpty()) return Collections.emptyList();
        Vec2f previous = path.rotation == null ? Objects.requireNonNull(result.get(0).rotation, "defaultObj") : path.rotation;
        result.sort(Comparator.comparingDouble(task ->
                RotationUtil.angularDistance(previous, task.rotation)));
        return result;
    }

    @Override public void execute(List<Packet<?>> packets, Vec3 position) {
        controller.module.camera.swingItem();
        TeleportPath path = new TeleportPath();
        ArrayList<Vec3> route = new ArrayList<>();
        if (position != null) {
            Vec3 next = clearAndFindNext(packets, position, path);
            for (int step = 0; step < 4 && clickType == FreecamClickType.EXTEND && next != null; step++) {
                packets.add(path.packet(next));
                route.add(next);
                next = clearAndFindNext(packets, next, path);
            }
        }
        if (!route.isEmpty()) {
            route.add(0, position);
            Collections.reverse(route);
            for (Vec3 point : route) packets.add(path.packet(point));
        }
    }

    public Vec3 clearAndFindNext(List<Packet<?>> packets, Vec3 position, TeleportPath path) {
        ArrayList<Vec3> candidates = new ArrayList<>();
        for (CreativePlaceTask task : findBlocks(path, position)) {
            mc.theWorld.getBlockState(task.position);
            breakBlock(packets, position, task, path);
            visited.add(task.position);
            if (++Adjust.packetBalance.balance >= 19) return null;
            Vec3 block = new Vec3(task.position);
            if (position.squareDistanceTo(block) > 2.0D) candidates.add(block);
        }
        java.util.Arrays.stream(EnumFacing.values()).forEach(face -> {
            candidates.add(position.addVector(face.getFrontOffsetX() * 2.0D,
                    (face.getFrontOffsetY() + 1.0D) * 2.0D, face.getFrontOffsetZ() * 2.0D));
        });
        Vec3 best = null;
        int maximum = 2;
        for (Vec3 candidate : candidates) {
            if (!PathBlockChecker.canTravel(position, candidate)) continue;
            ArrayList<BlockPos> nearby = new ArrayList<>();
            for (int x = -6; x < 6; x++) {
                for (int y = -6; y < 6; y++) {
                    for (int z = -6; z < 6; z++) {
                        BlockPos block = new BlockPos(candidate.xCoord + x, candidate.yCoord + y, candidate.zCoord + z);
                        if (mc.theWorld.getBlockState(block).getBlock() != Blocks.air) nearby.add(block);
                    }
                }
            }
            nearby.sort(Comparator.comparingDouble(block ->
                    MathHelper.sqrt_double(block.distanceSqToCenter(candidate.xCoord, candidate.yCoord, candidate.zCoord))));
            if (nearby.size() > maximum) {
                maximum = nearby.size();
                best = candidate;
            }
        }
        return best;
    }

    public boolean breakBlock(List<Packet<?>> packets, Vec3 position, CreativePlaceTask task, TeleportPath path) {
        boolean useLookOnly = DecimatorTool.lookOnly;
        DecimatorTool.lookOnly = !useLookOnly;
        boolean onGround = position.yCoord % 0.015625D == 0.0D;
        if (useLookOnly) {
            packets.add(new C03PacketPlayer.C05PacketPlayerLook(task.rotation.x, task.rotation.y, onGround));
        } else {
            packets.add(new C03PacketPlayer.C06PacketPlayerPosLook(position.xCoord, position.yCoord, position.zCoord,
                    task.rotation.x, task.rotation.y, onGround));
        }
        path.rotation = task.rotation;
        packets.add(new C0APacketAnimation());
        packets.add(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, task.position, task.facing));
        controller.recordBlock(task.position);
        mc.theWorld.setBlockState(task.position, Blocks.air.getDefaultState(), 3);
        return true;
    }
}
