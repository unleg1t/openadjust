package com.adjustclient.utilities.game.mineplex;
import com.adjustclient.utilities.game.server.ServerGame;
public enum MineplexGame implements ServerGame<MineplexGameState, MineplexGame> {
    BLOCK_HUNT("Block Hunt", 0), CAKE_WARS("Cake Wars", 0), CASTLE_SIEGE("Castle Siege", 0),
    CHAMPIONS("Champions", 0), DRAGONS("Dragons", 0), DRAGON_ESCAPE("Dragon Escape", 0),
    EVOLUTION("Evolution", 0), JUMP_ROPE("Jump Rope", 0), MICRO_BATTLE("Micro Battle", 2),
    MINE_STRKE("Mine-Strike", 0), ONE_IN_THE_QUIVER("One in the Quiver", 0), SKYWARS("Skywars", 0),
    SNAKE("Snake", 0), SPEED_BUILDERS("Speed Builders", 0), SPLEEF("Spleef", 0),
    SUPER_PAINTBALL("Super Paintball", 0), SURVIVAL_GAMES("Survival Games", 2),
    THE_BRIDGES("The Bridges", 0), TURF_WARS("Turf Wars", 0), LOBBY("Lobby", 6), UNKNOWN_GAME("Unknown Game", 0);
    public final String displayName;
    public final ServerGame.StateFactory<MineplexGameState> stateFactory;
    public final int flags;
    public Object field28;
    MineplexGame(String displayName, int flags) {
        this.displayName = displayName;
        this.flags = flags;
        stateFactory = () -> new MineplexGameState(this);
    }
}
