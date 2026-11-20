package com.adjustclient.utilities.game.server;
import com.adjustclient.core.events.Listener;
import com.adjustclient.utilities.server.ServerType;
import com.adjustclient.utilities.MinecraftInstance;
public abstract class AbstractGameTracker<G extends AbstractGameState<G,T>, T extends Enum<T> & ServerGame<G,T>> implements Listener, MinecraftInstance {
    public final ServerType server;
    public G state;
    protected AbstractGameTracker(ServerType server, G state) { this.server = server; this.state = state; }
}
