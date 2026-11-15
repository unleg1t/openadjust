package com.adjustclient.script.engine.event;

import com.adjustclient.event.MouseButtonInputEvent;

public final class MouseInputEventType extends ScriptEventType<MouseButtonInputEvent> {
    public static final MouseInputEventType INSTANCE;

    static {
        INSTANCE = new MouseInputEventType();
    }

    private MouseInputEventType() {
        super("MouseInputEvent", CANCELLABLE_EVENT_TYPE, MouseButtonInputEvent.class);
    }
}
