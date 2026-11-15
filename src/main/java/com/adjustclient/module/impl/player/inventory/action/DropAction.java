package com.adjustclient.module.impl.player.inventory.action;
import com.adjustclient.module.impl.player.inventory.click.InventoryClicker;
import com.adjustclient.module.impl.player.inventory.plan.InventorySlot;
public final class DropAction extends InventoryAction {
    private final InventorySlot slot;
    private final boolean wholeStack;
    public DropAction(InventorySlot slot, boolean wholeStack) {
        this.slot = slot;
        this.wholeStack = wholeStack;
    }
    @Override public void execute(InventoryClicker clicker) {
        java.util.Objects.requireNonNull(clicker.lastPosition);
        int slotId = slot.slotId;
        if (slotId < 0 && slotId != -999) return;
        clicker.clicks.add(new com.adjustclient.module.impl.player.inventory.click.WindowClick(
                slot.windowId, slotId, wholeStack ? 1 : 0, 4));
    }
}
