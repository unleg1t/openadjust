package com.adjustclient.script.engine.event;

import com.adjustclient.event.CancellablePlayerUpdateEvent;

public final class UpdateEventType extends ScriptEventType<CancellablePlayerUpdateEvent> {
    public static final UpdateEventType INSTANCE;

    static {
        INSTANCE = new UpdateEventType();
    }

    private UpdateEventType() {
        super("UpdateEvent", CANCELLABLE_EVENT_TYPE, CancellablePlayerUpdateEvent.class);
    }
}
