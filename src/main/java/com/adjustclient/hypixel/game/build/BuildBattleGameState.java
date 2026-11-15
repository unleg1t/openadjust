package com.adjustclient.hypixel.game.build;

import com.adjustclient.Adjust;
import com.adjustclient.hypixel.HypixelGame;
import com.adjustclient.hypixel.HypixelGameState;
import com.adjustclient.hypixel.game.LocrawResponse;
import com.adjustclient.server.ServerType;
import com.adjustclient.util.anticheat.ServerTracker;
import java.util.concurrent.CompletableFuture;

public final class BuildBattleGameState extends HypixelGameState {
    public BuildBattleGameState() { super(HypixelGame.BUILD_BATTLE); }
    public void acceptLocation(LocrawResponse location) {
        if ("BUILD_BATTLE_SPEED_BUILDERS".equals(location.mode)) {
            mc.addScheduledTask(() -> current(HypixelGame.SPEED_BUILDERS.stateFactory.create()));
        }
    }
    @Override public HypixelGameState locate() {
        CompletableFuture<LocrawResponse> future;
        if (locraw != null) future = CompletableFuture.completedFuture(locraw);
        else if (locrawFuture != null) future = locrawFuture;
        else {
            if (ServerTracker.INSTANCE.serverState == null
                    || ServerTracker.INSTANCE.serverState.antiCheatModel == null
                    || ServerTracker.INSTANCE.serverState.antiCheatModel.serverType != ServerType.HYPIXEL)
                throw new IllegalStateException("Unable to locate because we are not on Hypixel!");
            Adjust.chatMessageSender.sendMessage("/locraw", false);
            future = locrawFuture = new CompletableFuture<>();
        }
        future.thenAccept(location -> acceptLocation(location));
        return super.locate();
    }
}
