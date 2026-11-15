package com.adjustclient.backend.protocol.packet;

import com.adjustclient.backend.protocol.ImageIoHelper;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

public class ImageStreamPacket extends CerberusPacket<BufferedImage> {
    public BufferedImage writeImage;
    public List<CerberusPacketData> entries;
    public BufferedImage readImage;

    public ImageStreamPacket(byte packetId, BufferedImage writeImage, List<CerberusPacketData> entries) {
        super(packetId);
        this.writeImage = writeImage;
        this.entries = entries;
    }

    @Override
    public void write(CerberusPacketData data) {
        data.buffer.writeVarIntToBuffer(entries.size());
        for (CerberusPacketData entry : entries) {
            data.buffer.writeInt(entry.id);
            ImageIoHelper.writeImage(entry.image, data.buffer);
        }
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
