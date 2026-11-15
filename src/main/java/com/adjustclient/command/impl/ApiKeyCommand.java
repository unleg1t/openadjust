package com.adjustclient.command.impl;

import com.adjustclient.command.CommandExecutor;
import com.adjustclient.service.config.user.ApiKeyProperty;
import com.adjustclient.service.config.user.UserConfiguration;
import java.util.LinkedHashMap;
import java.util.UUID;
import net.minecraft.util.ChatColor;

public final class ApiKeyCommand extends CommandExecutor {
    public Object field24;

    public static final LinkedHashMap<String, ApiKeyProperty> TYPES;

    static {
        TYPES = new LinkedHashMap<>();
        for (ApiKeyProperty property : UserConfiguration.API_KEYS) TYPES.put(property.name.toLowerCase().replace("-", ""), property);
    }

    public ApiKeyCommand(String name) {
        super(name);
        field8 = "Manage your API keys for various services (accessible from Player List settings as well)";
        aliases = new String[] {"apikey"};
    }

    @Override public String[] tabComplete(String input, String[] args) {
        String[] parts = input.split(" ", -1);
        if (parts.length != 1) return null;
        return net.minecraft.command.CommandBase.getListOfStringsMatchingLastWord(parts, TYPES.keySet()).toArray(String[]::new);
    }

    @Override protected void run(String label, String[] args) {
        System.out.println(UserConfiguration.SERAPH_API_KEY.keyString);
        // "Too many arguments." / "Too little arguments." are emitted by the framework.
        if (args.length >= 3) { sendTooManyArguments(getUsage()); return; }
        if (args.length != 2) { sendTooLittleArguments(getUsage()); return; }
        ApiKeyProperty property = TYPES.get(args[0].toLowerCase());
        if (property == null) { sendUnknownArgument(args[0], getUsage()); return; }
        try {
            property.setKey(UUID.fromString(args[1]));
            sendMessage(createMessage().format("Updated your {} API key!", property.name));
        } catch (IllegalArgumentException exception) {
            String value = args[1];
            if (value.length() >= 9) value = value.substring(0, 8) + ChatColor.OBFUSCATED + value.substring(8);
            sendMessage(createMessage().format("{} is not a valid API key.", value));
        }
    }

    public static String getUsage() { return "<" + String.join("|", TYPES.keySet()) + "> <api-key>"; }
}
