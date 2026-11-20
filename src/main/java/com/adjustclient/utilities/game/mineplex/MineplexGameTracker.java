package com.adjustclient.utilities.game.mineplex;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.LowLevelPacketReceiveEvent;
import com.adjustclient.utilities.game.server.AbstractGameTracker;
import com.adjustclient.utilities.server.ServerType;
import com.adjustclient.utilities.anticheat.ServerTracker;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S47PacketPlayerListHeaderFooter;
import net.minecraft.util.StringUtils;
public final class MineplexGameTracker extends AbstractGameTracker<MineplexGameState, MineplexGame> {
    public Object field12;
    public MineplexGameTracker(ServerType server, MineplexGameState initialState) { super(server, initialState); }
    @EventHandler public void onPacket(LowLevelPacketReceiveEvent event) {
        var serverState = ServerTracker.INSTANCE.serverState;
        if (serverState == null || serverState.antiCheatModel == null
                || serverState.antiCheatModel.serverType != this.server) {
            return;
        }
        Packet<?> packet = event.packet;
        if (packet instanceof S01PacketJoinGame) {
            state = MineplexGame.UNKNOWN_GAME.stateFactory.create();
        } else if (packet instanceof S47PacketPlayerListHeaderFooter
                && (state.game == MineplexGame.UNKNOWN_GAME || state.game == MineplexGame.LOBBY)) {
            String title = StringUtils.stripControlCodes(((S47PacketPlayerListHeaderFooter) packet).getHeader().getUnformattedText()).trim();
            MineplexGame detected = detectGame(title);
            if (state.game != detected) state = detected.stateFactory.create();
        }
    }
    public MineplexGame detectGame(String title) {
        if (title.startsWith("Mineplex Network")) return MineplexGame.LOBBY;
        for (MineplexGame game : MineplexGame.values()) {
            if (game.displayName.equalsIgnoreCase(title)) return game;
        }
        return MineplexGame.UNKNOWN_GAME;
    }
}
