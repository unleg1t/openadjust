package com.adjustclient.event;

public final class WebMovementEvent extends Event {
    public boolean overrideMovement;
    public double x;
    public double y;
    public double z;
    public double horizontalDamping;
    public double verticalDamping;

    public WebMovementEvent(double x, double y, double z, double horizontalDamping, double verticalDamping) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.horizontalDamping = horizontalDamping;
        this.verticalDamping = verticalDamping;
    }
}
