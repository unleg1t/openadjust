package com.adjustclient.modules.impl.player.inventory.action;
import com.adjustclient.modules.impl.player.inventory.click.InventoryClicker;
import com.adjustclient.modules.impl.player.inventory.plan.InventorySlot;
public final class ShiftClickAction extends InventoryAction {
    private final InventorySlot slot;
    public ShiftClickAction(InventorySlot slot) {
        this.slot = slot;
    }
    @Override public void execute(InventoryClicker clicker) {
        java.util.Objects.requireNonNull(clicker.lastPosition);
        int slotId = slot.slotId;
        if (slotId < 0 && slotId != -999) return;
        clicker.clicks.add(new com.adjustclient.modules.impl.player.inventory.click.WindowClick(
                slot.windowId, slotId, 0, 1));
    }
}
