package com.adjustclient.module.impl.player.inventory.action;
import com.adjustclient.module.impl.player.inventory.click.InventoryClicker;
import com.adjustclient.module.impl.player.inventory.plan.InventorySlot;
public final class HotbarSwapAction extends InventoryAction {
    private final InventorySlot slot;
    private final int hotbarSlot;
    public HotbarSwapAction(InventorySlot slot, int hotbarSlot) {
        this.slot = slot;
        this.hotbarSlot = hotbarSlot;
    }
    @Override public void execute(InventoryClicker clicker) {
        java.util.Objects.requireNonNull(clicker.lastPosition);
        int slotId = slot.slotId;
        if (slotId < 0 && slotId != -999) return;
        clicker.clicks.add(new com.adjustclient.module.impl.player.inventory.click.WindowClick(
                slot.windowId, slotId, hotbarSlot, 2));
    }
}
