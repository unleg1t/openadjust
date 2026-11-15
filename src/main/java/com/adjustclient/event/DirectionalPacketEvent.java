package com.adjustclient.event;

public final class DirectionalPacketEvent extends CancellableEvent {
    public boolean outgoing;
    public net.minecraft.network.Packet<?> packet;

    public DirectionalPacketEvent(net.minecraft.network.Packet<?> packet, boolean outgoing) {
        this.packet = packet;
        this.outgoing = outgoing;
    }
}
