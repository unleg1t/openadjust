package com.adjustclient.utilities.game.packet;

public class PacketHold {
    public PacketQueueFilter filter;
    public long sequence;
    public boolean active;
    public Object field20;

    public PacketHold(PacketQueueFilter filter, long sequence, boolean active) {
        this.filter = filter;
        this.sequence = sequence;
        this.active = active;
    }
}
