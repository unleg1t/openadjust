package com.adjustclient.command.impl;

import com.adjustclient.Adjust;
import com.adjustclient.command.CommandExecutor;

public class ReplyCommand extends CommandExecutor {
    public Object field24;
    public ReplyCommand(String name, String... aliases) {
        super(name);
        this.field8 = "Send a private message to the last Adjust user you were messaging.";
        this.aliases = aliases;
    }

    @Override protected void run(String label, String[] args) {
        var recipient = MessageCommand.lastRecipient;
        if (recipient == null) {
            sendPersistentMessage(createMessage().format("You have nobody to reply to."));
            return;
        }
        // sendTooLittleArguments retains "Too little arguments." and the empty usage list.
        if (args.length == 0) { sendTooLittleArguments(); return; }
        String message = String.join(" ", args);
        Adjust.INSTANCE.screenStack.directMessages.send(recipient, message, createMessage());
    }
}
