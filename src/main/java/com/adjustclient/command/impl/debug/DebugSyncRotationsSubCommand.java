package com.adjustclient.command.impl.debug;

import com.adjustclient.command.ParentCommand;
import com.adjustclient.command.SubCommand;

public final class DebugSyncRotationsSubCommand extends SubCommand {
    public Object field16;

    public DebugSyncRotationsSubCommand(ParentCommand parent, String name) { super(parent, name); }

    @Override public void run(String label, String[] args) {
        com.adjustclient.rotation.handler.RenderRotationTracker.INSTANCE.rotation = null;
        sendMessage(createMessage().format("Synced rotations with server."));
    }
}
