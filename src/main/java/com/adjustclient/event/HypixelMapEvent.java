package com.adjustclient.event;

public final class HypixelMapEvent extends Event {
    public com.adjustclient.hypixel.HypixelGameState state;
    public String map;

    public HypixelMapEvent(com.adjustclient.hypixel.HypixelGameState state, String map) {
        this.state = state;
        this.map = map;
    }
}
