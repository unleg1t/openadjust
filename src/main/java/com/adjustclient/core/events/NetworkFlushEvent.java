package com.adjustclient.core.events;

public final class NetworkFlushEvent extends Event {
    public boolean flushAllowed;

    public NetworkFlushEvent(boolean flushAllowed) {
        this.flushAllowed = flushAllowed;
    }
}
