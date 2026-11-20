package com.adjustclient.core.events;

public final class KeyBindingUpdateEvent extends CancellableEvent {
    public int keyCode;

    public KeyBindingUpdateEvent(int keyCode) {
        this.keyCode = keyCode;
    }
}
