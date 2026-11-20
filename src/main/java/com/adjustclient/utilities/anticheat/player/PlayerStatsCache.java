package com.adjustclient.utilities.anticheat.player;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.EventBus;
import com.adjustclient.core.events.PlayerKillEvent;
import com.adjustclient.utilities.hypixel.HypixelGame;
import com.adjustclient.utilities.server.ServerType;
import com.adjustclient.utilities.anticheat.ServerTracker;
import com.adjustclient.utilities.anticheat.player.kill.*;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.play.server.S02PacketChat;

public final class PlayerStatsCache {
    public static PlayerStatsCache INSTANCE = new PlayerStatsCache();
    public final Map<String, PlayerKillStats> stats = new HashMap<>();
    public Object field8;
    private static final Map<HypixelGame, KillMessageParser> PARSERS;
    private static final KillMessageParser DEFAULT_PARSER;
    static {
        PARSERS = new HashMap<>();
        PARSERS.put(HypixelGame.BED_WARS, new TwoPlayerKillParser());
        PARSERS.put(HypixelGame.SKYWARS, new ColoredKillParser());
        PARSERS.put(HypixelGame.HOUSING, new KilledByKillParser());
        PARSERS.put(HypixelGame.BLITZ_SG, new ComponentKillParser());
        DEFAULT_PARSER = new SimpleKillParser();
    }
    public PlayerKillStats get(String name) {
        return stats.get(name);
    }
    public void record(KillEvent kill) {
        if (kill == null) return;
        PlayerKillEvent event = new PlayerKillEvent(kill.victim, kill.killer, false, kill.finalKill);
        EventBus.post(event);
        if (kill.killer != null) {
            stats.computeIfAbsent(kill.killer, name -> new PlayerKillStats()).kills++;
        }
        stats.computeIfAbsent(kill.victim, name -> new PlayerKillStats()).deaths++;
    }

    @SuppressWarnings("unused")
    private static void vendorReportKillTelemetry(PlayerKillEvent event) {
        if (Adjust.telemetryListener != null) {
            Adjust.telemetryListener.onPlayerName(event);
        }
    }
    public void onChat(S02PacketChat chat) {
        var server = ServerTracker.INSTANCE.serverState;
        if (server == null || server.antiCheatModel == null) return;
        ServerType type = server.antiCheatModel.serverType;
        if (type == ServerType.HYPIXEL) {
            var state = Adjust.gameStateTracker.state;
            if (state.startTimer != null && PARSERS.containsKey(state.game)) {
                record(PARSERS.get(state.game).parse(chat.getChatComponent()));
            }
        } else if (type == ServerType.ANTICHEAT_TEST) {
            record(DEFAULT_PARSER.parse(chat.getChatComponent()));
        }
    }
}
