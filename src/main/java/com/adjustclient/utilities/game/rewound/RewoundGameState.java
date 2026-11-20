package com.adjustclient.utilities.game.rewound;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.EventBus;
import com.adjustclient.core.events.RewoundGameEndedEvent;
import com.adjustclient.utilities.game.server.AbstractGameState;
import com.adjustclient.modules.impl.render.hud.HUDModule;
import com.adjustclient.utilities.Timer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S39PacketPlayerAbilities;

public class RewoundGameState extends AbstractGameState<RewoundGameState, RewoundGame> {
    public Timer startTimer;
    public Boolean lobby;
    public Timer respawnTimer;
    public boolean ended;
    public boolean allowFlying;
    public boolean rejoined;

    public RewoundGameState(RewoundGame game) {
        super(game);
    }

    public RewoundGameState copyFrom(RewoundGameState previous) {
        this.startTimer = previous.startTimer;
        this.lobby = previous.lobby;
        return this;
    }

    public void onWorldReset() {
    }

    public void onPlayerMove(C03PacketPlayer packet) {
    }

    public void onPacket(Packet<?> packet, boolean before) {
        if (!before) return;
        if (packet instanceof S39PacketPlayerAbilities abilities) {
            if (Boolean.FALSE.equals(lobby) && abilities.isAllowFlying() != allowFlying) {
                allowFlying = abilities.isAllowFlying();
                if (!allowFlying) {
                    respawnTimer = new Timer();
                }
            }
        } else if (packet instanceof S08PacketPlayerPosLook && respawnTimer != null) {
            if (Boolean.FALSE.equals(lobby) && !respawnTimer.hasElapsed(70L)) {
                Adjust.rewoundDisablerTracker.trigger(RewoundDisablerType.RESPAWN);
            }
            respawnTimer = null;
        }
    }

    public RewoundGameState onCreated() {
        if ((game.flags & 0x10) != 0) {
            setLobby(false);
        }
        return this;
    }

    public void setLobby(boolean lobby) {
        this.lobby = lobby;
        if (!lobby && game != RewoundGame.UNKNOWN_GAME) {
            if (mc.isCallingFromMinecraftThread()) {
                onGameServer();
            } else {
                mc.addScheduledTask(this::onGameServer);
            }
        }
    }

    public void onGameServer() {
        if ((game.flags & 2) != 0) {
            startGame();
        }
        if (HUDModule.INSTANCE_ALT != null && HUDModule.INSTANCE_ALT.playerList != null) {
            HUDModule.INSTANCE_ALT.playerList.updateColumns();
        }
    }

    public void startGame() {
        if (startTimer == null) {
            startTimer = new Timer();
        }
    }

    public void finishGame() {
        ended = true;
        EventBus.post(new RewoundGameEndedEvent(this));
    }

    public boolean isInSpawn() {
        return false;
    }

    public boolean isStarted() {
        return startTimer != null && startTimer.hasElapsed(5000L) && !ended;
    }
}
