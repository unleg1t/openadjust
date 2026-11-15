package com.adjustclient.module.impl.player.inventory.plan;

import net.minecraft.item.Item;

public record SlotAssignment(int slotId, Class<? extends InventoryPlanner> inventoryType, Item itemType) {
    @Override public String toString() {
        return "a[slotId=" + slotId + ", inventoryType=" + inventoryType + ", itemType=" + itemType + "]";
    }
}
