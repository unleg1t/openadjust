package com.adjustclient.hypixel.game;

import com.adjustclient.game.server.GameCountdown;
import com.adjustclient.hypixel.HypixelGame;
import com.adjustclient.hypixel.HypixelGameState;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S3EPacketTeams;

public final class ScubaSimulatorGameState extends HypixelGameState implements ScoreboardLineProvider {
    public GameCountdown countdown;
    public Object field48;
    public ScubaSimulatorGameState() { super(HypixelGame.SCUBA_SIMULATOR); }
    @Override public void onPacket(Packet<?> packet, boolean before) {
        super.onPacket(packet, before);
        if (before && packet instanceof S3EPacketTeams && Boolean.FALSE.equals(lobby)) {
            var parsed = parseCountdown((S3EPacketTeams) packet);
            if (parsed.getRight()) countdown = parsed.getLeft();
        }
    }
}
