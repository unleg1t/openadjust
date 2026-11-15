package com.adjustclient.protocol.v1_12_2.play.server;

import com.adjustclient.protocol.v1_12_2.ProtocolPacket;
import com.adjustclient.protocol.v1_12_2.ProtocolPacketBuffer;
import com.adjustclient.protocol.v1_12_2.play.PlayClientHandler;
import java.io.IOException;
import net.minecraft.network.play.server.S44PacketWorldBorder.Action;

public class SPacketWorldBorder implements ProtocolPacket<PlayClientHandler> {
    private Action action;
    private int size;
    private double centerX;
    private double centerZ;
    private double targetSize;
    private double diameter;
    private long timeUntilTarget;
    private int warningTime;
    private int warningDistance;

    public SPacketWorldBorder() {
    }

    @Override
    public void readPacketData(ProtocolPacketBuffer buffer) throws IOException {
    }

    @Override
    public void writePacketData(ProtocolPacketBuffer buffer) throws IOException {
    }

    @Override
    public void processPacket(PlayClientHandler handler) {
    }
}
