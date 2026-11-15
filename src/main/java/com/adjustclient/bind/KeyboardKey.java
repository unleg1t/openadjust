package com.adjustclient.bind;

import org.lwjgl.input.Keyboard;

public class KeyboardKey implements BindKey {
    public final int keyCode;
    private String field8;

    public KeyboardKey(int keyCode) { this.keyCode = keyCode; }

    @Override public String getName() {
        try {
            return Keyboard.getKeyName(keyCode);
        } catch (Exception exception) {
            return "Key" + keyCode;
        }
    }

    @Override public boolean isDown() { return Keyboard.isKeyDown(keyCode); }

    @Override public boolean equals(Object other) {
        return this == other || other != null && other.getClass() == KeyboardKey.class
                && keyCode == ((KeyboardKey) other).keyCode;
    }

    @Override public int hashCode() { return keyCode; }

    @Override public String toString() { return getName(); }
}
