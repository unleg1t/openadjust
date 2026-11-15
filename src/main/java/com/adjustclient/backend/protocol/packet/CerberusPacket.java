package com.adjustclient.backend.protocol.packet;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import javax.imageio.ImageIO;
import net.minecraft.network.PacketBuffer;

public abstract class CerberusPacket<T> {
    public final CountDownLatch latch = new CountDownLatch(1);
    public final byte packetId;

    protected CerberusPacket(byte packetId) {
        this.packetId = packetId;
    }

    public abstract void write(CerberusPacketData data) throws IOException;

    public abstract void read(CerberusPacketData data) throws IOException;

    public abstract T getResult();

    protected static BufferedImage readPng(PacketBuffer buffer) throws IOException {
        if (buffer.readBoolean()) {
            return null;
        }
        int length = buffer.readInt();
        byte[] bytes = new byte[length];
        buffer.readBytes(bytes);
        return ImageIO.read(new ByteArrayInputStream(bytes));
    }
}
