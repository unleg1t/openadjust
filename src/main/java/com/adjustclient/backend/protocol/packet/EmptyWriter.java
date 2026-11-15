package com.adjustclient.backend.protocol.packet;

import net.minecraft.network.PacketBuffer;

public class EmptyWriter implements PacketWriter {
    public byte[] data;

    @Override
    public void write(PacketBuffer buffer) {
    }
}
