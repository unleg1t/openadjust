package com.adjustclient.utilities.hypixel.game;
import com.adjustclient.modules.impl.exploit.playerdetect.hypixel.nick.NickedPlayer;
import java.util.Set;
public final class GamePlayer {
    public final Set<NickedPlayer> eliminatedPlayers;
    public final boolean teamEliminated;
    public Object field12;
    public GamePlayer(Set<NickedPlayer> players, boolean killed) {
        eliminatedPlayers = players;
        teamEliminated = killed;
    }
}
