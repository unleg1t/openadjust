package com.adjustclient.event;

public class DebugCoordinatesEvent extends Event {
    public double x;
    public double y;
    public double z;

    public DebugCoordinatesEvent(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
