package com.adjustclient.utilities.protocol.v1_12_2.play.server;

import com.adjustclient.utilities.protocol.v1_12_2.ProtocolPacket;
import com.adjustclient.utilities.protocol.v1_12_2.ProtocolPacketBuffer;
import com.adjustclient.utilities.protocol.v1_12_2.play.PlayClientHandler;
import java.io.IOException;

public class SPacketSetExperience implements ProtocolPacket<PlayClientHandler> {
    private float experienceBar;
    private int totalExperience;
    private int level;

    public SPacketSetExperience() {
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
