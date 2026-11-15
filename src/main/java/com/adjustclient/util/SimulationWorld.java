package com.adjustclient.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public final class SimulationWorld {
    public World world;
    public Entity exploder;
    public float strength;
    public double posX;
    public double posY;
    public double posZ;
    public Object field40;
    public int field44;

    public List<Entity> getAffectedEntities() {
        float diameter = strength * 2.0F;
        int minX = MathHelper.floor_double(posX - diameter - 1.0);
        int maxX = MathHelper.floor_double(posX + diameter + 1.0);
        int minY = MathHelper.floor_double(posY - diameter - 1.0);
        int maxY = MathHelper.floor_double(posY + diameter + 1.0);
        int minZ = MathHelper.floor_double(posZ - diameter - 1.0);
        int maxZ = MathHelper.floor_double(posZ + diameter + 1.0);
        List<Entity> affected = new ArrayList<>();
        for (Entity entity : world.getEntitiesWithinAABBExcludingEntity(exploder,
                new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ))) {
            if (entity.isImmuneToExplosions()) continue;
            double dx = entity.posX - posX;
            double dy = entity.posY - posY;
            double dz = entity.posZ - posZ;
            if ((double) MathHelper.sqrt_double(dx * dx + dy * dy + dz * dz) / (double) diameter <= 1.0) {
                dy += entity.getEyeHeight();
                if (MathHelper.sqrt_double(dx * dx + dy * dy + dz * dz) != 0.0F) affected.add(entity);
            }
        }
        return affected;
    }

    public Map<BlockPos, IBlockState> getAffectedBlocks() {
        Map<BlockPos, IBlockState> affected = new HashMap<>();
        for (int ix = 0; ix < 16; ++ix) {
            float fx = (float)ix / 15.0F * 2.0F - 1.0F;
            for (int iy = 0; iy < 16; ++iy) {
                float fy = (float)iy / 15.0F * 2.0F - 1.0F;
                for (int iz = 0; iz < 16; ++iz) {
                    if (ix != 0 && ix != 15 && iy != 0 && iy != 15 && iz != 0 && iz != 15) continue;
                    float fz = (float)iz / 15.0F * 2.0F - 1.0F;
                    float length = MathHelper.sqrt_float(fx * fx + fy * fy + fz * fz);
                    double stepX = fx / length * 0.300000011920929;
                    double stepY = fy / length * 0.300000011920929;
                    double stepZ = fz / length * 0.300000011920929;
                    double x = posX, y = posY, z = posZ;
                    // Unlike World.newExplosion, the simulator has no randomized initial ray energy.
                    for (float energy = strength; energy > 0.0F; energy -= 0.22500001F) {
                        BlockPos pos = new BlockPos(x, y, z);
                        IBlockState state = world.getBlockState(pos);
                        Block block = state.getBlock();
                        if (block.getMaterial() != Material.air) energy -= (block.getExplosionResistance(exploder) + 0.3F) * 0.3F;
                        if (energy > 0.0F && !block.isPassable(world, pos)) affected.put(pos, state);
                        x += stepX;
                        y += stepY;
                        z += stepZ;
                    }
                }
            }
        }
        return affected;
    }
}
