package com.adjustclient.utilities.protocol.v1_12_2.play.server;

import com.adjustclient.utilities.protocol.v1_12_2.ProtocolPacket;
import com.adjustclient.utilities.protocol.v1_12_2.ProtocolPacketBuffer;
import com.adjustclient.utilities.protocol.v1_12_2.play.PlayClientHandler;
import java.io.IOException;

public class SPacketConfirmTransaction implements ProtocolPacket<PlayClientHandler> {
    private int windowId;
    private short actionNumber;
    private boolean accepted;

    public SPacketConfirmTransaction() {
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
