package com.adjustclient.module.impl.player.inventory.chest;
import com.adjustclient.module.impl.player.inventory.action.InventoryAction;
import com.adjustclient.module.impl.player.inventory.plan.InventorySlot;

public abstract class ChestLootStrategy {
    public abstract InventoryAction chooseAction(InventorySlot slot);
    public abstract boolean canClose();
}
