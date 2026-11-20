package com.adjustclient.core.gui.clickgui;

import com.adjustclient.utilities.MinecraftInstance;
import com.adjustclient.utilities.script.engine.ScriptManager;
public final class UnusedTab extends ClickGuiTab implements MinecraftInstance {
    private Object obf72;
    private Object obf76;
    private Object obf84;

    @Override
    public java.util.ArrayList<ClickGuiComponent> getEntries() {
        ScriptManager scriptManager = ScriptManager.INSTANCE;
        if (scriptManager == null) {
            return entries;
        }
        if (scriptManager.revision != 0) {
            throw new IllegalStateException();
        }
        return entries;
    }
}
