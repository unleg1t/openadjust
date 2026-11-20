package com.adjustclient.core.events;

public final class PlayerFlaggedEvent extends Event {
    public final String playerName;

    public PlayerFlaggedEvent(String playerName) {
        this.playerName = playerName;
    }
}
