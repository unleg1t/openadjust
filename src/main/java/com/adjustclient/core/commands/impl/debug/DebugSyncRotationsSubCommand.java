package com.adjustclient.core.commands.impl.debug;

import com.adjustclient.core.commands.ParentCommand;
import com.adjustclient.core.commands.SubCommand;

public final class DebugSyncRotationsSubCommand extends SubCommand {
    public Object field16;

    public DebugSyncRotationsSubCommand(ParentCommand parent, String name) { super(parent, name); }

    @Override public void run(String label, String[] args) {
        com.adjustclient.core.rotation.handler.RenderRotationTracker.INSTANCE.rotation = null;
        sendMessage(createMessage().format("Synced rotations with server."));
    }
}
