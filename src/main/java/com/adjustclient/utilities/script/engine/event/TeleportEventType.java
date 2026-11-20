package com.adjustclient.utilities.script.engine.event;

import com.adjustclient.core.events.TeleportEvent;

public final class TeleportEventType extends ScriptEventType<TeleportEvent> {
    public static final TeleportEventType INSTANCE;

    static {
        INSTANCE = new TeleportEventType();
    }

    private TeleportEventType() {
        super("TeleportEvent", CANCELLABLE_EVENT_TYPE, TeleportEvent.class);
    }
}
