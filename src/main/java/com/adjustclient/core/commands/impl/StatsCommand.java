package com.adjustclient.core.commands.impl;

import com.adjustclient.core.commands.CommandExecutor;
import com.adjustclient.utilities.ChatMessage;
import com.adjustclient.utilities.PlayerNameCompleter;
import com.adjustclient.utilities.WorldUtil;
import com.adjustclient.modules.impl.exploit.playerdetect.PlayerDetectModule;
import com.adjustclient.modules.impl.exploit.playerdetect.hypixel.nick.NickedPlayer;
import com.adjustclient.modules.impl.render.streamer.StreamerModule;
import com.adjustclient.core.gui.stats.HypixelStatsScreen;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.ChatColor;

public class StatsCommand extends CommandExecutor {
    public Object field24;
    public StatsCommand(String name) {
        super(name);
        this.field8 = "Opens a GUI that shows you the players statistics on Hypixel.";
        this.aliases = new String[] {"stats", "statistics"};
    }

    @Override public String[] tabComplete(String input, String[] args) {
        return PlayerNameCompleter.complete(input, true);
    }

    public void resolvePlayer(String[] args) {
        String name = args[0];
        String uuid = null;
        for (NetworkPlayerInfo info : mc.thePlayer.sendQueue.getPlayerInfoMap()) {
            if (!info.getGameProfile().getName().equals(name)) continue;
            var detector = PlayerDetectModule.INSTANCE.nickDetector;
            Map<String, NickedPlayer> nicks = detector == null ? Collections.emptyMap() : detector.nickedPlayers;
            if (!nicks.containsKey(name)) {
                uuid = info.getGameProfile().getId().toString();
            } else {
                NickedPlayer real = nicks.get(name);
                if (real == null) {
                    ChatMessage message = createMessage();
                    message.color = ChatColor.GRAY;
                    message.append("The player ");
                    message.color = ChatColor.RESET;
                    message.append(StreamerModule.lookupMaskedName(name));
                    message.color = ChatColor.GRAY;
                    message.append(" is nicked.");
                    sendPersistentMessage(message);
                    return;
                }
                uuid = real.realUuid.toString();
            }
            break;
        }
        if (uuid == null) {
            try {
                uuid = WorldUtil.lookupUuid(name);
            } catch (IOException ignored) {
            }
        }
        if (uuid == null) {
            ChatMessage message = createMessage();
            message.color = ChatColor.GRAY;
            message.append("The player ");
            message.color = ChatColor.RESET;
            message.append(StreamerModule.lookupMaskedName(name));
            message.color = ChatColor.GRAY;
            message.append(" does not exist, are they nicked?");
            sendPersistentMessage(message);
            return;
        }
        final String resolved = uuid;
        mc.addScheduledTask(() -> {
            openStats(resolved);
        });
    }

    @Override protected void run(String label, String[] args) {
        if (args.length >= 2) { sendTooManyArguments(); return; }
        if (args.length == 1) {
            ForkJoinPool.commonPool().execute(() -> {
                resolvePlayer(args);
            });
            return;
        }
        mc.displayGuiScreen(new HypixelStatsScreen(null, mc.thePlayer.getUniqueID().toString()));
    }

    public static void openStats(String uuid) {
        mc.displayGuiScreen(new HypixelStatsScreen(null, uuid));
    }
}
