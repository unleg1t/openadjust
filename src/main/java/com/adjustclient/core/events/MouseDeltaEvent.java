package com.adjustclient.core.events;

public final class MouseDeltaEvent extends CancellableEvent {
    public boolean silent;
    public float deltaX;
    public float deltaY;
    public float sensitivity;
    public float partialTicks;

    public MouseDeltaEvent(float deltaX, float deltaY, float sensitivity, float partialTicks) {
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.sensitivity = sensitivity;
        this.partialTicks = partialTicks;
    }
}
