package com.adjustclient.core.events;

public final class KeyInputEvent extends CancellableEvent {
    public boolean down;
    public char character;
    public int keyCode;

    public KeyInputEvent(char character, int keyCode, boolean down) {
        this.character = character;
        this.keyCode = keyCode;
        this.down = down;
    }
}
