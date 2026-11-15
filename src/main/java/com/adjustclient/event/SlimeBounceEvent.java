package com.adjustclient.event;

public final class SlimeBounceEvent extends Event {
    public boolean bounceAllowed;

    public SlimeBounceEvent(boolean bounceAllowed) {
        this.bounceAllowed = bounceAllowed;
    }
}
