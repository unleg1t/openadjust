package com.adjustclient.event;

public abstract class CancellableEvent extends Event {
    public boolean cancelled;

    public boolean isCancelled() { return cancelled; }
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
}
