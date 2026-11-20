package com.adjustclient.utilities.script.engine;

import com.adjustclient.utilities.vfs.NamedVfsNode;

public final class ScriptModuleNode extends NamedVfsNode<ScriptModule> {
    public Object field4;
    public Object field8;

    @Override
    public String getName() {
        return field4 != null ? String.valueOf(field4) : "";
    }
}
