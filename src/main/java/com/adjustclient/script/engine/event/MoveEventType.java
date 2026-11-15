package com.adjustclient.script.engine.event;

import com.adjustclient.event.MoveEvent;

public final class MoveEventType extends ScriptEventType<MoveEvent> {
    public static final MoveEventType INSTANCE;

    static {
        INSTANCE = new MoveEventType();
    }

    private MoveEventType() {
        super("MoveEvent", CANCELLABLE_EVENT_TYPE, MoveEvent.class);
    }
}
