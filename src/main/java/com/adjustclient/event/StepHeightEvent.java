package com.adjustclient.event;

public final class StepHeightEvent extends Event {
    public float stepHeight;
    public boolean enabled = true;

    public StepHeightEvent(float stepHeight) {
        this.stepHeight = stepHeight;
    }
}
