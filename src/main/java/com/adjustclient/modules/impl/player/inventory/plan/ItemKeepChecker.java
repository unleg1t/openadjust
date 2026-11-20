package com.adjustclient.modules.impl.player.inventory.plan;
import com.adjustclient.modules.impl.player.inventory.item.ItemType;
import com.adjustclient.modules.impl.player.inventory.item.ItemTypes;
import net.minecraft.item.ItemStack;

public final class ItemKeepChecker {
    public final InventorySnapshot snapshot;
    public ItemKeepChecker(InventorySnapshot snapshot) { this.snapshot = snapshot; }
    public boolean isUseful(ItemStack stack) {
        if (stack == null) return false;
        for (ItemType type : ItemTypes.ALL) {
            if (type.matches(stack)) return type.keepFilter.isItemGood(type, stack, snapshot);
        }
        return false;
    }
}
