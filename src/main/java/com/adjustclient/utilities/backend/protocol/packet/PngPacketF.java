package com.adjustclient.utilities.backend.protocol.packet;

import com.adjustclient.utilities.backend.protocol.ImageIoHelper;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class PngPacketF extends CerberusPacket<BufferedImage> {
    public BufferedImage writeImage;
    public BufferedImage readImage;

    public PngPacketF(byte packetId, BufferedImage writeImage) {
        super(packetId);
        this.writeImage = writeImage;
    }

    @Override
    public void write(CerberusPacketData data) {
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
