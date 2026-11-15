package com.adjustclient.command.impl;

import com.adjustclient.command.CommandExecutor;
import com.adjustclient.util.*;
import com.adjustclient.module.impl.exploit.playerdetect.PlayerDetectModule;
import com.adjustclient.module.impl.exploit.playerdetect.hypixel.nick.NickedPlayer;
import java.util.Map;
import net.minecraft.util.ChatColor;

public class NicksCommand extends CommandExecutor {
    public Object field24;

    public NicksCommand(String name) {
        super(name);
        field8 = "View a list of all nicked players in your lobby.";
        aliases = new String[] {"nicks"};
    }

    public static void appendPlayer(ChatMessage message, Map.Entry<String, NickedPlayer> entry) {
        NickedPlayer player = entry.getValue();
        message.append("\n");
        message.format("{}: {}", entry.getKey(), player != null && player.realName != null ? player.realName : ChatColor.GRAY + "?");
    }

    @Override protected void run(String label, String[] args) {
        var detector = PlayerDetectModule.INSTANCE.nickDetector;
        if (detector != null) {
            Map<String, NickedPlayer> players = detector.nickedPlayers;
            ChatMessage message = new ChatMessage();
            message.color = ChatColor.WHITE;
            message.format("Listing {} nicks below:", players.size());
            players.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> appendPlayer(message, entry));
            sendMessage(message);
        } else {
            ChatMessage message = createMessage();
            Runnable open = () -> ScreenUtil.openClickGuiAt(PlayerDetectModule.INSTANCE.nickDetectEnabled);
            message.clickEvent = new net.minecraft.event.ClickEvent(net.minecraft.event.ClickEvent.Action.SUGGEST_COMMAND, "", open);
            message.withClickToOpenHover(); // "Click to open!"
            message.color = ChatColor.WHITE;
            message.append("Nick Detect");
            message.format(" is not enabled or you are not in a valid lobby.");
            sendMessage(message);
        }
    }
}
