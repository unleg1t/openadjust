package com.adjustclient.utilities.script.engine.event;

import com.adjustclient.core.events.LowLevelPacketReceiveEvent;

public final class LowLevelPacketReceiveEventType extends ScriptEventType<LowLevelPacketReceiveEvent> {
    public static final LowLevelPacketReceiveEventType INSTANCE;

    static {
        INSTANCE = new LowLevelPacketReceiveEventType();
    }

    private LowLevelPacketReceiveEventType() {
        super("LowLevelPacketReceiveEvent", CANCELLABLE_EVENT_TYPE, LowLevelPacketReceiveEvent.class);
    }
}
