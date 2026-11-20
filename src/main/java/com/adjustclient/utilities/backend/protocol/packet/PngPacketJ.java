package com.adjustclient.utilities.backend.protocol.packet;

import com.adjustclient.utilities.backend.protocol.ImageIoHelper;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class PngPacketJ extends CerberusPacket<BufferedImage> {
    public BufferedImage writeImage;
    public BufferedImage readImage;
    public int paramA;

    public PngPacketJ(byte packetId, int paramA, BufferedImage writeImage) {
        super(packetId);
        this.paramA = paramA;
        this.writeImage = writeImage;
    }

    @Override
    public void write(CerberusPacketData data) {
        data.buffer.writeVarIntToBuffer(paramA);
        ImageIoHelper.writeImage(writeImage, data.buffer);
    }

    @Override
    public void read(CerberusPacketData data) throws IOException {
        readImage = readPng(data.buffer);
    }

    @Override
    public BufferedImage getResult() {
        return readImage;
    }
}
