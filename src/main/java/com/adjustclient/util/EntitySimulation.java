package com.adjustclient.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public abstract class EntitySimulation {
    public World world;
    public AxisAlignedBB boundingBox;
    public float rotationYaw;
    public double posX;
    public double posY;
    public double posZ;
    public double motionX;
    public double motionY;
    public double motionZ;
    public float rotationPitch;
    public float width;
    public float height;

    public boolean getBlockState(net.minecraft.entity.Entity entity) {
        AxisAlignedBB box = boundingBox.expand(0.0, -0.4000000059604645, 0.0).contract(0.001, 0.001, 0.001);
        int minX = MathHelper.floor_double(box.minX);
        int maxX = MathHelper.floor_double(box.maxX + 1.0);
        int minY = MathHelper.floor_double(box.minY);
        int maxY = MathHelper.floor_double(box.maxY + 1.0);
        int minZ = MathHelper.floor_double(box.minZ);
        int maxZ = MathHelper.floor_double(box.maxZ + 1.0);
        if (!world.isAreaLoaded(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ), true)) return false;
        boolean water = false;
        Vec3 flow = new Vec3(0.0, 0.0, 0.0);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = minX; x < maxX; ++x) {
            for (int y = minY; y < maxY; ++y) {
                for (int z = minZ; z < maxZ; ++z) {
                    pos.set(x, y, z);
                    IBlockState state = world.getBlockState(pos);
                    Block block = state.getBlock();
                    if (block.getMaterial() == Material.water) {
                        float surface = y + 1 - BlockLiquid.getLiquidHeightPercent(state.getValue(BlockLiquid.LEVEL));
                        if (maxY >= surface) {
                            water = true;
                            flow = block.modifyAcceleration(world, pos, entity, flow);
                        }
                    }
                }
            }
        }
        if (flow.lengthVector() > 0.0) {
            flow = flow.normalize();
            motionX += flow.xCoord * 0.014;
            motionY += flow.yCoord * 0.014;
            motionZ += flow.zCoord * 0.014;
        }
        return water;
    }
}
