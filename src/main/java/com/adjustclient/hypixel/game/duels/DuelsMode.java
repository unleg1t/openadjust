package com.adjustclient.hypixel.game.duels;

import java.util.Arrays;

public enum DuelsMode {
    BRIDGE(java.util.List.of("Bridge Duel", "Bridge Doubles", "Bridge 3v3", "Bridge Teams"), "Bridge");

    public final java.util.List<String> aliases;
    public final String displayName;
    public Object field24;

    DuelsMode(java.util.List<String> aliases, String displayName) {
        this.aliases = aliases;
        this.displayName = displayName;
    }

    public boolean matches(String text) {
        return aliases.stream().anyMatch(
                alias -> alias.equals(text));
    }
}
