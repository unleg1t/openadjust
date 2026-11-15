package com.adjustclient.script.engine.event;

import com.adjustclient.event.LowLevelPacketReceiveEvent;

public final class LowLevelPacketReceiveEventType extends ScriptEventType<LowLevelPacketReceiveEvent> {
    public static final LowLevelPacketReceiveEventType INSTANCE;

    static {
        INSTANCE = new LowLevelPacketReceiveEventType();
    }

    private LowLevelPacketReceiveEventType() {
        super("LowLevelPacketReceiveEvent", CANCELLABLE_EVENT_TYPE, LowLevelPacketReceiveEvent.class);
    }
}
