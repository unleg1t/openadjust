package com.adjustclient.event;

public final class SafeWalkEvent extends Event {
    public boolean safeWalk;

    public SafeWalkEvent(boolean safeWalk) {
        this.safeWalk = safeWalk;
    }
}
