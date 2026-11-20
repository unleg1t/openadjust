package com.adjustclient.core.events;

public final class MouseInputGateEvent extends Event {
    public boolean forceMouseInput;

    public MouseInputGateEvent(boolean forceMouseInput) {
        this.forceMouseInput = forceMouseInput;
    }
}
