package com.adjustclient.core.events;

public final class FogRgbEvent extends Event {
    public float red;
    public float green;
    public float blue;

    public FogRgbEvent(float red, float green, float blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }
}
