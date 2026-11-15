package com.adjustclient.script.engine.event;

import com.adjustclient.event.GameLoopEvent;

public final class GameLoopEventType extends ScriptEventType<GameLoopEvent> {
    public static final GameLoopEventType INSTANCE;

    static {
        INSTANCE = new GameLoopEventType();
    }

    private GameLoopEventType() {
        super("GameLoopEvent", null, GameLoopEvent.class);
    }
}
