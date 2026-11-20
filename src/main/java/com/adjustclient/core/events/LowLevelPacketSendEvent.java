package com.adjustclient.core.events;

public final class LowLevelPacketSendEvent extends Event {
    public net.minecraft.network.Packet<?> packet;

    public LowLevelPacketSendEvent(net.minecraft.network.Packet<?> packet) {
        this.packet = packet;
    }
}
