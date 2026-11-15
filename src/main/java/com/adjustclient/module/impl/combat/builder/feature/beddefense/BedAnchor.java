package com.adjustclient.module.impl.combat.builder.feature.beddefense;

import com.adjustclient.util.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

public final class BedAnchor {
    public final BlockPos origin;
    public final int forwardX;
    public final int forwardZ;
    public final int lateralX;
    public final int lateralZ;

    public BedAnchor(BlockPos origin, int forwardX, int forwardZ) {
        this.origin = origin;
        this.forwardX = forwardX;
        this.forwardZ = forwardZ;
        lateralX = -forwardZ;
        lateralZ = forwardX;
    }

    public boolean isBedPosition(BlockPos pos) {
        return origin.equals(pos)
                || origin.add(forwardX, 0, forwardZ).equals(pos);
    }

    public static BedAnchor findNearbyBed(Vec3 origin) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) return null;
        BlockPos bed = BlockUtil.findBed(
                pos -> mc.theWorld.getBlockState(pos).getBlock() == Blocks.bed);
        return bed == null ? null : fromBed(bed, origin);
    }

    public BedDefensePlacement toPlacement(BlockPos pos, Block block) {
        int dx = pos.getX() - origin.getX();
        int dy = pos.getY() - origin.getY();
        int dz = pos.getZ() - origin.getZ();
        int longitudinal = dx * forwardX + dz * forwardZ;
        int lateral = dx * lateralX + dz * lateralZ;
        BedDefenseBlockType type = BedDefenseBlockType.OTHER;
        for (BedDefenseBlockType candidate : BedDefenseBlockType.values()) {
            if (candidate.blocks.contains(block)) {
                type = candidate;
                break;
            }
        }
        return new BedDefensePlacement(lateral, dy, longitudinal, type, Block.getIdFromBlock(block));
    }

    public static BedAnchor fromBed(BlockPos bed, Vec3 origin) {
        BlockPos[] parts = BlockUtil.getBedParts(bed);
        if (parts.length != 2) return null;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld.getBlockState(parts[0]).getBlock() != Blocks.bed
                || mc.theWorld.getBlockState(parts[1]).getBlock() != Blocks.bed) return null;
        BlockPos near = parts[0];
        BlockPos far = parts[1];
        if (near.distanceSqToCenter(origin.xCoord, origin.yCoord, origin.zCoord)
                > far.distanceSqToCenter(origin.xCoord, origin.yCoord, origin.zCoord)) {
            near = parts[1];
            far = parts[0];
        }
        return new BedAnchor(near, far.getX() - near.getX(), far.getZ() - near.getZ());
    }
}
