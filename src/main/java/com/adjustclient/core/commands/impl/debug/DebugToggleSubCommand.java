package com.adjustclient.core.commands.impl.debug;

import com.adjustclient.core.commands.ParentCommand;
import com.adjustclient.core.commands.SubCommand;

public final class DebugToggleSubCommand extends SubCommand {
    public Object field16;

    public DebugToggleSubCommand(ParentCommand parent, String name) { super(parent, name); }

    @Override public void run(String label, String[] args) {
        com.adjustclient.core.logging.Logger.debug ^= true;
        sendMessage(createMessage().format("Turned debugging {}!", com.adjustclient.core.logging.Logger.debug ? "on" : "off"));
    }
}
