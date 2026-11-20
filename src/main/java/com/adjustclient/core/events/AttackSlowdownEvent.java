package com.adjustclient.core.events;

public final class AttackSlowdownEvent extends Event {
    public double factor;

    public AttackSlowdownEvent() {
        this(0.6D);
    }

    public AttackSlowdownEvent(double factor) {
        this.factor = factor;
    }
}
