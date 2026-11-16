package com.adjustclient.service.session;

import com.adjustclient.Adjust;
import com.adjustclient.event.*;
import com.adjustclient.hypixel.HypixelGameState;
import com.adjustclient.service.storage.LocalStorage;
import com.adjustclient.ui.account.session.SessionListScreen;
import com.adjustclient.util.ByteUtil;
import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.util.Timer;
import com.adjustclient.util.anticheat.ServerTracker;
import com.adjustclient.util.anticheat.model.WatchdogModel;
import com.adjustclient.util.anticheat.blocker.BlockerState;
import com.mojang.util.UUIDTypeAdapter;
import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.Session;

public final class SessionService implements Listener, MinecraftInstance {
    public static SessionService INSTANCE;
    public Map<UUID, SessionStats> sessions;
    public Timer gameTimer;
    public Timer totalTimer;
    public Timer activityTimer;
    public boolean loaded;
    public Object field24;

    public SessionService() {
        sessions = new LinkedHashMap<>();
        loaded = false;
        gameTimer = new Timer();
        totalTimer = new Timer();
        activityTimer = new Timer();
        INSTANCE = this;
        LocalStorage storage = LocalStorage.INSTANCE;
        java.util.function.Consumer<byte[]> handler = data -> read(data);
        if (storage.pendingOperations == null) {
            File sessionFile = new File(storage.directory, "sessions.dat");
            if (!sessionFile.isFile()) {
                handler.accept(null);
            } else {
                try {
                    byte[] encrypted = com.google.common.io.Files.toByteArray(sessionFile);
                    handler.accept(storage.crypt("sessions.dat", encrypted));
                } catch (Exception exception) {
                    com.adjustclient.logging.Logger.log(com.adjustclient.logging.LogLevel.ERROR, true,
                            "Failed to run handler for local storage file {}! Reason: {}", "sessions.dat", exception);
                }
            }
        } else {
            storage.pendingOperations.add(() -> {
                try {
                    byte[] encrypted = com.google.common.io.Files.toByteArray(new File(storage.directory, "sessions.dat"));
                    handler.accept(storage.crypt("sessions.dat", encrypted));
                } catch (Exception exception) {
                    com.adjustclient.logging.Logger.log(com.adjustclient.logging.LogLevel.ERROR, true,
                            "Failed to run handler for local storage file {}! Reason: {}", "sessions.dat", exception);
                }
            });
        }
    }

    public void save() {
        if (!loaded) return;
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream output = new DataOutputStream(bytes);
            output.writeByte('S');
            output.writeByte(0);
            output.writeUTF(SessionListScreen.selectedName != null ? SessionListScreen.selectedName : "");
            synchronized (sessions) {
                output.writeInt(sessions.size());
                for (Map.Entry<UUID, SessionStats> entry : sessions.entrySet()) {
                    output.writeLong(entry.getKey().getMostSignificantBits());
                    output.writeLong(entry.getKey().getLeastSignificantBits());
                    SessionStats stats = entry.getValue();
                    output.writeUTF(stats.name != null ? stats.name : "");
                    ByteUtil.writeVarLong(output, stats.firstSeen - 1728500000000L);
                    ByteUtil.writeVarLong(output, stats.lastSeen - 1728500000000L);
                    ByteUtil.writeVarLong(output, stats.hypixelTime);
                    ByteUtil.writeVarLong(output, stats.totalTime);
                    ByteUtil.writeVarInt(output, stats.deaths);
                    ByteUtil.writeVarInt(output, stats.kills);
                    ByteUtil.writeVarInt(output, stats.games);
                }
            }
            loaded = false;
            LocalStorage storage = LocalStorage.INSTANCE;
            byte[] data = bytes.toByteArray();
            if (storage.pendingOperations == null) storage.saveNow("sessions.dat", data);
            else storage.pendingOperations.add(() -> storage.saveNow("sessions.dat", data));
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public static UUID resolveSessionUuid(Session session) {
        if (session == null) return null;
        String playerId = session.getPlayerID();
        if (playerId != null && !playerId.isEmpty()) {
            try {
                return UUIDTypeAdapter.fromString(playerId);
            } catch (IllegalArgumentException ignored) {
                // Offline/dev launch without --uuid passes non-UUID playerID such as "Player0".
            }
        }
        UUID profileId = session.getProfile() != null ? session.getProfile().getId() : null;
        if (profileId != null) return profileId;
        String username = session.getUsername();
        return username != null ? net.minecraft.entity.player.EntityPlayer.getOfflineUUID(username) : new UUID(0L, 0L);
    }

    @EventHandler public void onGameEnd(com.adjustclient.event.HypixelGameEndedEvent event) {
        HypixelGameState state = event.state;
        if (state.lobby == null || state.lobby || state.startTimer == null || !state.startTimer.hasElapsed(550L)) return;
        Session session = mc.getSession();
        UUID id = resolveSessionUuid(session);
        if (id == null) return;
        get(id, session.getUsername()).games++;
    }

    public void onTick() {
        if (mc.theWorld == null) return;
        BlockerState server = ServerTracker.INSTANCE.serverState;
        if (server == null || mc.isGamePaused()) {
            totalTimer.reset();
            gameTimer.reset();
            return;
        }
        Session session = mc.getSession();
        UUID id = resolveSessionUuid(session);
        if (id == null) return;
        SessionStats stats = get(id, session.getUsername());
        stats.lastSeen = System.currentTimeMillis();
        boolean countTotal;
        if (!isInGame(server)) {
            gameTimer.reset();
            countTotal = isActive();
        } else if ((Adjust.gameStateTracker.state.game.flags & 4) != 0 && !isActive()) {
            gameTimer.reset();
            countTotal = isActive();
        } else {
            stats.hypixelTime += Math.min(System.currentTimeMillis() - gameTimer.lastMs, 2000L);
            gameTimer.reset();
            countTotal = true;
        }
        if (countTotal) stats.totalTime += Math.min(System.currentTimeMillis() - totalTimer.lastMs, 2000L);
        totalTimer.reset();
    }

    public static boolean isInGame(BlockerState server) {
        if (!(server.antiCheatModel instanceof WatchdogModel)) return false;
        HypixelGameState state = Adjust.gameStateTracker.state;
        return state.startTimer != null && state.lobby != null && !state.lobby && !state.isSpectator();
    }

    public SessionStats get(UUID id, String name) {
        synchronized (sessions) {
            SessionStats stats = sessions.get(id);
            if (stats == null) {
                long now = System.currentTimeMillis();
                stats = new SessionStats(name, now, now);
                sessions.put(id, stats);
            }
            stats.name = name;
            return stats;
        }
    }

    public boolean isActive() {
        if (!activityTimer.hasElapsed(20000L)) return true;
        GameSettings settings = mc.gameSettings;
        if (settings.keyBindUseItem.isKeyDown() || settings.keyBindAttack.isKeyDown()
                || settings.keyBindForward.isKeyDown() || settings.keyBindBack.isKeyDown()
                || settings.keyBindLeft.isKeyDown() || settings.keyBindRight.isKeyDown()) {
            activityTimer.reset();
            return true;
        }
        return !activityTimer.hasElapsed(30000L);
    }

    @EventHandler public void onPlayerKill(PlayerKillEvent event) {
        Session session = mc.getSession();
        UUID id = resolveSessionUuid(session);
        if (id == null) return;
        SessionStats stats = get(id, session.getUsername());
        if (event.killer != null && event.killer.equals(mc.getSession().getUsername())) stats.kills++;
        if (event.victim != null && event.victim.equals(mc.getSession().getUsername())) stats.deaths++;
    }

    public void read(byte[] data) {
        if (data == null) { loaded = true; return; }
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(data))) {
            if (input.readUnsignedByte() != 'S') throw new IOException("Invalid session data header");
            input.readUnsignedByte();
            SessionListScreen.selectedName = input.readUTF();
            int count = input.readInt();
            for (int i = 0; i < count; i++) {
                long most = input.readLong();
                long least = input.readLong();
                String name = input.readUTF();
                long firstSeen = 1728500000000L + ByteUtil.readVarLong(input);
                long lastSeen = 1728500000000L + ByteUtil.readVarLong(input);
                long gameTime = ByteUtil.readVarLong(input);
                long totalTime = ByteUtil.readVarLong(input);
                int deaths = ByteUtil.readVarInt(input);
                int kills = ByteUtil.readVarInt(input);
                int games = ByteUtil.readVarInt(input);
                synchronized (sessions) {
                    SessionStats stats = new SessionStats(name, firstSeen, lastSeen);
                    stats.hypixelTime = gameTime;
                    stats.totalTime = totalTime;
                    stats.deaths = deaths;
                    stats.kills = kills;
                    stats.games = games;
                    sessions.put(new UUID(most, least), stats);
                }
            }
        } catch (Exception ignored) {
        }
        loaded = true;
    }
}
