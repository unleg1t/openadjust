package com.adjustclient.script.engine.event;

import com.adjustclient.event.Listener;
import com.adjustclient.script.engine.ScriptManager;

public final class ScriptEventBridge implements Listener {
    public ScriptManager manager;

    public ScriptEventBridge(ScriptManager manager) {
        this.manager = manager;
    }
}
