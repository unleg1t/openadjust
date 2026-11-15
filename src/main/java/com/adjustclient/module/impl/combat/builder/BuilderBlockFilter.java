package com.adjustclient.module.impl.combat.builder;

import com.adjustclient.module.impl.combat.builder.feature.beddefense.BedDefenseBlockType;
import com.adjustclient.render.animation.Animation;
import com.adjustclient.util.BlockPlacement;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public final class BuilderBlockFilter {
    public final BlockPos position;
    public EnumFacing facing;
    public final BedDefenseBlockType type;
    public Animation animation;
    public BlockPlacement cachedPlacement;
    public final int blockId;
    public boolean preferSoft;
    public boolean completed;

    public BuilderBlockFilter(BlockPos position, EnumFacing facing, BedDefenseBlockType type, int blockId) {
        this.position = position;
        this.facing = facing;
        this.type = type;
        this.blockId = blockId;
    }

    public boolean matches(ItemStack stack) {
        if (type == null || stack == null || !(stack.getItem() instanceof ItemBlock)) return false;
        Block block = ((ItemBlock) stack.getItem()).getBlock();
        if (type != BedDefenseBlockType.OTHER) return type.blocks.contains(block);
        return blockId == Block.getIdFromBlock(block);
    }

    public boolean matches(IBlockState state) {
        if (type == null) return false;
        Block block = state.getBlock();
        if (type != BedDefenseBlockType.OTHER) return type.blocks.contains(block);
        return blockId == Block.getIdFromBlock(block);
    }
}
