package com.adjustclient.core.events;

public class VoiceAuthenticatedEvent extends Event {
    public net.minecraft.util.Session session;

    public VoiceAuthenticatedEvent() { }

    public VoiceAuthenticatedEvent(net.minecraft.util.Session session) {
        this.session = session;
    }
}
