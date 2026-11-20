package com.adjustclient.core.bind;

import java.nio.charset.StandardCharsets;
import com.adjustclient.core.settings.property.KeybindManager;

public record ScrollKey(int keyCode) implements BindKey {
    @Override public String getName() { return keyCode == 1069 ? "Scroll Up" : "Scroll Down"; }

    public static ScrollKey parse(String name) {
        if (name.equalsIgnoreCase("SCROLLUP") || name.equalsIgnoreCase("WHEELUP")) return new ScrollKey(1069);
        if (name.equalsIgnoreCase("SCROLLDOWN") || name.equalsIgnoreCase("WHEELDOWN")) return new ScrollKey(1070);
        return null;
    }

    @Override public boolean isDown() {
        return keyCode == 1069 ? KeybindManager.scrollUp : KeybindManager.scrollDown;
    }

    public static ScrollKey fromWheel(int delta) {
        if (delta > 0) return new ScrollKey(1069);
        if (delta < 0) return new ScrollKey(1070);
        return null;
    }

    @Override public boolean equals(Object other) {
        return this == other || other instanceof ScrollKey key && keyCode == key.keyCode;
    }

    @Override public int hashCode() { return keyCode; }

    @Override public String toString() {
        return "g[direction=" + keyCode + "]";
    }
}
