package com.adjustclient.utilities.script.engine.event;

import com.adjustclient.core.events.WorldResetEvent;

public final class PlayerCreateEventType extends ScriptEventType<WorldResetEvent> {
    public static final PlayerCreateEventType INSTANCE;

    static {
        INSTANCE = new PlayerCreateEventType();
    }

    private PlayerCreateEventType() {
        super("PlayerCreateEvent", null, WorldResetEvent.class);
    }
}
