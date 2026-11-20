package com.adjustclient.core.events;

public final class HypixelGameEndedEvent extends Event {
    public com.adjustclient.utilities.hypixel.HypixelGameState state;

    public HypixelGameEndedEvent(com.adjustclient.utilities.hypixel.HypixelGameState state) {
        this.state = state;
    }
}
