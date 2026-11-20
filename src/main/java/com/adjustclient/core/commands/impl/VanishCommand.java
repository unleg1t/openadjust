package com.adjustclient.core.commands.impl;

import com.adjustclient.core.commands.CommandExecutor;
import com.adjustclient.utilities.*;
import com.adjustclient.modules.impl.exploit.playerdetect.PlayerDetectModule;
import com.adjustclient.modules.impl.exploit.playerdetect.hypixel.vanish.VanishedPlayer;
import java.util.Map;
import net.minecraft.util.ChatColor;

public final class VanishCommand extends CommandExecutor {
    public Object field24;

    public VanishCommand(String name) {
        super(name);
        this.field8 = "View a list of all vanished players in your lobby.";
        this.aliases = new String[] {"vanishedplayers", "vanished"};
    }

    public static void appendPlayer(ChatMessage message, Map.Entry<String, VanishedPlayer> entry) {
        VanishedPlayer player = entry.getValue();
        message.append("\n");
        message.format("{}", String.valueOf(player.rankPrefix) + player.name);
    }

    @Override protected void run(String label, String[] args) {
        var detector = PlayerDetectModule.INSTANCE.vanishDetector;
        if (detector != null) {
            Map<String, VanishedPlayer> players = detector.vanishedPlayers;
            ChatMessage message = new ChatMessage();
            message.color = ChatColor.WHITE;
            message.format("Listing {} vanished players below:", players.size());
            players.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> appendPlayer(message, entry));
            sendMessage(message);
        } else {
            ChatMessage message = createMessage();
            message.color = ChatColor.WHITE;
            message.append("Vanish Detect");
            message.format(" is not enabled or you are not in a valid lobby.");
            sendMessage(message);
        }
    }
}
