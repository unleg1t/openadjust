package com.adjustclient.modules.impl.player.inventory.plan;

import com.adjustclient.utilities.Vec2f;
import java.util.Objects;
import net.minecraft.item.ItemStack;

public final class InventorySlot {
    public final InventoryPlanner inventory;
    public final ItemStack itemStack;
    public final Vec2f position;
    public final int windowId;
    public final int slotId;
    public boolean protectedSlot;

    public InventorySlot(InventoryPlanner inventory, ItemStack itemStack, Vec2f position, int windowId, int slotId) {
        this.inventory = inventory;
        this.itemStack = itemStack;
        this.position = position;
        this.windowId = windowId;
        this.slotId = slotId;
    }

    @Override public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof InventorySlot slot)) return false;
        return Objects.equals(inventory, slot.inventory)
                && Objects.equals(itemStack, slot.itemStack)
                && Objects.equals(position, slot.position)
                && windowId == slot.windowId
                && slotId == slot.slotId;
    }

    @Override public int hashCode() {
        return Objects.hash(inventory, itemStack, position,
                windowId, slotId);
    }

    @Override public String toString() {
        return "InventorySlot[inventory=" + inventory + ", itemStack=" + itemStack
                + ", position=" + position + ", windowId=" + windowId
                + ", slotId=" + slotId + "]";
    }
}
