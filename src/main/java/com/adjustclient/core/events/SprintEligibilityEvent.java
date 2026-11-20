package com.adjustclient.core.events;

public final class SprintEligibilityEvent extends Event {
    public boolean sprintEligible;

    public SprintEligibilityEvent(boolean sprintEligible) {
        this.sprintEligible = sprintEligible;
    }
}
