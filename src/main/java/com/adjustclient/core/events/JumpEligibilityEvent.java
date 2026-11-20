package com.adjustclient.core.events;

public class JumpEligibilityEvent extends Event {
    public int jumpTicks;
    public boolean onGround;

    public JumpEligibilityEvent(int jumpTicks, boolean onGround) {
        this.jumpTicks = jumpTicks;
        this.onGround = onGround;
    }
}
