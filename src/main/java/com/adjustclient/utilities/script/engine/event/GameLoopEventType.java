package com.adjustclient.utilities.script.engine.event;

import com.adjustclient.core.events.GameLoopEvent;

public final class GameLoopEventType extends ScriptEventType<GameLoopEvent> {
    public static final GameLoopEventType INSTANCE;

    static {
        INSTANCE = new GameLoopEventType();
    }

    private GameLoopEventType() {
        super("GameLoopEvent", null, GameLoopEvent.class);
    }
}
