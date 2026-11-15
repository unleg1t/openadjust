package com.adjustclient.game.server;

public interface ServerGame<G extends AbstractGameState<G, T>, T extends Enum<T> & ServerGame<G, T>> {
    @FunctionalInterface
    interface StateFactory<G> { G create(); }
}
