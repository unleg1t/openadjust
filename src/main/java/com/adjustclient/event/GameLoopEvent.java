package com.adjustclient.event;

public final class GameLoopEvent extends Event {
    public final boolean preScheduled;

    public GameLoopEvent(boolean preScheduled) {
        this.preScheduled = preScheduled;
    }
}
