package com.adjustclient.command.impl;

import com.adjustclient.command.CommandExecutor;
import com.adjustclient.service.config.user.UserConfiguration;

public final class CubelifyCommand extends CommandExecutor {
    public Object field24;
    public CubelifyCommand(String name, String... aliases) {
        super(name);
        field8 = "Set your Cubelify token.";
        this.aliases = aliases;
    }

    @Override protected void run(String label, String[] args) {
        if (args.length >= 3) { sendTooManyArguments(); return; }
        String pattern = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.[0-9a-f]{128}";
        if (args.length == 0) {
            String token = UserConfiguration.INSTANCE.cubelifyToken;
            sendMessage(createMessage().format(token != null && token.matches(pattern)
                    ? "Cubelify token is set." : "Cubelify token is not set."));
            return;
        }
        String token;
        if (args.length == 2 && args[0].equals("Bearer")) token = args[1];
        else if (args.length == 1) token = args[0];
        else { sendMessage(createMessage().format("Not a valid token.")); return; }
        if (token.matches(pattern)) {
            UserConfiguration.INSTANCE.cubelifyToken = token;
            sendMessage(createMessage().format("Set Cubelify token."));
        } else sendMessage(createMessage().format("Not a valid token."));
    }
}
