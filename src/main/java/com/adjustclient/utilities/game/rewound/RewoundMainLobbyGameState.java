package com.adjustclient.utilities.game.rewound;

public final class RewoundMainLobbyGameState extends RewoundGameState {
    public RewoundMainLobbyGameState() {
        super(RewoundGame.MAIN);
    }

    @Override
    public RewoundGameState onCreated() {
        this.lobby = Boolean.TRUE;
        return super.onCreated();
    }
}
