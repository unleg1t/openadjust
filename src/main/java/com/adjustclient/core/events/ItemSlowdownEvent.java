package com.adjustclient.core.events;

public final class ItemSlowdownEvent extends Event {
    public float factor;
    public boolean apply;
    public boolean allowSprinting;
    public boolean ignoreSneakForSprint;

    public ItemSlowdownEvent(float factor, boolean apply) {
        this.factor = factor;
        this.apply = apply;
    }
}
