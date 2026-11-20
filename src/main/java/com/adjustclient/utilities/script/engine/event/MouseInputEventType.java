package com.adjustclient.utilities.script.engine.event;

import com.adjustclient.core.events.MouseButtonInputEvent;

public final class MouseInputEventType extends ScriptEventType<MouseButtonInputEvent> {
    public static final MouseInputEventType INSTANCE;

    static {
        INSTANCE = new MouseInputEventType();
    }

    private MouseInputEventType() {
        super("MouseInputEvent", CANCELLABLE_EVENT_TYPE, MouseButtonInputEvent.class);
    }
}
