package com.adjustclient.event;

public class JumpEligibilityEvent extends Event {
    public int jumpTicks;
    public boolean onGround;

    public JumpEligibilityEvent(int jumpTicks, boolean onGround) {
        this.jumpTicks = jumpTicks;
        this.onGround = onGround;
    }
}
