package com.adjustclient.script.engine.event;

import com.adjustclient.event.MotionEvent;

public final class MotionEventType extends ScriptEventType<MotionEvent> {
    public static final MotionEventType INSTANCE;

    static {
        INSTANCE = new MotionEventType();
    }

    private MotionEventType() {
        super("MotionEvent", CANCELLABLE_EVENT_TYPE, MotionEvent.class);
    }
}
