package com.adjustclient.service.telemetry;

import com.adjustclient.Adjust;
import com.adjustclient.event.HypixelGameEndedEvent;
import com.adjustclient.event.HypixelGameStartedEvent;
import com.adjustclient.event.Listener;
import com.adjustclient.event.PlayerKillEvent;
import com.adjustclient.hypixel.HypixelGame;
import com.adjustclient.hypixel.HypixelGameState;
import com.adjustclient.module.impl.exploit.playerdetect.hypixel.nick.NickDetector;
import com.adjustclient.server.ServerType;
import com.adjustclient.service.telemetry.event.*;
import com.adjustclient.util.anticheat.ServerTracker;
import com.adjustclient.util.anticheat.blocker.BlockerState;
import net.minecraft.client.Minecraft;

public final class TelemetryListener implements Listener {
    public TelemetryEntry state = new TelemetryEntry();

    public void onGameMode(HypixelGameEndedEvent event) {
    }

    public void onGameStart(HypixelGameStartedEvent event) {
    }

    public void onGameState() {
    }

    public void onSessionStart() {
    }

    public void onEntityName(Object event) {
    }

    public void onPlayerName(PlayerKillEvent event) {
    }

    public void onHypixelJoin() {
    }

    public void report(TelemetryRecord record) {
    }

    @SuppressWarnings("unused")
    private void vendorOnGameMode(HypixelGameEndedEvent event) {
        if (event == null || event.state == null || state == null || !state.enabled || state.gameSent) {
            return;
        }
        TelemetryQueue queue = TelemetryQueue.INSTANCE;
        if (queue != null) {
            queue.enqueue(new GameModeRecord(TelemetryFlag.GAME, TelemetryCategory.LOW, event.state.game));
        }
    }

    @SuppressWarnings("unused")
    private void vendorOnGameStart(HypixelGameStartedEvent event) {
        if (event == null || event.state == null || state == null || !state.enabled || state.gameSent) {
            return;
        }
        TelemetryQueue queue = TelemetryQueue.INSTANCE;
        if (queue != null) {
            queue.enqueue(new GameStartRecord(TelemetryFlag.GAME, TelemetryCategory.LOW, event.state.game));
        }
    }

    @SuppressWarnings("unused")
    private void vendorOnGameState() {
        if (state == null || !state.gameStateDirty) {
            return;
        }
        HypixelGameState gameState = Adjust.gameStateTracker == null ? null : Adjust.gameStateTracker.state;
        if (gameState == null || gameState.lobby == null) {
            return;
        }
        state.gameStateDirty = false;
        if (gameState.game == HypixelGame.UNKNOWN_GAME || !state.enabled || state.gameSent) {
            return;
        }
        byte lobbyByte = gameState.lobby ? (byte) 1 : (byte) 0;
        TelemetryQueue queue = TelemetryQueue.INSTANCE;
        if (queue != null) {
            queue.enqueue(new GameStateRecord(TelemetryFlag.GAME, TelemetryCategory.LOW, gameState.game, lobbyByte));
        }
    }

    @SuppressWarnings("unused")
    private void vendorOnSessionStart() {
        if (state == null || !state.enabled) {
            return;
        }
        TelemetryQueue queue = TelemetryQueue.INSTANCE;
        if (queue != null) {
            queue.enqueue(new SessionStartRecord(TelemetryFlag.ACTION, TelemetryCategory.LOW));
        }
    }

    @SuppressWarnings("unused")
    private void vendorOnEntityName(Object event) {
        if (state == null || event == null || !state.enabled || state.gameSent) {
            return;
        }
        String entityName = event.toString();
        if (state.entityNames.add(entityName)) {
            TelemetryQueue queue = TelemetryQueue.INSTANCE;
            if (queue != null) {
                queue.enqueue(new EntityNameRecord(TelemetryFlag.MODULE, TelemetryCategory.LOW, entityName));
            }
        }
    }

    @SuppressWarnings("unused")
    private void vendorOnPlayerName(PlayerKillEvent event) {
        if (event == null || state == null || !state.enabled || state.gameSent) {
            return;
        }
        String localName = Minecraft.getMinecraft().getSession().getUsername();
        if (event.killer != null && event.killer.equals(localName)) {
            boolean nicked = NickDetector.isNicked(event.victim);
            byte flags = (byte) (event.flags | (nicked ? 2 : 0));
            vendorReport(new PlayerNameRecord(TelemetryFlag.GAME, TelemetryCategory.LOW, event.victim, flags));
        }
        if (event.victim != null && !event.victim.equals(localName)) {
            TelemetryQueue queue = TelemetryQueue.INSTANCE;
            if (queue != null) {
                queue.enqueue(new ServerAddressRecord(TelemetryFlag.GAME, TelemetryCategory.LOW, event.victim));
            }
        }
    }

    @SuppressWarnings("unused")
    private void vendorOnHypixelJoin() {
        if (state == null || !state.enabled) {
            return;
        }
        BlockerState blocker = ServerTracker.INSTANCE == null ? null : ServerTracker.INSTANCE.serverState;
        if (blocker != null && blocker.antiCheatModel != null
                && blocker.antiCheatModel.serverType == ServerType.HYPIXEL) {
            TelemetryQueue queue = TelemetryQueue.INSTANCE;
            if (queue != null) {
                queue.enqueue(new HypixelJoinRecord(TelemetryFlag.ACTION, TelemetryCategory.LOW));
            }
        }
    }

    @SuppressWarnings("unused")
    private void vendorReport(TelemetryRecord record) {
        if (state == null || !state.enabled || record == null) {
            return;
        }
        TelemetryQueue queue = TelemetryQueue.INSTANCE;
        if (queue != null) {
            queue.enqueue(record);
        }
    }
}
