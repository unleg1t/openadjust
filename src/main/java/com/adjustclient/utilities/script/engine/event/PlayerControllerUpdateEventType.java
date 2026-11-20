package com.adjustclient.utilities.script.engine.event;

import com.adjustclient.core.events.ControllerUpdatePhaseEvent;

public final class PlayerControllerUpdateEventType extends ScriptEventType<ControllerUpdatePhaseEvent> {
    public static final PlayerControllerUpdateEventType INSTANCE;

    static {
        INSTANCE = new PlayerControllerUpdateEventType();
    }

    private PlayerControllerUpdateEventType() {
        super("PlayerControllerUpdateEvent", null, ControllerUpdatePhaseEvent.class);
    }
}
