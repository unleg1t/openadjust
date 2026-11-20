package com.adjustclient.utilities.script.engine.event;

import com.adjustclient.core.events.MotionEvent;

public final class MotionEventType extends ScriptEventType<MotionEvent> {
    public static final MotionEventType INSTANCE;

    static {
        INSTANCE = new MotionEventType();
    }

    private MotionEventType() {
        super("MotionEvent", CANCELLABLE_EVENT_TYPE, MotionEvent.class);
    }
}
