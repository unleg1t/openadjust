package com.adjustclient.script.engine.event;

import com.adjustclient.event.ScreenRender2DEvent;

public final class RenderScreenEventType extends ScriptEventType<ScreenRender2DEvent> {
    public static final RenderScreenEventType INSTANCE;

    static {
        INSTANCE = new RenderScreenEventType();
    }

    private RenderScreenEventType() {
        super("RenderScreenEvent", null, ScreenRender2DEvent.class);
    }
}
