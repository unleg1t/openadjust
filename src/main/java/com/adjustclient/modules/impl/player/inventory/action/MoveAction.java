package com.adjustclient.modules.impl.player.inventory.action;
import com.adjustclient.modules.impl.player.inventory.click.InventoryClicker;
import com.adjustclient.modules.impl.player.inventory.plan.InventorySlot;

public final class MoveAction extends InventoryAction {
    public final InventorySlot source;
    public final InventorySlot destination;
    public MoveAction(InventorySlot source, InventorySlot destination) { this.source = source; this.destination = destination; }
    @Override public void execute(InventoryClicker clicker) {
        java.util.Objects.requireNonNull(clicker.lastPosition);
        if (source.slotId >= 0 || source.slotId == -999)
            clicker.clicks.add(new com.adjustclient.modules.impl.player.inventory.click.WindowClick(
                    source.windowId, source.slotId, 0, 0));
        java.util.Objects.requireNonNull(clicker.lastPosition);
        if (destination.slotId >= 0 || destination.slotId == -999)
            clicker.clicks.add(new com.adjustclient.modules.impl.player.inventory.click.WindowClick(
                    destination.windowId, destination.slotId, 0, 0));
        if (destination.itemStack == null) throw new RuntimeException("Ax");
        java.util.Objects.requireNonNull(clicker.lastPosition);
        clicker.clicks.add(new com.adjustclient.modules.impl.player.inventory.click.WindowClick(
                destination.windowId, -999, 0, 0));
    }
}
