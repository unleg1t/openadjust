package com.adjustclient.utilities.script.engine.event;

import com.adjustclient.core.events.Listener;
import com.adjustclient.utilities.script.engine.ScriptManager;

public final class ScriptEventBridge implements Listener {
    public ScriptManager manager;

    public ScriptEventBridge(ScriptManager manager) {
        this.manager = manager;
    }
}
