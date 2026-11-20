package com.adjustclient.core.events;

public final class BindChangeEvent extends Event {
    public com.adjustclient.core.bind.Bind bind;
    public java.util.List<com.adjustclient.core.bind.BindKey> removed;
    public java.util.List<com.adjustclient.core.bind.BindKey> added;

    public BindChangeEvent(com.adjustclient.core.bind.Bind bind, java.util.List<com.adjustclient.core.bind.BindKey> removed, java.util.List<com.adjustclient.core.bind.BindKey> added) {
        this.bind = bind; this.removed = removed; this.added = added;
    }
}
