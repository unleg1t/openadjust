package com.adjustclient.modules.impl.player.inventory.chest;
import com.adjustclient.modules.impl.player.inventory.InventoryModule;
import com.adjustclient.modules.impl.player.inventory.action.InventoryAction;
import com.adjustclient.modules.impl.player.inventory.action.ShiftClickAction;
import com.adjustclient.modules.impl.player.inventory.plan.InventorySlot;
import com.adjustclient.modules.impl.player.inventory.plan.ItemPlanner;

public final class DefaultChestStrategy extends ChestLootStrategy {
    private final InventoryModule inventory;
    public DefaultChestStrategy(InventoryModule inventory) { this.inventory = inventory; }
    public InventoryAction chooseAction(InventorySlot slot) {
        var planner = slot.inventory;
        if (planner instanceof ItemPlanner) {
            var snapshot = planner.snapshot;
            if (!snapshot.keepChecker.isUseful(slot.itemStack)) return null;
            InventoryAction action = snapshot.hotbarLayout.chooseSwap(slot, planner);
            return action != null ? action : new ShiftClickAction(slot);
        }
        return planner.chooseAction(slot);
    }
    public boolean canClose() { return true; }
}
