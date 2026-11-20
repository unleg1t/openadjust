package com.adjustclient.core.commands.impl;

import com.adjustclient.core.commands.CommandExecutor;
import com.adjustclient.managers.globalchat.ChatFontStyle;
import com.adjustclient.utilities.ChatMessage;
import net.minecraft.util.ChatComponentText;

public class EmojiCommand extends CommandExecutor {
    public Object field24;
    public EmojiCommand(String name, String... aliases) {
        super(name);
        field8 = "Displays emojis that can be used in the Adjust global chat.";
        this.aliases = aliases;
    }

    @Override protected void run(String label, String[] args) {
        ChatFontStyle[] emojis = ChatFontStyle.values();
        ChatMessage message = createMessage();
        message.format("Displaying {} emojis:", emojis.length);
        for (ChatFontStyle emoji : emojis) {
            message.append("\n");
            message.format("{} - ", emoji.prefix);
            message.append(new ChatComponentText(emoji.suffix));
        }
        sendMessage(message);
    }
}
