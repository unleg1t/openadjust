package com.adjustclient.event;

public final class MouseButtonInputEvent extends CancellableEvent {
    public boolean down;
    public int button;

    public MouseButtonInputEvent(int button, boolean down) {
        this.button = button;
        this.down = down;
    }
}
