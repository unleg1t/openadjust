package com.adjustclient.util;

import net.minecraft.block.*;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import java.util.*;
import java.util.function.Predicate;
import net.minecraft.util.EnumFacing;

public final class BlockUtil {
    public static final java.util.function.ToDoubleFunction<BlockPos> DISTANCE_TO_PLAYER = pos -> {
        Entity player = Minecraft.getMinecraft().thePlayer;
        return pos.distanceSq(player.posX, player.posY, player.posZ);
    };
    public static final Set<Block> INTERACTABLE_BLOCKS;
    static {
        INTERACTABLE_BLOCKS = Set.of(Blocks.enchanting_table, Blocks.dispenser, Blocks.cauldron,
                Blocks.anvil, Blocks.chest, Blocks.beacon, Blocks.brewing_stand,
                Blocks.stone_button, Blocks.wooden_button, Blocks.cake, Blocks.lever,
                Blocks.noteblock, Blocks.jukebox, Blocks.ender_chest, Blocks.trapped_chest,
                Blocks.furnace, Blocks.hopper, Blocks.unpowered_repeater, Blocks.powered_repeater,
                Blocks.unpowered_comparator, Blocks.powered_comparator, Blocks.crafting_table, Blocks.trapdoor,
                Blocks.iron_trapdoor, Blocks.command_block, Blocks.bed);
    }

    public static boolean isColliding(AxisAlignedBB box) {
        Minecraft mc = Minecraft.getMinecraft();
        List<AxisAlignedBB> collisions = new ArrayList<>(1);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = MathHelper.floor_double(box.minX); x < MathHelper.floor_double(box.maxX + 1.0); x++) {
            for (int z = MathHelper.floor_double(box.minZ); z < MathHelper.floor_double(box.maxZ + 1.0); z++) {
                for (int y = MathHelper.floor_double(box.minY) - 1; y < MathHelper.floor_double(box.maxY + 1.0); y++) {
                    pos.set(x, y, z);
                    IBlockState state = mc.theWorld.getBlockState(pos);
                    state.getBlock().addCollisionBoxesToList(mc.theWorld, pos, state, box, collisions, null);
                    if (!collisions.isEmpty()) return true;
                }
            }
        }
        return false;
    }

    public static boolean isInteractable(Block block) {
        return block instanceof BlockFenceGate || block instanceof BlockFence
                || block instanceof BlockDoor || INTERACTABLE_BLOCKS.contains(block);
    }

    public static BlockPos findBed(Predicate<BlockPos> filter) {
        Minecraft mc = Minecraft.getMinecraft();
        BlockPos origin = new BlockPos(mc.thePlayer);
        List<BlockPos> candidates = new ArrayList<>();
        for (int x = -6; x < 6; x++) {
            for (int y = -6; y < 6; y++) {
                for (int z = -6; z < 6; z++) {
                    BlockPos pos = origin.add(x, y, z);
                    if (filter.test(pos)) candidates.add(pos);
                }
            }
        }
        candidates.sort(Comparator.comparingDouble(DISTANCE_TO_PLAYER));
        return candidates.isEmpty() ? null : candidates.getFirst();
    }

    public static boolean isColliding(AxisAlignedBB box, Predicate<IBlockState> filter) {
        Minecraft mc = Minecraft.getMinecraft();
        List<AxisAlignedBB> collisions = new ArrayList<>(1);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = MathHelper.floor_double(box.minX); x < MathHelper.floor_double(box.maxX + 1.0); x++) {
            for (int z = MathHelper.floor_double(box.minZ); z < MathHelper.floor_double(box.maxZ + 1.0); z++) {
                for (int y = MathHelper.floor_double(box.minY) - 1; y < MathHelper.floor_double(box.maxY + 1.0); y++) {
                    pos.set(x, y, z);
                    IBlockState state = mc.theWorld.getBlockState(pos);
                    if (filter.test(state)) {
                        state.getBlock().addCollisionBoxesToList(mc.theWorld, pos, state, box, collisions, null);
                        if (!collisions.isEmpty()) return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean hasSameProperties(IBlockState a, IBlockState b, IProperty<?>[] properties) {
        for (IProperty<?> property : properties) {
            Object first = a.getProperties().containsKey(property) ? a.getValue(property) : null;
            Object second = b.getProperties().containsKey(property) ? b.getValue(property) : null;
            if (!Objects.equals(first, second)) return false;
        }
        return true;
    }

    public static boolean isInLiquidOrClimbable(AxisAlignedBB box) {
        Minecraft mc = Minecraft.getMinecraft();
        List<AxisAlignedBB> collisions = new ArrayList<>(1);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = MathHelper.floor_double(box.minX); x < MathHelper.floor_double(box.maxX + 1.0); x++) {
            for (int z = MathHelper.floor_double(box.minZ); z < MathHelper.floor_double(box.maxZ + 1.0); z++) {
                for (int y = MathHelper.floor_double(box.minY) - 1; y < MathHelper.floor_double(box.maxY + 1.0); y++) {
                    pos.set(x, y, z);
                    IBlockState state = mc.theWorld.getBlockState(pos);
                    Block block = state.getBlock();
                    if (y >= 0 && (block instanceof BlockLiquid || block instanceof BlockVine || block instanceof BlockLadder)) return true;
                    block.addCollisionBoxesToList(mc.theWorld, pos, state, box, collisions, null);
                    if (!collisions.isEmpty()) return true;
                }
            }
        }
        return false;
    }

    public static AxisAlignedBB getSelectedBox(BlockPos pos) {
        Minecraft mc = Minecraft.getMinecraft();
        Block block = mc.theWorld.getBlockState(pos).getBlock();
        block.setBlockBoundsBasedOnState(mc.theWorld, pos);
        return block.getSelectedBoundingBox(mc.theWorld, pos);
    }

    public static boolean hasCollision(BlockPos pos) {
        Minecraft mc = Minecraft.getMinecraft();
        IBlockState state = mc.theWorld.getBlockState(pos);
        AxisAlignedBB box = state.getBlock().getCollisionBoundingBox(mc.theWorld, pos, state);
        return box != null && box.maxY - box.minY != 0.0;
    }

    public static boolean isNotReplaceable(BlockPos pos) {
        Minecraft mc = Minecraft.getMinecraft();
        Block block = mc.theWorld.getBlockState(pos).getBlock();
        return !(block instanceof BlockAir) && !block.isReplaceable(mc.theWorld, pos);
    }

    public static boolean isTorchOrSign(BlockPos pos) {
        Block block = Minecraft.getMinecraft().theWorld.getBlockState(pos).getBlock();
        return block instanceof BlockTorch || block instanceof BlockSign;
    }

    public static Block getBlock(BlockPos pos) {
        Minecraft mc = Minecraft.getMinecraft();
        return mc.theWorld == null ? null : mc.theWorld.getBlockState(pos).getBlock();
    }

    public static boolean isOverLiquid(Entity entity) {
        Minecraft mc = Minecraft.getMinecraft();
        if (entity == null || mc.theWorld == null) return false;
        AxisAlignedBB box = entity.getEntityBoundingBox();
        int y = MathHelper.floor_double(box.minY - 0.01);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = MathHelper.floor_double(box.minX); x <= MathHelper.floor_double(box.maxX); x++) {
            for (int z = MathHelper.floor_double(box.minZ); z <= MathHelper.floor_double(box.maxZ); z++) {
                pos.set(x, y, z);
                if (mc.theWorld.getBlockState(pos).getBlock().getMaterial().isLiquid()) return true;
            }
        }
        return false;
    }

    public static BlockPos[] getBedParts(BlockPos pos) {
        IBlockState state = Minecraft.getMinecraft().theWorld.getBlockState(pos);
        EnumFacing direction = EnumFacing.NORTH;
        if (state.getBlock() instanceof BlockBed) {
            direction = state.getProperties().containsKey(BlockBed.FACING) ? state.getValue(BlockBed.FACING) : null;
            BlockBed.EnumPartType part = state.getProperties().containsKey(BlockBed.PART) ? state.getValue(BlockBed.PART) : null;
            if (part == BlockBed.EnumPartType.HEAD) direction = direction.getOpposite();
        }
        return new BlockPos[] { pos, pos.offset(direction) };
    }

    public static boolean isCollidingBelow(AxisAlignedBB box, double maxY) {
        Minecraft mc = Minecraft.getMinecraft();
        List<AxisAlignedBB> collisions = new ArrayList<>(1);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = MathHelper.floor_double(box.minX); x < MathHelper.floor_double(box.maxX + 1.0); x++) {
            for (int z = MathHelper.floor_double(box.minZ); z < MathHelper.floor_double(box.maxZ + 1.0); z++) {
                for (int y = MathHelper.floor_double(box.minY) - 1; y < MathHelper.floor_double(box.maxY + 1.0); y++) {
                    pos.set(x, y, z);
                    IBlockState state = mc.theWorld.getBlockState(pos);
                    state.getBlock().addCollisionBoxesToList(mc.theWorld, pos, state, box, collisions, null);
                    for (AxisAlignedBB collision : collisions) {
                        if (collision.maxY <= maxY) return true;
                    }
                    collisions.clear();
                }
            }
        }
        return false;
    }

    public static AxisAlignedBB getCollisionBox(BlockPos pos) {
        Minecraft mc = Minecraft.getMinecraft();
        IBlockState state = mc.theWorld.getBlockState(pos);
        return state.getBlock().getCollisionBoundingBox(mc.theWorld, pos, state);
    }

    public static BlockPos getPlacePos(BlockPos pos, EnumFacing facing) {
        Minecraft mc = Minecraft.getMinecraft();
        return canReplace(mc.theWorld.getBlockState(pos).getBlock(), mc.theWorld, pos) ? pos : pos.offset(facing);
    }

    public static boolean canReplace(Block block, net.minecraft.world.World world, BlockPos pos) {
        return block instanceof BlockLiquid || block.isReplaceable(world, pos);
    }

    public static long getBreakTimeMs(Block block, BlockPos pos, ItemStack tool) {
        if (block.getBlockHardness(Minecraft.getMinecraft().theWorld, pos) < 0.0F) return Long.MAX_VALUE;
        float damage = BlockBreakUtil.getRelativeHardness(block, pos, tool);
        if (damage == 0.0F) return Long.MAX_VALUE;
        float total = damage;
        long time = 50;
        while (total < 1.0F && time < 10000) {
            total += damage;
            time += 50;
        }
        return time;
    }
}
