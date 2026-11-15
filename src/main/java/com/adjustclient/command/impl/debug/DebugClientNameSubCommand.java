package com.adjustclient.command.impl.debug;

import com.adjustclient.command.ParentCommand;
import com.adjustclient.command.SubCommand;

public final class DebugClientNameSubCommand extends SubCommand {
    public Object field16;

    public DebugClientNameSubCommand(ParentCommand parent, String name) { super(parent, name); }

    @Override public void run(String label, String[] args) {
        String value = String.join(" ", args);
        if (value.isBlank()) {
            sendMessage(createMessage().format("Cannot be empty!"));
            return;
        }
        com.adjustclient.Adjust.NAME = value;
        sendMessage(createMessage().format("Updated client name to {}!", value));
    }
}
