package com.adjustclient.command.impl;

import com.adjustclient.command.ParentCommand;

public class VoiceCommand extends ParentCommand {
    public final VoiceMuteSubCommand mute;
    public VoiceCommand(String name, String muteName, String... aliases) {
        super(name);
        this.field8 = "Configure voice chat.";
        this.aliases = aliases;
        mute = new VoiceMuteSubCommand(this, muteName);
        subCommands.add(mute);
    }
}
