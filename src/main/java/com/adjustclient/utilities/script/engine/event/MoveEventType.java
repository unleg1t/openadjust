package com.adjustclient.utilities.script.engine.event;

import com.adjustclient.core.events.MoveEvent;

public final class MoveEventType extends ScriptEventType<MoveEvent> {
    public static final MoveEventType INSTANCE;

    static {
        INSTANCE = new MoveEventType();
    }

    private MoveEventType() {
        super("MoveEvent", CANCELLABLE_EVENT_TYPE, MoveEvent.class);
    }
}
