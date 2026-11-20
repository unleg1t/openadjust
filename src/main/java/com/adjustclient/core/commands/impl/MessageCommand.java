package com.adjustclient.core.commands.impl;

import com.adjustclient.Adjust;
import com.adjustclient.core.commands.CommandExecutor;
import com.adjustclient.core.settings.property.UserManager;
import com.adjustclient.managers.party.PartyChat;
import com.adjustclient.managers.user.ClientUser;
import com.adjustclient.utilities.CommandCompleter;
import java.util.Arrays;

public class MessageCommand extends CommandExecutor {
    public static ClientUser lastRecipient;
    public Object field24;
    public MessageCommand() {
        super("Message");
        field8 = "Send a private message to an online Adjust user.";
        aliases = new String[] {"message", "msg", "whisper", "w".toLowerCase()};
    }

    @Override public String[] tabComplete(String input, String[] args) {
        return input.contains(" ") ? null : CommandCompleter.complete(input, CommandCompleter.USERS);
    }

    public void send(String[] args, ClientUser user) {
        if (user == null) { sendUnknownUser(args[0]); return; }
        String text = PartyChat.expandQuickMessages(String.join(" ", Arrays.asList(args).subList(1, args.length)));
        Adjust.INSTANCE.screenStack.directMessages.send(user, text, createMessage());
        lastRecipient = user;
    }

    @Override protected void run(String label, String[] args) {
        if (args.length < 2) { sendTooLittleArguments(); return; }
        ((UserManager) Adjust.INSTANCE.field40).lookup(args[0], user -> send(args, user));
    }
}
