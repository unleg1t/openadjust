package com.adjustclient.event;

public final class PacketAppliedEvent extends Event {
    public net.minecraft.network.Packet<?> packet;

    public PacketAppliedEvent(net.minecraft.network.Packet<?> packet) {
        this.packet = packet;
    }
}
