package com.adjustclient.module.impl.player.inventory.item.filter;
import com.adjustclient.module.impl.player.inventory.item.*;
import com.adjustclient.module.impl.player.inventory.plan.InventorySnapshot;
import com.adjustclient.module.impl.player.inventory.plan.ItemPlanner;
import net.minecraft.item.*;
import java.util.List;
public class SimpleItemFilter implements ItemFilter {
    public final List<Item> exemptItems;
    public final int limit;
    public SimpleItemFilter(int limit, Item... exemptItems) { this.limit = limit; this.exemptItems = List.of(exemptItems); }
    @Override public boolean isItemGood(ItemType type, ItemStack stack, InventorySnapshot snapshot) {
        if (exemptItems.contains(stack.getItem())) return true;
        int total = 0, largestCount = Integer.MIN_VALUE;
        ItemStack largest = null;
        for (ItemStack candidate : snapshot.getSlots(type, ItemPlanner.class)) {
            int count = Math.max(0, candidate.stackSize);
            total += count;
            if (count > largestCount) { largest = candidate; largestCount = count; }
        }
        return total <= limit || total - stack.stackSize <= limit || largest == stack;
    }
}
