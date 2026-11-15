package com.adjustclient.script.engine.event;

import com.adjustclient.event.PreClientTickEvent;

public final class ClientTickEventType extends ScriptEventType<PreClientTickEvent> {
    public static final ClientTickEventType INSTANCE;

    static {
        INSTANCE = new ClientTickEventType();
    }

    private ClientTickEventType() {
        super("ClientTickEvent", CANCELLABLE_EVENT_TYPE, PreClientTickEvent.class);
    }
}
