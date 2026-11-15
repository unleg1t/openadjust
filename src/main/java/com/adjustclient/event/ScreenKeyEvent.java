package com.adjustclient.event;

public final class ScreenKeyEvent extends CancellableEvent {
    public boolean pressed;
    public char key;
    public int keyCode;

    public ScreenKeyEvent(boolean pressed, char key, int keyCode) {
        this.pressed = pressed; this.key = key; this.keyCode = keyCode;
    }

    public ScreenKeyEvent(boolean pressed, short key, int keyCode) {
        this(pressed, (char) key, keyCode);
    }
}
