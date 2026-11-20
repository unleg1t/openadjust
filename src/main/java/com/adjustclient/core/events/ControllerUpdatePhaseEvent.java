package com.adjustclient.core.events;

public final class ControllerUpdatePhaseEvent extends Event {
    public boolean pre;

    public ControllerUpdatePhaseEvent(boolean pre) {
        this.pre = pre;
    }
}
