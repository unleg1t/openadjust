package com.adjustclient.modules.impl.player.inventory.item.type;
import com.adjustclient.modules.impl.player.inventory.item.ItemType;
import com.adjustclient.modules.impl.player.inventory.item.filter.BestItemFilter;
import com.adjustclient.utilities.BlockBreakUtil;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.*;
import net.minecraft.init.Blocks;
public final class ToolItemType extends ItemType implements BestItemFilter.RankedType {
    public final ToolKind kind;
    public ToolItemType(ToolKind kind) { this.kind = kind; keepFilter = new BestItemFilter(this); }
    @Override public boolean matches(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemTool)) return false;
        for (ToolKind candidate : ToolKind.values()) {
            if (candidate.predicate.test((ItemTool) stack.getItem())) return kind == candidate;
        }
        return false;
    }
    @Override public int score(ItemType type, ItemStack stack) {
        return (int) (BlockBreakUtil.getDigSpeed(Minecraft.getMinecraft().thePlayer, kind.block, stack, true, false) * 20000.0F);
    }
    @FunctionalInterface interface ToolPredicate { boolean test(ItemTool tool); }
    public enum ToolKind {
        PICKAXE(Blocks.stone, tool -> tool instanceof ItemPickaxe),
        AXE(Blocks.log, tool -> tool instanceof ItemAxe),
        SPADE(Blocks.dirt, tool -> tool instanceof ItemSpade);
        public final Block block;
        final ToolPredicate predicate;
        ToolKind(Block block, ToolPredicate predicate) { this.block = block; this.predicate = predicate; }
    }
}
