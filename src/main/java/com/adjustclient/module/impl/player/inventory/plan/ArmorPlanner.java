package com.adjustclient.module.impl.player.inventory.plan;

import com.adjustclient.module.impl.player.inventory.action.*;
import com.adjustclient.module.impl.player.inventory.item.ItemType;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

public final class ArmorPlanner extends InventoryPlanner {

    public ArmorPlanner(InventorySnapshot snapshot) { super(snapshot); }

    @Override public ItemStack getStack(int index) {
        return mc.thePlayer == null ? null : mc.thePlayer.inventory.getStackInSlot(index);
    }

    public InventoryAction chooseAction(InventorySlot slot) {
        if (Integer.compareUnsigned(slot.slotId, 5) < 0) return null;
        InventorySnapshot snapshot = this.snapshot;
        ItemStack stack = slot.itemStack;
        if (snapshot.keepChecker.isUseful(stack)) {
            Integer assigned = snapshot.hotbarLayout.findAssignedSlot(slot);
            if (assigned != null) {
                ItemStack existing = getStack(assigned);
                ItemType type = snapshot.hotbarLayout.slotTypes[assigned];
                if (existing == null || !type.matches(existing)
                        || !snapshot.hotbarLayout.canReplace(snapshot, type, stack, existing)
                        || !snapshot.keepChecker.isUseful(existing))
                    return new HotbarSwapAction(slot, assigned);
            }
            if (!snapshot.autoArmor || !(stack.getItem() instanceof ItemArmor armor)) return null;
            ItemStack equipped = mc.thePlayer.inventory.armorInventory[3 - armor.armorType];
            if (equipped != null && equipped.isItemStackEqual(stack)) return null;
            if (equipped == null) return new ShiftClickAction(slot);
            InventorySlot destination = snapshot.findSlot(equipped);
            if (destination == null) return null;
            String message = "Armor 'to': " + equipped;
            return new MoveAction(slot, destination);
        }
        if (slot.slotId >= 5 && slot.slotId < 9) return null;
        return new DropAction(slot, stack.stackSize >= 2 || Math.random() >= 0.36);
    }

}
