package com.adjustclient.core.events;

public final class ItemUseDurationEvent extends Event {
    public int useDuration;

    public ItemUseDurationEvent(int useDuration) {
        this.useDuration = useDuration;
    }
}
