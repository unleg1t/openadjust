package com.adjustclient.script.engine.event;

import com.adjustclient.event.LowLevelPacketSendEvent;

public final class LowLevelPacketSendEventType extends ScriptEventType<LowLevelPacketSendEvent> {
    public static final LowLevelPacketSendEventType INSTANCE;

    static {
        INSTANCE = new LowLevelPacketSendEventType();
    }

    private LowLevelPacketSendEventType() {
        super("LowLevelPacketSendEvent", null, LowLevelPacketSendEvent.class);
    }
}
