package com.adjustclient.core.events;

public abstract class CancellableEvent extends Event {
    public boolean cancelled;

    public boolean isCancelled() { return cancelled; }
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
}
