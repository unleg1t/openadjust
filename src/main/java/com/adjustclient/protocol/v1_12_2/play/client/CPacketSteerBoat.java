package com.adjustclient.protocol.v1_12_2.play.client;

import com.adjustclient.protocol.v1_12_2.ProtocolPacket;
import com.adjustclient.protocol.v1_12_2.ProtocolPacketBuffer;
import com.adjustclient.protocol.v1_12_2.play.PlayServerHandler;
import java.io.IOException;

public class CPacketSteerBoat implements ProtocolPacket<PlayServerHandler> {
    private boolean left;
    private boolean right;

    public CPacketSteerBoat() {
    }

    @Override
    public void readPacketData(ProtocolPacketBuffer buffer) throws IOException {
    }

    @Override
    public void writePacketData(ProtocolPacketBuffer buffer) throws IOException {
    }

    @Override
    public void processPacket(PlayServerHandler handler) {
    }
}
