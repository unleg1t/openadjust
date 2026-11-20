package com.adjustclient.core.events;

public final class HypixelMapEvent extends Event {
    public com.adjustclient.utilities.hypixel.HypixelGameState state;
    public String map;

    public HypixelMapEvent(com.adjustclient.utilities.hypixel.HypixelGameState state, String map) {
        this.state = state;
        this.map = map;
    }
}
