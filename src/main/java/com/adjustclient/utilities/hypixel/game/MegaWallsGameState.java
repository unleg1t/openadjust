package com.adjustclient.utilities.hypixel.game;
import com.adjustclient.Adjust;
import com.adjustclient.core.events.HypixelGameStartedEvent;
import com.adjustclient.utilities.hypixel.HypixelGame;
import com.adjustclient.utilities.hypixel.HypixelGameState;
import com.adjustclient.managers.telemetry.TelemetryRecord;
import com.adjustclient.utilities.Timer;
public final class MegaWallsGameState extends HypixelGameState implements TeamGame {
    public boolean bossBarSpawned;
    public Object field48;
    public MegaWallsGameState() { super(HypixelGame.MEGA_WALLS); }
    @Override public void onBossBarSpawn() { bossBarSpawned = true; }
    @Override public void onGameServer() {
        if (!bossBarSpawned) {
            HypixelGameState previous = Adjust.gameStateTracker.previousTrackedState;
            if (previous instanceof MegaWallsGameState) {
                if (previous.ended) Adjust.gameStateTracker.previousTrackedState = null;
                else Adjust.gameStateTracker.state = previous;
            }
            if (startTimer == null) {
                startTimer = new Timer();
            }
        }
        super.onGameServer();
    }

    @SuppressWarnings("unused")
    private void vendorOnGameServerTelemetry(TelemetryRecord fallbackStartRecord) {
        if (Adjust.telemetryListener != null) {
            Adjust.telemetryListener.onGameStart(new HypixelGameStartedEvent(this));
            if (fallbackStartRecord != null) {
                Adjust.telemetryListener.report(fallbackStartRecord);
            }
        }
    }
}
