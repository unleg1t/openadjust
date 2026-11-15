package com.adjustclient.event;

public final class HypixelGameEndedEvent extends Event {
    public com.adjustclient.hypixel.HypixelGameState state;

    public HypixelGameEndedEvent(com.adjustclient.hypixel.HypixelGameState state) {
        this.state = state;
    }
}
