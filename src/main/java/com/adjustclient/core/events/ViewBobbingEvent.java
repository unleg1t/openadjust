package com.adjustclient.core.events;

public final class ViewBobbingEvent extends CancellableEvent {
    public final Type type;

    public ViewBobbingEvent(Type type) {
        this.type = type;
    }

    public enum Type {
        ARM,
        WORLD
    }
}
