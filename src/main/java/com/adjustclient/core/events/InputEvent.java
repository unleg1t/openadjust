package com.adjustclient.core.events;

public class InputEvent extends Event {
    public InputAction action;

    public InputEvent(InputAction action) {
        this.action = action;
    }
}
