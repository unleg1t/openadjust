package com.adjustclient.managers.util;

public class ClientInfoEntry {
    public final ClientInfoSource source;
    public final long timestamp;
    public final int sequence;

    public ClientInfoEntry(ClientInfoSource source, long timestamp, int sequence) {
        this.source = source;
        this.timestamp = timestamp;
        this.sequence = sequence;
    }
}
