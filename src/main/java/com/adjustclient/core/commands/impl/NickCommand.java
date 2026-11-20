package com.adjustclient.core.commands.impl;

import com.adjustclient.core.commands.CommandExecutor;
import com.adjustclient.utilities.ChatMessage;
import com.adjustclient.managers.config.user.UserConfiguration;
import com.adjustclient.modules.impl.render.streamer.StreamerModule;
import com.google.common.collect.HashBiMap;
import java.util.Objects;

public final class NickCommand extends CommandExecutor {
    public Object field24;

    public NickCommand(String name) {
        super(name);
        field8 = "Changes your in-game username. This effect is visual only, you are the only one who can see it.";
        aliases = new String[] {"nickname", "nick"};
    }

    @Override protected void run(String label, String[] args) {
        if (args.length == 0) {
            if (UserConfiguration.field2f84 != null) sendMessage(createMessage().format("Your nickname {} has been cleared!", UserConfiguration.field2f84));
            else sendMessage(createMessage().append("You don't have a nickname set!"));
            UserConfiguration.setNickname(null);
            StreamerModule.refreshPlayerNames();
            return;
        }
        String nickname = String.join(" ", args);
        if (Objects.equals(UserConfiguration.field2f84, nickname)) {
            sendMessage(createMessage().format("Your nickname is already {}!", nickname));
            return;
        }
        if (!isValidNickname(nickname)) {
            sendMessage(createMessage().format("{} is not a valid username!", nickname));
            return;
        }
        nickname = nickname.replaceAll("&(?=[4-9]|c|e|a|b|3|1|9|d|5|f|7|8|0|r|l|o|n|m|k)", "§");
        sendMessage(createMessage().format("Your username was set to {}! Use '{}' with no arguments to clear it.", nickname, "." + label));
        setNickname(nickname);
    }

    public void setNickname(String nickname) {
        UserConfiguration.setNickname(nickname);
        StreamerModule.refreshPlayerNames();
    }

    public static boolean isValidNickname(String nickname) {
        if (nickname.length() < 2 || nickname.length() >= 25 || nickname.isBlank()) return false;
        for (char character : nickname.toCharArray()) {
            if (character == 167 || character < 32 || character == 127) return false;
        }
        return true;
    }
}
