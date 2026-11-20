package com.adjustclient.core.events;

public final class InventoryKeyEvent extends CancellableEvent {
    public int keyCode;

    public InventoryKeyEvent(int keyCode) {
        this.keyCode = keyCode;
    }
}
