package com.adjustclient.core.events;

public final class BoatLookEvent extends Event {
    public float yaw;
    public float pitch;

    public BoatLookEvent(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }
}
