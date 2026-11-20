package com.adjustclient.core.events;

import com.adjustclient.utilities.rotation.RotationAware;

public final class MoveFlyingEvent extends CancellableEvent implements RotationAware {
    public boolean pre;
    public boolean specialInputScaling;
    public float friction;
    public float rotationYaw;

    public MoveFlyingEvent(boolean pre, float friction, float rotationYaw) {
        this.pre = pre;
        this.friction = friction;
        this.rotationYaw = rotationYaw;
    }

    public boolean isPre() { return pre; }
    public boolean isPost() { return !pre; }
}
