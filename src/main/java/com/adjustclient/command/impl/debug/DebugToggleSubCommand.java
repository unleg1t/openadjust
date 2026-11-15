package com.adjustclient.command.impl.debug;

import com.adjustclient.command.ParentCommand;
import com.adjustclient.command.SubCommand;

public final class DebugToggleSubCommand extends SubCommand {
    public Object field16;

    public DebugToggleSubCommand(ParentCommand parent, String name) { super(parent, name); }

    @Override public void run(String label, String[] args) {
        com.adjustclient.logging.Logger.debug ^= true;
        sendMessage(createMessage().format("Turned debugging {}!", com.adjustclient.logging.Logger.debug ? "on" : "off"));
    }
}
