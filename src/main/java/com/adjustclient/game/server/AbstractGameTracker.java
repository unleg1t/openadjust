package com.adjustclient.game.server;
import com.adjustclient.event.Listener;
import com.adjustclient.server.ServerType;
import com.adjustclient.util.MinecraftInstance;
public abstract class AbstractGameTracker<G extends AbstractGameState<G,T>, T extends Enum<T> & ServerGame<G,T>> implements Listener, MinecraftInstance {
    public final ServerType server;
    public G state;
    protected AbstractGameTracker(ServerType server, G state) { this.server = server; this.state = state; }
}
