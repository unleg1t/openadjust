package com.adjustclient.script.engine.event;

import com.adjustclient.event.AttackInputStageEvent;

public final class LeftMouseEventType extends ScriptEventType<AttackInputStageEvent> {
    public static final LeftMouseEventType INSTANCE;

    static {
        INSTANCE = new LeftMouseEventType();
    }

    private LeftMouseEventType() {
        super("LeftMouseEvent", CANCELLABLE_EVENT_TYPE, AttackInputStageEvent.class);
    }
}
