package com.adjustclient.core.commands.impl;

import com.adjustclient.core.commands.CommandExecutor;
import com.adjustclient.utilities.ChatMessage;
import com.adjustclient.utilities.ChatUtil;
import net.minecraft.util.ChatColor;

public final class HClipCommand extends CommandExecutor {
    public Object field24;

    public HClipCommand(String name) {
        super(name);
        field8 = "Moves the player horizontally by the specified amount.";
        aliases = new String[] {"hclip", "hc"};
    }

    @Override protected void run(String label, String[] args) {
        // Framework output: "Too many arguments."
        if (args.length >= 2) { sendTooManyArguments(); return; }
        if (args.length != 1) { sendTooLittleArguments(); return; }
        try {
            String input = args[0].equalsIgnoreCase("nan") ? "NaN" : args[0].equalsIgnoreCase("infinity") ? "Infinity" : args[0];
            double distance = Double.parseDouble(input);
            double absolute = Math.abs(distance);
            String value = Double.toString(absolute);
            double yaw = mc.thePlayer.rotationYaw * 0.0174532925199433;
            mc.thePlayer.setPosition(mc.thePlayer.posX - Math.sin(yaw) * distance,
                    mc.thePlayer.posY, mc.thePlayer.posZ + Math.cos(yaw) * distance);
            ChatMessage message = createMessage();
            message.color = ChatColor.GRAY;
            message.append(value.contains(".") ? "You were teleported " : "You were teleported to ");
            message.color = ChatColor.WHITE;
            message.append(value);
            message.color = ChatColor.GRAY;
            message.append(value.contains(".") ? " block" + (absolute == 1.0 ? " " : "s ") + (distance > 0.0 ? "forward" : "backward") + "." : ".");
            ChatUtil.addChatMessage(message.getComponent(), 0, true, ChatUtil.DEFAULT_COLOR);
        } catch (NumberFormatException exception) {
            ChatMessage message = createMessage();
            message.color = ChatColor.GRAY;
            message.append("Unable to translate ");
            message.color = ChatColor.WHITE;
            message.append(args[0]);
            message.color = ChatColor.GRAY;
            message.append(" into a number.");
            ChatUtil.addChatMessage(message.getComponent(), 0, true, ChatUtil.DEFAULT_COLOR);
        }
    }

    @Override public int getExecutionDelay() { return 32; }
}
