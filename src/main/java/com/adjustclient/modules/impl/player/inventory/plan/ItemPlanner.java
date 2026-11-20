package com.adjustclient.modules.impl.player.inventory.plan;

import com.adjustclient.modules.impl.player.inventory.action.InventoryAction;
import com.adjustclient.modules.impl.player.inventory.action.ShiftClickAction;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

public final class ItemPlanner extends InventoryPlanner {
    public final Container container;

    public ItemPlanner(InventorySnapshot snapshot, Container container) {
        super(snapshot);
        this.container = container;
    }

    @Override public ItemStack getStack(int index) {
        net.minecraft.inventory.Slot slot = container.getSlot(index);
        return slot == null ? null : slot.getStack();
    }

    public InventoryAction chooseAction(InventorySlot slot) {
        if (slot.itemStack == null) return null;
        com.adjustclient.modules.impl.player.inventory.item.ItemType matched = null;
        for (var type : com.adjustclient.modules.impl.player.inventory.item.ItemTypes.ALL) {
            if (type.matches(slot.itemStack)) { matched = type; break; }
        }
        if (matched == null) return null;
        if (matched.keepFilter.getClass().isSynthetic()) return null;
        if (!snapshot.keepChecker.isUseful(slot.itemStack)) return null;
        InventoryAction action = snapshot.hotbarLayout.chooseSwap(slot, this);
        return action == null ? new ShiftClickAction(slot) : action;
    }

}
