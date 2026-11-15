package com.adjustclient.event;

public final class LowLevelPacketReceiveEvent extends CancellableEvent {
    public net.minecraft.network.Packet<?> packet;

    public LowLevelPacketReceiveEvent(net.minecraft.network.Packet<?> packet) {
        this.packet = packet;
    }
}
