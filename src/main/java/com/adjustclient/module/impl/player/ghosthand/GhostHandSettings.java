package com.adjustclient.module.impl.player.ghosthand;

import com.adjustclient.setting.*;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.function.BooleanSupplier;
import net.minecraft.init.Blocks;

public final class GhostHandSettings {
    public final BooleanSetting players;
    public final BooleanSetting passive;
    public final BooleanSetting hostile;
    public final BooleanSetting teammates;
    public final NumberSetting targetOpacity;
    public final NumberSetting armorOpacity;
    public final BooleanSetting blocksEnabled;
    public final BlocksSetting blocks;
    public final BooleanSetting fallbackToDefault;

    public GhostHandSettings(GhostHandModule module) {
        ExpandSetting targets = new ExpandSetting(module, "Targets");
        players = new BooleanSetting(targets, "Players", false);
        passive = new BooleanSetting(targets, "Passive", false);
        hostile = new BooleanSetting(targets, "Hostile", false);
        teammates = new BooleanSetting(targets, "Teammates", true);
        ExpandSetting opacity = new ExpandSetting(module, "Opacity");
        targetOpacity = new NumberSetting(opacity, "Target", 20, 10, 100, 1).formatter(NumberSetting.INTEGER_SUFFIX_B);
        armorOpacity = new NumberSetting(opacity, "Armor", 15, 0, 100, 1).formatter(NumberSetting.INTEGER_SUFFIX_B);
        ExpandSetting blockGroup = new ExpandSetting(module, "Blocks");
        blocksEnabled = new BooleanSetting(blockGroup, "Enabled", true, "Yes", "No");
        blockGroup.enabledPredicate =
                () -> blocksEnabled.isEnabled();
        blocks = new BlocksSetting(blockGroup, "Blocks", new IntArrayList(new int[] {54, 146, 130}));
        for (int id : new int[] {54, 146, 130}) blocks.availableIds.add(id);
        for (net.minecraft.block.Block block : new net.minecraft.block.Block[] {
                Blocks.standing_sign, Blocks.trapdoor,
                Blocks.iron_trapdoor, Blocks.iron_door,
                Blocks.oak_door, Blocks.dark_oak_door,
                Blocks.spruce_door, Blocks.birch_door,
                Blocks.acacia_door, Blocks.jungle_door,
                Blocks.lever, Blocks.stone_button,
                Blocks.wooden_button, Blocks.dispenser,
                Blocks.hopper, Blocks.dropper,
                Blocks.furnace, Blocks.crafting_table,
                Blocks.jukebox, Blocks.noteblock,
                Blocks.beacon, Blocks.enchanting_table,
                Blocks.anvil, Blocks.bed,
                Blocks.command_block}) {
            blocks.availableIds.add(net.minecraft.block.Block.getIdFromBlock(block));
        }
        fallbackToDefault = new BooleanSetting(blockGroup, "Fallback to default", true);
        fallbackToDefault.setDescription("If the cursor can't find any of the selected blocks, fallback to the actual cursor object.");
    }

    public int getTargetMask() {
        int mask = players.isEnabled() ? 2 : 0;
        if (hostile.isEnabled()) mask |= 4;
        if (passive.isEnabled()) mask |= 8;
        return mask | 0x60;
    }

    public boolean includeTeammates() { return teammates.isEnabled(); }
    public boolean isBlocksEnabled() { return blocksEnabled.isEnabled(); }
    public BlocksSetting getBlocks() { return blocks; }
    public boolean isFallbackToDefault() { return fallbackToDefault.isEnabled(); }

    public float getTargetOpacity() { return targetOpacity.getFloat(); }

    public float getArmorOpacity() { return armorOpacity.getFloat(); }
}
