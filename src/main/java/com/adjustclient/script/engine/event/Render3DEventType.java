package com.adjustclient.script.engine.event;

import com.adjustclient.event.Render3DEvent;

public final class Render3DEventType extends ScriptEventType<Render3DEvent> {
    public static final Render3DEventType INSTANCE;

    static {
        INSTANCE = new Render3DEventType();
    }

    private Render3DEventType() {
        super("Render3DEvent", null, Render3DEvent.class);
    }
}
