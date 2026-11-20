package com.adjustclient.utilities.game.server;

import com.adjustclient.utilities.MinecraftInstance;
import com.adjustclient.utilities.Timer;

public abstract class AbstractGameState<G extends AbstractGameState<G, T>, T extends Enum<T> & ServerGame<G, T>> implements MinecraftInstance {
    public final T game;
    public Timer createdTimer;
    public Object field12;
    protected AbstractGameState(T game) { this.game = java.util.Objects.requireNonNull(game); createdTimer = new Timer(); }
}
