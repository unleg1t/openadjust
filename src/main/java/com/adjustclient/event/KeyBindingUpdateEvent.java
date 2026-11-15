package com.adjustclient.event;

public final class KeyBindingUpdateEvent extends CancellableEvent {
    public int keyCode;

    public KeyBindingUpdateEvent(int keyCode) {
        this.keyCode = keyCode;
    }
}
