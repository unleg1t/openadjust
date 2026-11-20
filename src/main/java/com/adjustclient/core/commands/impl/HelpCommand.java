package com.adjustclient.core.commands.impl;

import com.adjustclient.Adjust;
import com.adjustclient.core.commands.CommandBase;
import com.adjustclient.utilities.ChatMessage;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.ChatColor;

public final class HelpCommand extends CommandBase {
    public Object field24;
    public HelpCommand() {
        super("Help");
        field8 = "Displays a list of every Adjust command.";
        aliases = new String[] {"help", "?".toLowerCase(), "cmds", "commands"};
    }

    @Override public boolean execute(String label, String[] args) {
        ChatMessage message = new ChatMessage();
        var commands = Adjust.INSTANCE.commandManager.view;
        for (int i = 0; i < commands.size(); i++) {
            CommandBase command = commands.get(i);
            if (command.hidden) continue;
            ChatComponentText names = new ChatComponentText("");
            names.appendSibling(new ChatComponentText(String.valueOf(command.getName()))
                    .setChatStyle(new ChatStyle().setColor(ChatColor.WHITE)));
            names.appendSibling(new ChatComponentText(" ").setChatStyle(new ChatStyle().setColor(ChatColor.RESET)));
            if (command.aliases != null) {
                for (int j = 0; j < command.aliases.length; j++) {
                    names.appendSibling(new ChatComponentText(String.valueOf(command.aliases[j])));
                    if (j + 1 < command.aliases.length) names.appendText(", ");
                }
            }
            ChatMessage hover = new ChatMessage();
            hover.italic = true;
            hover.append("Click to insert command");
            message.hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover.getComponent());
            if (command.aliases != null && command.aliases.length > 0) {
                message.clickEvent = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                        "" + com.adjustclient.managers.config.user.UserConfiguration.COMMAND_PREFIX.getChar() + command.aliases[0] + " ");
            }
            message.append(names);
            message.color = ChatColor.WHITE;
            message.append(": ");
            message.color = ChatColor.GRAY;
            message.append(command.field8);
            if (i + 1 < commands.size()) message.append("\n");
        }
        sendMessage(message);
        return false;
    }
}
