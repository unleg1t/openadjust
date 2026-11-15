package com.adjustclient.event;

public final class SlimeSlowdownEvent extends Event {
    public float slipperiness;
    public double horizontalDamping;

    public SlimeSlowdownEvent(float slipperiness, double horizontalDamping) {
        this.slipperiness = slipperiness;
        this.horizontalDamping = horizontalDamping;
    }
}
