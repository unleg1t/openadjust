package com.adjustclient.event;

public class InputEvent extends Event {
    public InputAction action;

    public InputEvent(InputAction action) {
        this.action = action;
    }
}
