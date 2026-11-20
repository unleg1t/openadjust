package com.adjustclient.modules.impl.render;

import com.adjustclient.Adjust;
import com.adjustclient.utilities.hypixel.HypixelGame;
import com.adjustclient.modules.Category;
import com.adjustclient.modules.Module;
import com.adjustclient.core.settings.BlocksSetting;
import com.adjustclient.core.settings.NumberSetting;
import com.adjustclient.utilities.Timer;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3i;

public final class XRayModule extends Module {
    public static boolean active;
    public static Timer toggleTimer;
    public static Set<Vec3i> offsets;
    public static NumberSetting alpha;
    public static BlocksSetting blocks;
    public static XRayModule INSTANCE;

    static {
        toggleTimer = new Timer();
        offsets = new HashSet<>(Arrays.asList(
                new Vec3i(0, -2, 0), new Vec3i(1, -1, 0), new Vec3i(0, -1, -1),
                new Vec3i(0, -1, 0), new Vec3i(0, -1, 1), new Vec3i(-1, -1, 0),
                new Vec3i(2, 0, 0), new Vec3i(0, 0, 2), new Vec3i(0, 0, -2),
                new Vec3i(-2, 0, 0), new Vec3i(1, 0, -1), new Vec3i(1, 0, 0),
                new Vec3i(1, 0, 1), new Vec3i(0, 0, -1), new Vec3i(0, 0, 1),
                new Vec3i(-1, 0, -1), new Vec3i(-1, 0, 0), new Vec3i(-1, 0, 1),
                new Vec3i(1, 1, 0), new Vec3i(0, 1, -1), new Vec3i(0, 1, 0),
                new Vec3i(0, 1, 1), new Vec3i(-1, 1, 0), new Vec3i(0, 2, 0)));
    }

    public XRayModule() {
        super("X-Ray", Category.RENDER); // Module registers "module_toggle." + name.
        INSTANCE = this;
        notifyToggle = false;
        alpha = new NumberSetting(this, "Alpha", 40, 0, 80, 1).formatter(NumberSetting.INTEGER_SUFFIX_B);
        Block[] catalog = {
            Blocks.quartz_ore, Blocks.diamond_ore, Blocks.emerald_ore, Blocks.redstone_ore, Blocks.lapis_ore, Blocks.gold_ore, Blocks.iron_ore, Blocks.coal_ore,
            Blocks.bedrock, Blocks.quartz_block, Blocks.diamond_block, Blocks.emerald_block, Blocks.redstone_block, Blocks.lapis_block, Blocks.gold_block, Blocks.iron_block,
            Blocks.coal_block, Blocks.glowstone, Blocks.trapped_chest, Blocks.ender_chest, Blocks.dispenser, Blocks.chest, Blocks.hopper, Blocks.dropper,
            Blocks.furnace, Blocks.crafting_table, Blocks.jukebox, Blocks.noteblock, Blocks.standing_sign, Blocks.standing_banner, Blocks.beacon, Blocks.enchanting_table,
            Blocks.anvil, Blocks.bed, Blocks.command_block, Blocks.cobblestone, Blocks.mossy_cobblestone, Blocks.end_portal_frame, Blocks.brewing_stand, Blocks.mob_spawner,
            Blocks.portal, Blocks.skull, Blocks.tnt, Blocks.stone_button, Blocks.wooden_button, Blocks.planks, Blocks.wooden_pressure_plate, Blocks.stone_pressure_plate,
            Blocks.heavy_weighted_pressure_plate, Blocks.light_weighted_pressure_plate, Blocks.quartz_stairs, Blocks.stone_stairs, Blocks.sandstone_stairs, Blocks.oak_stairs, Blocks.dark_oak_stairs, Blocks.spruce_stairs,
            Blocks.birch_stairs, Blocks.acacia_stairs, Blocks.jungle_stairs, Blocks.nether_brick_stairs, Blocks.stone_brick_stairs, Blocks.stained_glass_pane, Blocks.glass, Blocks.glass_pane,
            Blocks.stained_glass, Blocks.trapdoor, Blocks.iron_trapdoor, Blocks.iron_door, Blocks.oak_door, Blocks.dark_oak_door, Blocks.spruce_door, Blocks.birch_door,
            Blocks.acacia_door, Blocks.jungle_door, Blocks.ladder, Blocks.stone_slab, Blocks.stone_slab2, Blocks.wooden_slab, Blocks.torch, Blocks.lit_pumpkin,
            Blocks.wool, Blocks.sponge, Blocks.bookshelf, Blocks.brick_block, Blocks.stonebrick, Blocks.prismarine, Blocks.sea_lantern, Blocks.cobblestone_wall,
            Blocks.oak_fence, Blocks.dark_oak_fence, Blocks.spruce_fence, Blocks.birch_fence, Blocks.acacia_fence, Blocks.jungle_fence, Blocks.nether_brick_fence, Blocks.oak_fence_gate,
            Blocks.dark_oak_fence_gate, Blocks.spruce_fence_gate, Blocks.birch_fence_gate, Blocks.acacia_fence_gate, Blocks.jungle_fence_gate, Blocks.piston, Blocks.redstone_wire, Blocks.redstone_torch,
            Blocks.redstone_lamp, Blocks.unpowered_repeater, Blocks.unpowered_comparator, Blocks.rail, Blocks.golden_rail, Blocks.activator_rail, Blocks.detector_rail, Blocks.daylight_detector,
            Blocks.tripwire_hook, Blocks.water, Blocks.lava
        };
        // Default-selected 115 blocks match catalog (BlocksSetting value + defaultValue), plus 8 extra blocks in availableIds.
        IntArrayList selected = new IntArrayList();
        for (Block block : catalog) selected.add(Block.getIdFromBlock(block));
        blocks = new BlocksSetting(this, "Blocks", selected);
        blocks.availableIds.addAll(selected);
        Block[] extraAvailable = {Blocks.fire, Blocks.obsidian, Blocks.log, Blocks.cactus,
                Blocks.pumpkin, Blocks.hay_block, Blocks.clay, Blocks.nether_brick};
        for (Block block : extraAvailable) blocks.availableIds.add(Block.getIdFromBlock(block));
        blocks.addListener(() -> {
            if (mc.renderGlobal != null && isEnabled()) mc.renderGlobal.loadRenderers();
        });
    }

    public static int getAlpha() { return (int) ((alpha.getInt() / 100.0f) * 255.0f); }

    public static boolean shouldRenderBlock(Block block, BlockPos pos) {
        if (!blocks.contains(block)) return false;
        if (Adjust.gameStateTracker.state == null || Adjust.gameStateTracker.state.game != HypixelGame.UHC_CHAMPIONS) return true;
        for (Vec3i offset : offsets) {
            BlockPos adjacent = offset.getX() == 0 && offset.getY() == 0 && offset.getZ() == 0
                    ? pos : new BlockPos(pos.getX() + offset.getX(), pos.getY() + offset.getY(), pos.getZ() + offset.getZ());
            if (!mc.theWorld.isBlockLoaded(pos, false)) continue;
            Block neighbor = mc.theWorld.getBlockState(adjacent).getBlock();
            if (neighbor == Blocks.mob_spawner || !neighbor.isFullBlock() || neighbor.getMaterial().isLiquid()
                    || !neighbor.getMaterial().blocksMovement() || neighbor.isTranslucent()) return true;
        }
        return false;
    }

    @Override public void toggle(boolean enabled) {
        if (enabled && mc.theWorld == null) enabled = false;
        if (isEnabled() != enabled) {
            if (toggleTimer.lastMs + 100L > System.currentTimeMillis()) return;
            toggleTimer.reset();
        }
        super.toggle(enabled);
    }

    @Override public void onEnable() {
        active = true;
        mc.renderGlobal.loadRenderers();
    }

    @Override public void onDisable() {
        active = false;
        mc.renderGlobal.loadRenderers();
    }
}
