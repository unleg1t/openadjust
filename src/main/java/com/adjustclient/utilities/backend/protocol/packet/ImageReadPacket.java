package com.adjustclient.utilities.backend.protocol.packet;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class ImageReadPacket extends CerberusPacket<BufferedImage> {
    public byte[] payload;
    public BufferedImage image;

    public ImageReadPacket(byte packetId, byte[] payload) {
        super(packetId);
        this.payload = payload;
    }

    @Override
    public void write(CerberusPacketData data) {
        data.buffer.writeVarIntToBuffer(payload.length);
        data.buffer.writeBytes(payload);
    }

    @Override
    public void read(CerberusPacketData data) throws IOException {
        image = readPng(data.buffer);
    }

    @Override
    public BufferedImage getResult() {
        return image;
    }
}
