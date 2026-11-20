package com.adjustclient.modules.impl.player.inventory.chest;
import com.adjustclient.modules.impl.player.inventory.action.InventoryAction;
import com.adjustclient.modules.impl.player.inventory.plan.InventorySlot;

public abstract class ChestLootStrategy {
    public abstract InventoryAction chooseAction(InventorySlot slot);
    public abstract boolean canClose();
}
