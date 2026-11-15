package com.adjustclient.command.impl.debug;

import com.adjustclient.command.ParentCommand;
import com.adjustclient.command.SubCommand;

public class DebugAhmedSubCommand extends SubCommand {
    public Object field16;

    public DebugAhmedSubCommand(ParentCommand parent, String name) { super(parent, name); }

    @Override public void run(String label, String[] args) {
    }
}
