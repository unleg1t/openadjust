package com.adjustclient.utilities.script.engine.event;

import com.adjustclient.core.events.AttackInputStageEvent;

public final class LeftMouseEventType extends ScriptEventType<AttackInputStageEvent> {
    public static final LeftMouseEventType INSTANCE;

    static {
        INSTANCE = new LeftMouseEventType();
    }

    private LeftMouseEventType() {
        super("LeftMouseEvent", CANCELLABLE_EVENT_TYPE, AttackInputStageEvent.class);
    }
}
