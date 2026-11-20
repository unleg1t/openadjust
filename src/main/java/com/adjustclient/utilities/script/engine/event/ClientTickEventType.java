package com.adjustclient.utilities.script.engine.event;

import com.adjustclient.core.events.PreClientTickEvent;

public final class ClientTickEventType extends ScriptEventType<PreClientTickEvent> {
    public static final ClientTickEventType INSTANCE;

    static {
        INSTANCE = new ClientTickEventType();
    }

    private ClientTickEventType() {
        super("ClientTickEvent", CANCELLABLE_EVENT_TYPE, PreClientTickEvent.class);
    }
}
