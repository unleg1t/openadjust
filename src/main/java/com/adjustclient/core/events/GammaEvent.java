package com.adjustclient.core.events;

public final class GammaEvent extends Event {
    public float gamma;

    public GammaEvent(float gamma) {
        this.gamma = gamma;
    }
}
