package com.adjustclient.core.events;

import com.adjustclient.utilities.rotation.RotationAware;

public final class JumpMotionEvent extends Event implements RotationAware {
    public float yaw;
    public boolean sprintImpulse;
    public boolean pre;

    public JumpMotionEvent(float yaw, boolean sprintImpulse, boolean pre) {
        this.yaw = yaw;
        this.sprintImpulse = sprintImpulse;
        this.pre = pre;
    }
}
