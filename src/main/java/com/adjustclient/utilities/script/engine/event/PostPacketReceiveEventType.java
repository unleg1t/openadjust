package com.adjustclient.utilities.script.engine.event;

import com.adjustclient.core.events.PacketAppliedEvent;

public final class PostPacketReceiveEventType extends ScriptEventType<PacketAppliedEvent> {
    public static final PostPacketReceiveEventType INSTANCE;

    static {
        INSTANCE = new PostPacketReceiveEventType();
    }

    private PostPacketReceiveEventType() {
        super("PostPacketReceiveEvent", null, PacketAppliedEvent.class);
    }
}
