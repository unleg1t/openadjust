package com.adjustclient.utilities.script.engine.event;

import com.adjustclient.core.events.KeyInputEvent;

public final class KeyboardInputEventType extends ScriptEventType<KeyInputEvent> {
    public static final KeyboardInputEventType INSTANCE;

    static {
        INSTANCE = new KeyboardInputEventType();
    }

    private KeyboardInputEventType() {
        super("KeyboardInputEvent", CANCELLABLE_EVENT_TYPE, KeyInputEvent.class);
    }
}
