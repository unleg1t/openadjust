package com.adjustclient.command.impl;

import com.adjustclient.command.CommandExecutor;
import com.adjustclient.module.impl.render.streamer.StreamerModule;
import com.adjustclient.util.ChatMessage;
import com.adjustclient.util.ClipboardUtil;
import net.minecraft.util.ChatColor;

public final class UsernameCommand extends CommandExecutor {
    public Object field24;

    public UsernameCommand(String name) {
        super(name);
        this.field8 = "Copies the username of the account you are currently logged into to your clipboard.";
        this.aliases = new String[] {"username", "name", "ign"};
    }

    @Override protected void run(String label, String[] args) {
        String username = mc.getSession().getUsername();
        ChatMessage message = createMessage();
        message.color = ChatColor.GRAY;
        message.append("Your username ");
        message.color = ChatColor.WHITE;
        if (StreamerModule.nameMap != null && StreamerModule.INSTANCE.isEnabled()) {
            message.obfuscated = true;
            message.append(StreamerModule.replaceNames(username));
        } else message.append(username);
        message.color = ChatColor.GRAY;
        message.append(" was copied to your clipboard.");
        sendMessage(message);
        ClipboardUtil.copyToClipboard(username);
    }
}
