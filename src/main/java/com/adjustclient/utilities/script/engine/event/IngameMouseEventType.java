package com.adjustclient.utilities.script.engine.event;

import com.adjustclient.core.events.IngameActionEvent;

public final class IngameMouseEventType extends ScriptEventType<IngameActionEvent> {
    public static final IngameMouseEventType INSTANCE;

    static {
        INSTANCE = new IngameMouseEventType();
    }

    private IngameMouseEventType() {
        super("IngameMouseEvent", null, IngameActionEvent.class);
    }
}
