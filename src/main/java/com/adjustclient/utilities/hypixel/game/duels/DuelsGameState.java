package com.adjustclient.utilities.hypixel.game.duels;

import com.adjustclient.utilities.game.server.GameCountdown;
import com.adjustclient.utilities.hypixel.HypixelGame;
import com.adjustclient.utilities.hypixel.HypixelGameState;
import com.adjustclient.utilities.hypixel.game.ScoreboardLineProvider;
import com.adjustclient.utilities.Timer;
import com.adjustclient.utilities.anticheat.ServerTracker;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraft.util.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import net.minecraft.network.play.server.S45PacketTitle;

public final class DuelsGameState extends HypixelGameState implements ScoreboardLineProvider {
    public GameCountdown countdown;
    public final Timer phaseTimer = new Timer(0L);
    public Phase phase;
    public DuelsMode mode;
    public boolean bossBarSpawned;
    public Object field64;
    public DuelsGameState() { super(HypixelGame.DUELS); }

    enum Phase {
        TITLE, CLOSE_WINDOW;
        public Object field16;
    }

    public void detectMode(String text) {
        for (DuelsMode candidate : DuelsMode.values()) {
            if (candidate.matches(text) || text.contains(candidate.displayName)) mode = candidate;
        }
    }

    @Override public void onPacket(Packet<?> packet, boolean before) {
        super.onPacket(packet, before);
        if (!before) return;
        if (packet instanceof S2EPacketCloseWindow) {
            if (startTimer != null && !ended && phase == Phase.TITLE
                    && ((S2EPacketCloseWindow) packet).getWindowId() == 0) {
                phase = Phase.CLOSE_WINDOW;
                phaseTimer.reset();
            }
        } else if (packet instanceof S2FPacketSetSlot) {
            S2FPacketSetSlot slot = (S2FPacketSetSlot) packet;
            if (startTimer != null && !ended && phase == Phase.CLOSE_WINDOW
                    && slot.func_149175_c() == 0 && slot.func_149173_d() == 9 && slot.func_149174_e() == null) {
                phase = null;
                onGameEnd();
            }
        } else if (packet instanceof S45PacketTitle) {
            S45PacketTitle title = (S45PacketTitle) packet;
            if (startTimer != null && !ended && phase == null
                    && title.getType() == S45PacketTitle.Type.TITLE) {
                int latency = 0;
                var state = ServerTracker.INSTANCE.serverState;
                if (state != null) {
                    var watcher = state.packetWatcher;
                    if (watcher.delayCount != 0) latency = watcher.totalDelay / watcher.delayCount;
                }
                if (hasStartedFor(latency + 300L)) {
                    phase = Phase.TITLE;
                    phaseTimer.reset();
                }
            }
        } else if (packet instanceof S3EPacketTeams) {
            if (!Boolean.FALSE.equals(lobby)) return;
            S3EPacketTeams teams = (S3EPacketTeams) packet;
            Pair<GameCountdown, Boolean> parsed = parseCountdown(teams);
            if (parsed.getRight()) countdown = parsed.getLeft();
            if (startTimer == null && mode == null && teams.getName().equals("team_4")) {
                String text = teams.getPrefix() + teams.getSuffix();
                if (text.contains(": §a")) detectMode(StringUtils.stripControlCodes(text.split(": §a")[1]));
            }
        } else if (packet instanceof S03PacketTimeUpdate || packet instanceof S14PacketEntity) {
            if (phaseTimer.hasElapsed(540L)) phase = null;
        }
    }

    @Override public void onBossBarSpawn() { bossBarSpawned = true; }

    @Override public Integer getAdditionalData() { return mode == DuelsMode.BRIDGE ? Integer.valueOf(100) : null; }
}
