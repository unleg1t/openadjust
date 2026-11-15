package com.adjustclient.event;

public final class MotionEvent extends CancellableEvent {
    public boolean onGround;
    public boolean sprinting;
    public boolean sneaking;
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;
    public boolean pre;

    public MotionEvent(double x, double y, double z, float yaw, float pitch,
            boolean onGround, boolean sprinting, boolean sneaking, boolean pre) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
        this.sprinting = sprinting;
        this.sneaking = sneaking;
        this.pre = pre;
    }
}
