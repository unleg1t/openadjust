package com.adjustclient.utilities.backend.protocol.packet;

import java.awt.image.BufferedImage;
import net.minecraft.network.PacketBuffer;

public class CerberusPacketData {
    public PacketBuffer buffer;
    public BufferedImage image;
    public int id;

    public CerberusPacketData(PacketBuffer buffer) {
        this.buffer = buffer;
    }

    public CerberusPacketData(BufferedImage image, int id) {
        this.image = image;
        this.id = id;
    }
}
