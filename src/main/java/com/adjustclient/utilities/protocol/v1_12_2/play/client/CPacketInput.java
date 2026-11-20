package com.adjustclient.utilities.protocol.v1_12_2.play.client;

import com.adjustclient.utilities.protocol.v1_12_2.ProtocolPacket;
import com.adjustclient.utilities.protocol.v1_12_2.ProtocolPacketBuffer;
import com.adjustclient.utilities.protocol.v1_12_2.play.PlayServerHandler;
import java.io.IOException;

public class CPacketInput implements ProtocolPacket<PlayServerHandler> {
    private float strafeSpeed;
    private float forwardSpeed;
    private boolean jumping;
    private boolean sneaking;

    public CPacketInput() {
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
