package com.adjustclient.core.events;

public final class MouseWheelEvent extends Event {
    public int wheelDelta;

    public MouseWheelEvent(int wheelDelta) {
        this.wheelDelta = wheelDelta;
    }
}
