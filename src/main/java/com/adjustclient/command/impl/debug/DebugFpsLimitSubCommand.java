package com.adjustclient.command.impl.debug;

import com.adjustclient.command.ParentCommand;
import com.adjustclient.command.SubCommand;

public class DebugFpsLimitSubCommand extends SubCommand {
    public static boolean fpsLimitEnabled;
    public Object field16;

    public DebugFpsLimitSubCommand(ParentCommand parent, String name) { super(parent, name); }

    @Override public void run(String label, String[] args) {
        sendMessage(createMessage().format("Turned FPS limit {}!", fpsLimitEnabled ? "on" : "off"));
    }
}
