package com.adjustclient.event;

public final class SprintEligibilityEvent extends Event {
    public boolean sprintEligible;

    public SprintEligibilityEvent(boolean sprintEligible) {
        this.sprintEligible = sprintEligible;
    }
}
