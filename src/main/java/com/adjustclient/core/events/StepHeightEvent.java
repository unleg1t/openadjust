package com.adjustclient.core.events;

public final class StepHeightEvent extends Event {
    public float stepHeight;
    public boolean enabled = true;

    public StepHeightEvent(float stepHeight) {
        this.stepHeight = stepHeight;
    }
}
