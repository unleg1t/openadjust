package com.adjustclient.backend.protocol.packet;

import com.adjustclient.backend.protocol.ImageIoHelper;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

public class PngReadPacket extends CerberusPacket<BufferedImage> {
    public List<BufferedImage> images;
    public BufferedImage image;

    public PngReadPacket(byte packetId, List<BufferedImage> images) {
        super(packetId);
        this.images = images;
    }

    @Override
    public void write(CerberusPacketData data) {
        data.buffer.writeVarIntToBuffer(images.size());
        for (BufferedImage img : images) {
            ImageIoHelper.writeImage(img, data.buffer);
        }
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
