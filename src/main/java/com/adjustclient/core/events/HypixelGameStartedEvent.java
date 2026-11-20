package com.adjustclient.core.events;

public final class HypixelGameStartedEvent extends Event {
    public com.adjustclient.utilities.hypixel.HypixelGameState state;

    public HypixelGameStartedEvent(com.adjustclient.utilities.hypixel.HypixelGameState state) {
        this.state = state;
    }
}
