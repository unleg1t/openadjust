package com.adjustclient.script.engine.event;

import com.adjustclient.event.ControllerUpdatePhaseEvent;

public final class PlayerControllerUpdateEventType extends ScriptEventType<ControllerUpdatePhaseEvent> {
    public static final PlayerControllerUpdateEventType INSTANCE;

    static {
        INSTANCE = new PlayerControllerUpdateEventType();
    }

    private PlayerControllerUpdateEventType() {
        super("PlayerControllerUpdateEvent", null, ControllerUpdatePhaseEvent.class);
    }
}
