package com.adjustclient.command.impl.debug;

import com.adjustclient.command.ParentCommand;
import com.adjustclient.command.SubCommand;
import com.adjustclient.changelog.Changelog;

public final class DebugVersionSubCommand extends SubCommand {
    public Object field16;

    public DebugVersionSubCommand(ParentCommand parent, String name) { super(parent, name); }

    @Override public void run(String label, String[] args) {
        sendMessage(createMessage().format("You are on version {} with build {} ({}).",
                Changelog.latest.title, Changelog.contentHash, 0));
    }
}
