package com.adjustclient.hypixel;

import com.adjustclient.Adjust;
import com.adjustclient.event.DirectionalPacketEvent;
import com.adjustclient.event.Listener;
import com.adjustclient.hypixel.game.bedwars.BedWarsGameState;
import com.adjustclient.logging.LogLevel;
import com.adjustclient.logging.Logger;
import com.adjustclient.service.telemetry.TelemetryCategory;
import com.adjustclient.service.telemetry.TelemetryFlag;
import com.adjustclient.service.telemetry.TelemetryQueue;
import com.adjustclient.service.telemetry.event.GameEventRecordA;
import com.adjustclient.service.telemetry.event.GameEventRecordB;
import com.adjustclient.service.telemetry.event.GameEventRecordC;
import com.adjustclient.util.Timer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.network.play.server.S3EPacketTeams;
import org.apache.commons.lang3.StringUtils;

public final class HypixelGameEventReporter implements Listener {
    public boolean reportedTeams;
    public Timer timer;

    public void onPacket(DirectionalPacketEvent event) {
    }

    @SuppressWarnings("unused")
    private void vendorOnPacket(DirectionalPacketEvent event) {
        if (event == null || event.packet == null) {
            return;
        }
        if (event.packet instanceof S38PacketPlayerListItem) {
            S38PacketPlayerListItem listPacket = (S38PacketPlayerListItem) event.packet;
            if (!reportedTeams || listPacket.getAction() != S38PacketPlayerListItem.Action.ADD_PLAYER) {
                return;
            }
            if (!(Adjust.gameStateTracker.state instanceof BedWarsGameState)) {
                return;
            }
            BedWarsGameState bedWarsState = (BedWarsGameState) Adjust.gameStateTracker.state;
            if (bedWarsState.playerTeams == null) {
                return;
            }
            try {
                for (S38PacketPlayerListItem.AddPlayerData data : listPacket.getEntries()) {
                    if (data == null || data.getProfile() == null || data.getProfile().getName() == null) {
                        continue;
                    }
                    String playerName = data.getProfile().getName();
                    if (!bedWarsState.playerTeams.containsKey(playerName.toLowerCase())) {
                        if (Adjust.telemetryListener != null
                                && Adjust.telemetryListener.state != null
                                && Adjust.telemetryListener.state.enabled
                                && !Adjust.telemetryListener.state.gameSent) {
                            TelemetryQueue queue = TelemetryQueue.INSTANCE;
                            if (queue != null) {
                                queue.enqueue(new GameEventRecordC(TelemetryFlag.SERVER, TelemetryCategory.EXTREME, playerName));
                            }
                        }
                        return;
                    }
                }
            } catch (Exception e) {
                Logger.log(LogLevel.ERROR, true, "Failed to process events: {}", e);
            }
            return;
        }

        if (event.packet instanceof S3EPacketTeams) {
            S3EPacketTeams teamsPacket = (S3EPacketTeams) event.packet;
            String teamName = teamsPacket.getName();
            if (!isSpectatorTeam(teamName) && !"\u00a76\u00a7lSPEC \u00a76".equals(teamsPacket.getPrefix())) {
                return;
            }
            Collection<String> rawPlayers = teamsPacket.getPlayers();
            List<String> playersList = rawPlayers instanceof List
                    ? (List<String>) rawPlayers
                    : new ArrayList<>(rawPlayers);
            String joined = joinPlayers(playersList);
            if (teamsPacket.getAction() == 3) {
                if (Adjust.telemetryListener.state != null
                        && Adjust.telemetryListener.state.enabled
                        && !Adjust.telemetryListener.state.gameSent) {
                    TelemetryQueue queue = TelemetryQueue.INSTANCE;
                    if (queue != null) {
                        queue.enqueue(new GameEventRecordA(TelemetryFlag.SERVER, TelemetryCategory.EXTREME, joined));
                    }
                }
            } else {
                Adjust.telemetryListener.report(
                        new GameEventRecordB(TelemetryFlag.SERVER, TelemetryCategory.EXTREME, teamName, joined));
                reportedTeams = true;
            }
        }
    }

    public static boolean isSpectatorTeam(String team) {
        if (Adjust.gameStateTracker == null || Adjust.gameStateTracker.state == null
                || Adjust.gameStateTracker.state.game == HypixelGame.WARLORDS) {
            return false;
        }
        if (team.matches("spec\\d{2}")) {
            return true;
        }
        if ("a9990".equals(team)) {
            return true;
        }
        if (team.startsWith("000pre")) {
            return true;
        }
        if (team.startsWith("a008-")) {
            return true;
        }
        if (team.contains("\u00a7") && team.split("\u00a7")[0].endsWith("00")) {
            return true;
        }
        if (StringUtils.isNumeric(team) && team.endsWith("01")) {
            return Adjust.gameStateTracker.state.game != HypixelGame.THE_HYPIXEL_PIT;
        }
        return false;
    }

    public static String joinPlayers(List<String> players) {
        if (players == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String player : players) {
            sb.append(player).append(", ");
        }
        return sb.length() > 0 ? sb.substring(0, sb.length() - 2) : sb.toString();
    }
}
