package com.adjustclient.modules.impl.player.inventory.item.filter;
import com.adjustclient.modules.impl.player.inventory.item.*;
import com.adjustclient.modules.impl.player.inventory.plan.*;
import net.minecraft.item.ItemStack;
import java.util.*;
public final class BestItemFilter implements ItemFilter {
    public final RankedType ranker;
    public BestItemFilter(RankedType ranker) { this.ranker = ranker; }
    @Override public boolean isItemGood(ItemType type, ItemStack stack, InventorySnapshot snapshot) {
        List<ItemStack> candidates = new ArrayList<>();
        for (InventorySlot slot : snapshot.slots) candidates.add(slot.itemStack);
        candidates.removeIf(candidate -> !type.matches(candidate));
        int bestScore = Integer.MIN_VALUE;
        ItemStack best = stack;
        for (ItemStack candidate : candidates) {
            InventorySlot slot = Objects.requireNonNull(snapshot.findSlot(candidate));
            int score = ranker.score(type, candidate);
            if (slot.inventory instanceof ArmorPlanner) {
                if (snapshot.hotbarLayout.isAssigned(slot)) score++;
                score++;
            }
            if (score > bestScore) { best = candidate; bestScore = score; }
        }
        return best == stack;
    }
    public interface RankedType { int score(ItemType type, ItemStack stack); }
}
