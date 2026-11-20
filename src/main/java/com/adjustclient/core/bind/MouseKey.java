package com.adjustclient.core.bind;

import org.lwjgl.input.Mouse;

public class MouseKey implements BindKey {
    private static final String[] BUTTON_NAMES = { "LMB", "RMB", "MMB", "BMB", "FMB" };
    public final int button;
    private String field8;

    public MouseKey(int button) { this.button = button; }

    @Override public String getName() {
        return button >= 0 && button < 5 ? BUTTON_NAMES[button] : "MB" + button;
    }

    public static int parse(String name) {
        for (int i = 0; i < 5; i++) {
            if (name.equalsIgnoreCase(BUTTON_NAMES[i])) return i;
        }
        return -1;
    }

    @Override public boolean isDown() { return Mouse.isButtonDown(button); }

    @Override public boolean equals(Object other) {
        return this == other || other != null && other.getClass() == MouseKey.class
                && button == ((MouseKey) other).button;
    }

    @Override public int hashCode() { return 31 + Integer.hashCode(button + 150993404); }

    @Override public String toString() { return getName(); }
}
