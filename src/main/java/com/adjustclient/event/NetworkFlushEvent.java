package com.adjustclient.event;

public final class NetworkFlushEvent extends Event {
    public boolean flushAllowed;

    public NetworkFlushEvent(boolean flushAllowed) {
        this.flushAllowed = flushAllowed;
    }
}
