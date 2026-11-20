package com.adjustclient.utilities.game.rewound;

import com.adjustclient.utilities.game.server.ServerGame;
import net.minecraft.util.StringUtils;

public enum RewoundGame implements ServerGame<RewoundGameState, RewoundGame> {
    MAIN("Main Lobby", RewoundMainLobbyGameState::new, 32),
    SKYWARS("SkyWars", 0),
    BEDWARS("Bed Wars", RewoundBedWarsGameState::new, 0),
    PIT("Pit", RewoundPitGameState::new, 22),
    DUELS("Duels", 0),
    SURVIVAL_GAMES("Blitz SG", 0),
    REPLAY("Replay", 38),
    UNKNOWN_GAME("Unknown Game", 0);

    public final String displayName;
    public final ServerGame.StateFactory<RewoundGameState> stateFactory;
    public final int flags;

    RewoundGame(String displayName, int flags) {
        this.displayName = displayName;
        this.flags = flags;
        this.stateFactory = () -> new RewoundGameState(this);
    }

    RewoundGame(String displayName, ServerGame.StateFactory<RewoundGameState> stateFactory, int flags) {
        this.displayName = displayName;
        this.stateFactory = stateFactory;
        this.flags = flags;
    }

    public static RewoundGame fromName(String name) {
        if (name == null) return UNKNOWN_GAME;
        if ("BLITZ".equalsIgnoreCase(name)) return SURVIVAL_GAMES;
        try {
            return valueOf(StringUtils.stripControlCodes(name).replaceAll(" ", "_").toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return UNKNOWN_GAME;
        }
    }
}
