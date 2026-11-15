package com.adjustclient.event;

public final class PlayerFlaggedEvent extends Event {
    public final String playerName;

    public PlayerFlaggedEvent(String playerName) {
        this.playerName = playerName;
    }
}
