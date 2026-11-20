package com.adjustclient.core.commands.impl;

import com.adjustclient.core.commands.CommandExecutor;
import com.adjustclient.utilities.ChatMessage;
import com.adjustclient.utilities.ChatUtil;
import net.minecraft.util.ChatColor;

public final class VClipCommand extends CommandExecutor {
    public static final com.adjustclient.utilities.Timer TELEPORT_TIMER = new com.adjustclient.utilities.Timer(0L);
    public Object field24;

    public VClipCommand(String name) {
        super(name);
        this.field8 = "Moves the player vertically by the specified amount.";
        this.aliases = new String[] {"vclip", "vc"};
    }

    @Override protected void run(String label, String[] args) {
        if (args.length >= 2) { sendTooManyArguments(); return; }
        if (args.length != 1) { sendTooLittleArguments(); return; }
        try {
            String input = args[0].equalsIgnoreCase("nan") ? "NaN" : args[0].equalsIgnoreCase("infinity") ? "Infinity" : args[0];
            double distance = Double.parseDouble(input);
            double absolute = Math.abs(distance);
            String value = Double.toString(absolute);
            var server = com.adjustclient.utilities.anticheat.ServerTracker.INSTANCE.serverState;
            if (server != null && server.antiCheatModel != null
                    && server.antiCheatModel.serverType == com.adjustclient.utilities.server.ServerType.HYPIXEL && distance == 0.42)
                distance = 0.4199999868869781;
            absolute = Math.abs(distance);
            value = Double.toString(absolute);
            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + distance, mc.thePlayer.posZ);
            TELEPORT_TIMER.reset();
            ChatMessage message = createMessage();
            message.color = ChatColor.GRAY;
            message.append(value.contains(".") ? "You were teleported " : "You were teleported to ");
            message.color = ChatColor.WHITE;
            message.append(value);
            message.color = ChatColor.GRAY;
            message.append(value.contains(".") ? " block" + (absolute == 1.0 ? " " : "s ") + (distance > 0.0 ? "up" : "down") + "." : ".");
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
