package com.adjustclient.event;

public final class MouseInputGateEvent extends Event {
    public boolean forceMouseInput;

    public MouseInputGateEvent(boolean forceMouseInput) {
        this.forceMouseInput = forceMouseInput;
    }
}
