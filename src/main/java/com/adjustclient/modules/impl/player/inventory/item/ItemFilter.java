package com.adjustclient.modules.impl.player.inventory.item;
import com.adjustclient.modules.impl.player.inventory.plan.InventorySnapshot;
import net.minecraft.item.ItemStack;
@FunctionalInterface
public interface ItemFilter {
    boolean isItemGood(ItemType type, ItemStack stack, InventorySnapshot snapshot);
}
