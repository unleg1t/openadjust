package com.adjustclient.core.commands.impl.debug;

import com.adjustclient.core.commands.ParentCommand;
import com.adjustclient.core.commands.SubCommand;
import com.adjustclient.core.changelog.Changelog;

public final class DebugVersionSubCommand extends SubCommand {
    public Object field16;

    public DebugVersionSubCommand(ParentCommand parent, String name) { super(parent, name); }

    @Override public void run(String label, String[] args) {
        sendMessage(createMessage().format("You are on version {} with build {} ({}).",
                Changelog.latest.title, Changelog.contentHash, 0));
    }
}
