package com.adjustclient.event;

public final class DisconnectTeardownEvent extends Event {
    public ScreenEventPhase origin;
    public net.minecraft.util.IChatComponent reason;

    public DisconnectTeardownEvent(ScreenEventPhase origin, net.minecraft.util.IChatComponent reason) {
        this.origin = origin;
        this.reason = reason;
    }
}
