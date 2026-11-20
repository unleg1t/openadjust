package com.adjustclient.utilities.script.engine.event;

import com.adjustclient.core.events.Render3DEvent;

public final class Render3DEventType extends ScriptEventType<Render3DEvent> {
    public static final Render3DEventType INSTANCE;

    static {
        INSTANCE = new Render3DEventType();
    }

    private Render3DEventType() {
        super("Render3DEvent", null, Render3DEvent.class);
    }
}
