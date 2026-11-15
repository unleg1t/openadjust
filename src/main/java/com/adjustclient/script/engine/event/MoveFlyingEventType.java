package com.adjustclient.script.engine.event;

import com.adjustclient.event.MoveFlyingEvent;

public final class MoveFlyingEventType extends ScriptEventType<MoveFlyingEvent> {
    public static final MoveFlyingEventType INSTANCE;

    static {
        INSTANCE = new MoveFlyingEventType();
    }

    private MoveFlyingEventType() {
        super("MoveFlyingEvent", CANCELLABLE_EVENT_TYPE, MoveFlyingEvent.class);
    }
}
