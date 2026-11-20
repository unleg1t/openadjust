package com.adjustclient.modules.impl.player.inventory.plan;

import com.adjustclient.modules.impl.player.inventory.HotbarLayout;
import com.adjustclient.modules.impl.player.inventory.InventorySettings;
import com.adjustclient.modules.impl.player.inventory.click.InventoryClicker;
import com.adjustclient.modules.impl.player.inventory.item.ItemType;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;

public final class InventorySnapshot {
    public final HotbarLayout hotbarLayout;
    public final List<InventorySlot> slots = new ArrayList<>();
    public final List<InventorySlot> unassignedSlots = new ArrayList<>();
    public final List<SlotAssignment> assignments = new ArrayList<>();
    public InventoryClicker clicker;
    public final InventorySettings settings;
    public ItemKeepChecker keepChecker;
    public boolean autoArmor;

    public InventorySnapshot(HotbarLayout layout, InventorySettings settings) {
        this.hotbarLayout = layout;
        this.settings = settings;
    }

    public InventorySlot findSlot(ItemStack slotKey) {
        for (InventorySlot slot : slots)
            if (slot.itemStack == slotKey) return slot;
        return null;
    }

    public List<ItemStack> getSlots(Class<?>... inventoryTypes) {
        List<ItemStack> result = new ArrayList<>(slots.size());
        outer: for (InventorySlot slot : slots) {
            for (Class<?> type : inventoryTypes)
                if (type == slot.inventory.getClass()) continue outer;
            result.add(slot.itemStack);
        }
        return result;
    }

    public List<ItemStack> getSlots(ItemType type, Class<?>... inventoryTypes) {
        List<ItemStack> result = getSlots(inventoryTypes);
        result.removeIf(stack -> !type.matches(stack));
        return result;
    }

    public void addSlot(InventorySlot slot) {
        boolean assigned = false;
        for (SlotAssignment assignment : assignments) {
            if (slot.slotId == assignment.slotId()
                    && slot.inventory.getClass() == assignment.inventoryType()
                    && slot.itemStack.getItem() == assignment.itemType()) {
                assigned = true;
                break;
            }
        }
        if (!assigned && !slot.protectedSlot) unassignedSlots.add(slot);
        slots.add(slot);
    }
}
