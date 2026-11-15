package com.adjustclient.command;

import com.adjustclient.util.ChatMessage;
import com.adjustclient.util.ChatUtil;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.ChatColor;

public interface Command {
    String getName();

    default void sendPersistentMessage(ChatMessage message) {
        if (message.component == null) message.component = new ChatComponentText("");
        ChatUtil.addChatMessage(message.component, 0, true, ChatUtil.DEFAULT_COLOR);
    }

    default void sendUnknownConfiguration(String name) {
        ChatMessage message = createMessage();
        message.append("Unknown configuration file ");
        message.append(name);
        message.color = ChatColor.GRAY;
        message.append("!");
        sendPersistentMessage(message);
    }

    default void sendTooManyArguments(String... usage) {
        ChatMessage message = createMessage();
        message.color = ChatColor.GRAY;
        message.append("Too many arguments.");
        appendUsage(message, usage);
        sendPersistentMessage(message);
    }

    default void sendReplacingMessage(ChatMessage message) {
        if (message.component == null) message.component = new ChatComponentText("");
        ChatUtil.addChatMessage(message.component, getName().hashCode(), false, ChatUtil.DEFAULT_COLOR);
    }

    default void appendUsage(ChatMessage message, String... usage) {
        if (usage == null || usage.length == 0) return;
        message.color = ChatColor.GRAY;
        message.append(" Possible matches: ");
        for (int i = 0; i < usage.length; i++) {
            message.color = ChatColor.WHITE;
            message.append(String.valueOf(usage[i]));
            if (i != usage.length - 1) {
                message.color = ChatColor.GRAY;
                message.append(i == usage.length - 2 ? " or " : ", ");
            }
        }
        message.color = ChatColor.GRAY;
        message.append(".");
    }

    default void sendUnknownArgument(String argument, String... usage) {
        ChatMessage message = createMessage();
        message.color = ChatColor.GRAY;
        message.append("Unknown argument ");
        message.color = ChatColor.WHITE;
        message.append(argument);
        message.color = ChatColor.GRAY;
        message.append(".");
        appendUsage(message, usage);
        sendPersistentMessage(message);
    }

    default ChatMessage createMessage() {
        ChatMessage message = new ChatMessage();
        message.color = ChatColor.GRAY;
        message.component = new ChatComponentText("");
        message.component.appendSibling(new ChatComponentText(getName() + " ")
                .setChatStyle(new ChatStyle().setBold(true)));
        return message;
    }

    default void sendMessage(ChatMessage message) {
        // ChatUtil.addChatMessage includes no-world logging, chat-style inheritance and persistent-chat output.
        if (message.component == null) message.component = new ChatComponentText("");
        ChatUtil.addChatMessage(message.component, getName().hashCode(), true, ChatUtil.DEFAULT_COLOR);
    }

    default void sendUnknownUser(String name) {
        ChatMessage message = createMessage();
        message.color = ChatColor.GRAY;
        message.append("No user by the name ");
        message.color = ChatColor.WHITE;
        message.append(name);
        message.color = ChatColor.GRAY;
        message.append(" exists.");
        sendPersistentMessage(message);
    }

    default void sendTooLittleArguments(String... usage) {
        ChatMessage message = createMessage();
        message.color = ChatColor.GRAY;
        message.append("Too little arguments.");
        appendUsage(message, usage);
        sendPersistentMessage(message);
    }

    default void sendRateLimited() {
        ChatMessage message = createMessage();
        message.color = ChatColor.GRAY;
        message.append("You are being ratelimited. Please wait a little bit before trying again.");
        sendPersistentMessage(message);
    }

    default void sendUnknownModule(String name) {
        ChatMessage message = createMessage();
        message.format("Cannot find module {}.", new Object[] { name });
        sendPersistentMessage(message);
    }
}
