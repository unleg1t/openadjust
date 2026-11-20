package com.adjustclient.core.events;

public final class SafeWalkEvent extends Event {
    public boolean safeWalk;

    public SafeWalkEvent(boolean safeWalk) {
        this.safeWalk = safeWalk;
    }
}
