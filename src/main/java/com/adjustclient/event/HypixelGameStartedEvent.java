package com.adjustclient.event;

public final class HypixelGameStartedEvent extends Event {
    public com.adjustclient.hypixel.HypixelGameState state;

    public HypixelGameStartedEvent(com.adjustclient.hypixel.HypixelGameState state) {
        this.state = state;
    }
}
