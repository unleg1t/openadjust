package com.adjustclient.event;

public final class ScreenMouseClickEvent extends CancellableEvent {
    public com.adjustclient.bind.BindMode inputAction;
    public float mouseX;
    public float mouseY;
    public int mouseButton;

    public ScreenMouseClickEvent() { }

    public ScreenMouseClickEvent(com.adjustclient.bind.BindMode inputAction, float mouseX, float mouseY, int mouseButton) {
        this.inputAction = inputAction;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.mouseButton = mouseButton;
    }
}
