package com.adjustclient.hypixel;

import com.adjustclient.Adjust;
import com.adjustclient.event.*;
import com.adjustclient.game.server.AbstractGameTracker;
import com.adjustclient.hypixel.api.model.player.HypixelPlayer;
import com.adjustclient.hypixel.game.TabTrackingGameState;
import com.adjustclient.module.impl.exploit.playerdetect.hypixel.nick.NickedPlayer;
import com.adjustclient.server.ServerType;
import com.adjustclient.ui.hud.notification.NotificationBuilder;
import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.util.SoundUtil;
import com.adjustclient.util.anticheat.ServerTracker;
import java.util.ArrayList;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import com.adjustclient.module.impl.combat.killaura.KillAuraModule;

public final class HypixelGameTracker extends AbstractGameTracker<HypixelGameState, HypixelGame> implements MinecraftInstance {
    public final ServerType server;
    public HypixelGameState state;
    public HypixelGameState previousTrackedState;
    public Object field16;

    public HypixelGameTracker(ServerType server, HypixelGameState initialState) {
        super(server, initialState);
        this.server = server;
        this.state = initialState;
    }

    public void reset() {
        state = HypixelGame.UNKNOWN_GAME.stateFactory.create();
        previousTrackedState = null;
    }

    private boolean isActiveServer() {
        var serverState = ServerTracker.INSTANCE.serverState;
        return serverState != null && serverState.antiCheatModel != null
                && serverState.antiCheatModel.serverType == this.server;
    }

    public static void notifyWinstreakEnd(String killer, String victim, HypixelPlayer player) {
        if (player == null || player.stats == null) return;
        var stats = player.stats.getCurrentGameStats();
        if (stats == null) return;
        Integer streak = stats.getWinstreak();
        Integer threshold = stats.getWinstreakThreshold();
        if (streak == null || threshold == null || (float) streak <= (float) threshold * 0.4F) return;
        NotificationBuilder builder = new NotificationBuilder();
        builder.title = "Winstreak";
        builder.parts = new ArrayList<>();
        builder.actions = new ArrayList<>();
        if (killer == null) {
            builder.lifetimeSteps = 100;
            builder.append(victim)
                    .addText("'s winstreak of ", 0xFFBEBCBD)
                    .append(Integer.toString(streak))
                    .addText(" has been ended.", 0xFFBEBCBD)
                    .show();
        } else {
            boolean self = killer.equals(mc.getSession().getUsername());
            String name = killer;
            if (self) {
                name = "You";
                SoundUtil.play("notification.winstreakend", 1.0F, 0.9F);
            }
            builder.lifetimeSteps = self ? 160 : 120;
            builder.append(name)
                    .addText(" ended ", 0xFFBEBCBD)
                    .append(victim)
                    .addText("'s winstreak of ", 0xFFBEBCBD)
                    .append(Integer.toString(streak))
                    .addText(".", 0xFFBEBCBD)
                    .show();
        }
    }

    @EventHandler
    public void onPlayerKill(PlayerKillEvent event) {
        if (!isActiveServer()) return;
        if (!(state instanceof TabTrackingGameState) || !event.finalKill) return;
        if (event.victim.equals(mc.getSession().getUsername())) return;
        var team = mc.theWorld.getScoreboard().getPlayersTeam(event.victim);
        if (team == null) return;
        var result = ((TabTrackingGameState) state).recordKill(event.victim, event.killer, team);
        if (!result.teamEliminated || !Adjust.hypixelApiManager.isHypixelAvailable()) return;
        for (NickedPlayer nick : result.eliminatedPlayers) {
            if (nick.realUuid == null) continue;
            String victim = nick.realName;
            String killer = event.killer;
            Adjust.hypixelApiManager.getPlayer(nick.realUuid).thenAccept(
                    player -> notifyWinstreakEnd(killer, victim, player));
        }
    }

    @EventHandler
    public void onPacket(LowLevelPacketReceiveEvent event) {
        if (!isActiveServer()) return;
        Packet<?> packet = event.packet;
        if (packet instanceof S3BPacketScoreboardObjective) {
            if (state.game != HypixelGame.UNKNOWN_GAME && state.game != HypixelGame.DUELS) return;
            String title = ((S3BPacketScoreboardObjective) packet).func_149337_d();
            if (title == null) return;
            HypixelGame game = HypixelGame.fromName(title);
            if ((state.game != HypixelGame.UNKNOWN_GAME && game == HypixelGame.UNKNOWN_GAME) || state.game == game) return;
            setGame(game.stateFactory.create().copyFrom(state).locate());
            if (state.lobby != null && !state.lobby) {
                if (state instanceof TabTrackingGameState) {
                    if (previousTrackedState == null || previousTrackedState.getClass() != state.getClass()) {
                        previousTrackedState = state;
                    }
                } else if (state.game != HypixelGame.UNKNOWN_GAME) {
                    previousTrackedState = null;
                }
                state.onGameServer();
            }
        } else if (packet instanceof S30PacketWindowItems) {
            return;
        } else if (packet instanceof S01PacketJoinGame) {
            setGame(HypixelGame.UNKNOWN_GAME.stateFactory.create());
        }
        state.onPacket(packet, true);
    }

    @EventHandler(priority = 20)
    public void onPacketApplied(PacketAppliedEvent event) {
        if (isActiveServer() && state != null) {
            state.onPacket(event.packet, false);
        }
    }

    @EventHandler
    public void onLocrawPacket(DirectionalPacketEvent event) {
        if (isActiveServer() && state != null) {
            state.parseLocraw(event);
        }
    }

    @EventHandler
    public void onUpdate(CancellablePlayerUpdateEvent event) {
        if (!event.isCancelled() && isActiveServer() && state != null) {
            state.onUpdate();
        }
    }

    @EventHandler
    public void onDisconnect(DisconnectTeardownEvent event) {
        if (isActiveServer()) {
            reset();
        }
    }

    public void setGame(HypixelGameState state) {
        if (ServerTracker.INSTANCE != null) {
            if (KillAuraModule.INSTANCE != null && KillAuraModule.INSTANCE.isEnabled()
                    && state.game == HypixelGame.MURDER_MYSTERY) {
                KillAuraModule.INSTANCE.setEnabled(false, true);
            }
        }
        this.state = state;
    }

    @SuppressWarnings("unused")
    private static void vendorMarkGameStateDirty() {
        if (ServerTracker.INSTANCE != null && Adjust.telemetryListener != null) {
            Adjust.telemetryListener.onGameState();
        }
    }
}
