package com.adjustclient.event;

public final class BindChangeEvent extends Event {
    public com.adjustclient.bind.Bind bind;
    public java.util.List<com.adjustclient.bind.BindKey> removed;
    public java.util.List<com.adjustclient.bind.BindKey> added;

    public BindChangeEvent(com.adjustclient.bind.Bind bind, java.util.List<com.adjustclient.bind.BindKey> removed, java.util.List<com.adjustclient.bind.BindKey> added) {
        this.bind = bind; this.removed = removed; this.added = added;
    }
}
